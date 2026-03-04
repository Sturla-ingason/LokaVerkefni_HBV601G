package main.app.views.auth

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import main.app.R
import androidx.core.net.toUri

class CreatePostFragment : Fragment(R.layout.fragment_create_post) {
    companion object {
        const val REQ_KEY = "camera_result"
        const val BUNDLE_URI = "photo_uri"
    }

    private var selectedPhotoUri: Uri? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoPreview = view.findViewById<ImageView>(R.id.photoPreview)
        val addPhotoButton = view.findViewById<Button>(R.id.addPhotoButton)
        val postButton = view.findViewById<Button>(R.id.postButton)

        parentFragmentManager.setFragmentResultListener(REQ_KEY, viewLifecycleOwner) { _, bundle ->
            val uriString = bundle.getString(BUNDLE_URI) ?: return@setFragmentResultListener
            val uri = uriString.toUri()
            selectedPhotoUri = uri
            photoPreview.setImageURI(uri)
        }

        addPhotoButton.setOnClickListener { showPhotoDialog() }

        postButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, HomePage())
                .commit()
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
            .setNegativeButton("Import Photo", null)
            .setNeutralButton("Cancel", null)
            .show()
    }
}