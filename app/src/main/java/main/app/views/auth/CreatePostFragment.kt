package main.app.views.auth

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.CreatePostState
import main.app.ViewModel.CreatePostViewModel
import main.app.repository.CameraRepository

/**
 * Fragment for creating a new post.
 * Allows the user to write a description and optionally attach a photo,
 * either taken with the camera or selected from the gallery.
 * Submits the post via CreatePostViewModel object and navigates to the
 * profile tab on success.
 */

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {

    private var selectedPhotoUri: Uri? = null
    private var pendingImageUri: Uri? = null

    private val viewModel: CreatePostViewModel by viewModels()
    private lateinit var cameraRepo: CameraRepository

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedPhotoUri = it
            view?.findViewById<ImageView>(R.id.photoPreview)?.setImageURI(it)
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingImageUri?.let { uri ->
                selectedPhotoUri = uri
                view?.findViewById<ImageView>(R.id.photoPreview)?.setImageURI(uri)
            }
        } else {
            Toast.makeText(requireContext(), "Camera cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_LONG).show()
        }
    }


    /**
     *Called when the fragments use is ready to be used.
     * Sets the click listener for add-photo and post buttons.
     *
     * @param view the root view of the fragment layout
     * @param savedInstanceState the previously saved state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraRepo = CameraRepository(requireContext())

        val addPhotoButton = view.findViewById<Button>(R.id.addPhotoButton)
        val postButton = view.findViewById<Button>(R.id.postButton)
        val postText = view.findViewById<EditText>(R.id.postText)

        viewModel.resetState()

        addPhotoButton.setOnClickListener { showPhotoDialog() }

        postButton.setOnClickListener {
            val description = postText.text.toString()

            if (description.isBlank() && selectedPhotoUri == null) {
                Toast.makeText(requireContext(), "Please add a description or a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
     * Shows the dialog for what to do in photos
     * cancel, import photo or take photo
     */
    private fun showPhotoDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Add Photo")
            .setPositiveButton("Take Photo") { _, _ ->
                checkPermissionAndLaunch()
            }
            .setNegativeButton("Import Photo") { _, _ ->
                pickImage.launch("image/*")
            }
            .setNeutralButton("Cancel", null)
            .show()
    }


    /**
     * Checks if the camers permision has been granted
     */
    private fun checkPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            launchCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }


    /**
     * Lunches the camera for the user to take a photo with
     */
    private fun launchCamera() {
        val uri = cameraRepo.createImageUri()
        pendingImageUri = uri

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }

        cameraLauncher.launch(intent)
    }
}