package main.app.views.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import main.app.R
import main.app.repository.CameraRepository

/**
 * User Story #2: "As a user, I want to create a post using my phone camera"
 *
 * Flow:
 *  1. User taps "Take Photo" → camera permission is checked/requested
 *  2. System camera opens and saves the photo to a URI from CameraRepository
 *  3. Photo is shown in the preview
 *  4. User taps "Post" → TODO: wire up to PostRepository once US #13 defines the final Post model
 */
class CameraFragment : Fragment() {

    private lateinit var cameraRepo: CameraRepository
    private lateinit var imagePreview: ImageView
    private lateinit var takePhotoButton: Button
    private lateinit var postButton: Button

    /** URI prepared before launching the camera — camera writes the photo here */
    private var pendingImageUri: Uri? = null

    // Called when the system camera returns
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            pendingImageUri?.let { uri ->
                imagePreview.setImageURI(uri)
                imagePreview.visibility = View.VISIBLE
                postButton.isEnabled = true
            }
        } else {
            Toast.makeText(requireContext(), "Camera cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Called after the user responds to the permission dialog
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_LONG).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraRepo = CameraRepository(requireContext())
        imagePreview  = view.findViewById(R.id.imagePreview)
        takePhotoButton = view.findViewById(R.id.takePhotoButton)
        postButton    = view.findViewById(R.id.postButton)

        postButton.isEnabled = false

        takePhotoButton.setOnClickListener { checkPermissionAndLaunch() }
        postButton.setOnClickListener     { submitPost() }
    }

    private fun checkPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val uri = cameraRepo.createImageUri()
        pendingImageUri = uri
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
        }
        cameraLauncher.launch(intent)
    }

    private fun submitPost() {
        val uri = pendingImageUri ?: return

        // TODO: Once US #13 (create post) finalises the Post data model to include an imageUri,
        //       pass uri.toString() into the shared PostRepository here.
        Toast.makeText(requireContext(), "Photo posted!", Toast.LENGTH_SHORT).show()

        // Reset UI
        imagePreview.visibility = View.GONE
        imagePreview.setImageURI(null)
        postButton.isEnabled = false
        pendingImageUri = null
    }
}