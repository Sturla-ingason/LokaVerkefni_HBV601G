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
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import main.app.R
import main.app.repository.PostRepository
import androidx.core.net.toUri

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {
    companion object {
        const val REQ_KEY = "camera_result"
        const val BUNDLE_URI = "photo_uri"
    }

    private var selectedPhotoUri: Uri? = null
    private val postRepository = PostRepository()

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedPhotoUri = it
            view?.findViewById<ImageView>(R.id.photoPreview)?.setImageURI(it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoPreview = view.findViewById<ImageView>(R.id.photoPreview)
        val addPhotoButton = view.findViewById<Button>(R.id.addPhotoButton)
        val postButton = view.findViewById<Button>(R.id.postButton)
        val postText = view.findViewById<EditText>(R.id.postText)

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

            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    var imageBytes: ByteArray? = null
                    var mimeType: String? = null
                    
                    selectedPhotoUri?.let { uri ->
                        val inputStream = requireContext().contentResolver.openInputStream(uri)
                        imageBytes = inputStream?.readBytes()
                        inputStream?.close()
                        mimeType = requireContext().contentResolver.getType(uri)
                    }

                    postRepository.createPost(description, imageBytes, mimeType)
                    
                    Toast.makeText(requireContext(), "Post created!", Toast.LENGTH_SHORT).show()
                    
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.flFragment, HomePage())
                        .commit()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Failed to create post", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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
