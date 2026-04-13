package main.app.views.auth

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.CreatePostState
import main.app.ViewModel.CreatePostViewModel

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {
    companion object {
        const val REQ_KEY = "camera_result"
        const val BUNDLE_URI = "photo_uri"
    }

    private var selectedPhotoUri: Uri? = null
    private val viewModel: CreatePostViewModel by viewModels()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedPhotoUri = it
            view?.findViewById<ImageView>(R.id.photoPreview)?.setImageURI(it)
        }
    }


    /**
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoPreview = view.findViewById<ImageView>(R.id.photoPreview)
        val addPhotoButton = view.findViewById<Button>(R.id.addPhotoButton)
        val postButton = view.findViewById<Button>(R.id.postButton)
        val postText = view.findViewById<EditText>(R.id.postText)

        // Reset state when the view is created so old success/error doesn't re-trigger
        viewModel.resetState()

        // Listener for taking a picture via CameraFragment
        parentFragmentManager.setFragmentResultListener(REQ_KEY, viewLifecycleOwner) { _, bundle ->
            val uriString = bundle.getString(BUNDLE_URI) ?: return@setFragmentResultListener
            val uri = uriString.toUri()
            selectedPhotoUri = uri
            photoPreview.setImageURI(uri)
        }

        addPhotoButton.setOnClickListener { showPhotoDialog() }

        postButton.setOnClickListener {
            val description = postText.text.toString()
            if (description.isBlank() && selectedPhotoUri == null) {
                Toast.makeText(requireContext(), "Please add a description or a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Read image bytes here since we need contentResolver from the fragment
            var imageBytes: ByteArray? = null
            var mimeType: String? = null
            selectedPhotoUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                imageBytes = inputStream?.readBytes()
                inputStream?.close()
                mimeType = requireContext().contentResolver.getType(uri)
            }

            viewModel.createPost(description, imageBytes, mimeType)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is CreatePostState.Loading -> postButton.isEnabled = false
                    is CreatePostState.Success -> {
                        Toast.makeText(requireContext(), "Post created!", Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                        // Switch to profile tab so the user sees their new post
                        requireActivity()
                            .findViewById<BottomNavigationView>(R.id.bottomNavigationView)
                            .selectedItemId = R.id.profile
                    }
                    is CreatePostState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        postButton.isEnabled = true
                    }
                    is CreatePostState.Idle -> postButton.isEnabled = true
                }
            }
        }
    }


    /**
     * This is the dialog to let the user pick how they want to add the photo
     */
    private fun showPhotoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setPositiveButton("Take Photo") { _, _ ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, CameraFragment())
                    .addToBackStack(null)
                    .commit()
            }
            .setNegativeButton("Import Photo") { _, _ ->
                pickImage.launch("image/*")
            }
            .setNeutralButton("Cancel", null)
            .show()
    }
}