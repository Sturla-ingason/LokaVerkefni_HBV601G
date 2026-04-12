package main.app.views.auth

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.R
import main.app.apiConnections.HttpRoutes
import main.app.repository.PostRepository

class EditPostFragment : DialogFragment() {

    private val postRepository = PostRepository()
    private var onPostEdited: ((String) -> Unit)? = null
    private var onPostDeleted: (() -> Unit)? = null

    // Image state
    private var existingImageId: Long? = null   // current image from the post
    private var removeExistingImage = false      // user wants to delete the existing image
    private var newImageUri: Uri? = null         // user picked a new image

    private lateinit var imagePreview: ImageView
    private lateinit var addImageButton: Button
    private lateinit var removeImageButton: Button

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        newImageUri = uri
        removeExistingImage = false             // picking a new image cancels any pending remove
        imagePreview.setImageURI(uri)
        imagePreview.visibility = View.VISIBLE
        removeImageButton.visibility = View.VISIBLE
    }

    companion object {
        fun newInstance(
            postId: Int,
            currentDescription: String,
            imageIds: List<Long>?,
            onEdited: (String) -> Unit,
            onDeleted: () -> Unit = {}
        ): EditPostFragment {
            val fragment = EditPostFragment()
            val args = Bundle()
            args.putInt("postId", postId)
            args.putString("currentDescription", currentDescription)
            if (!imageIds.isNullOrEmpty()) {
                args.putLong("existingImageId", imageIds[0])
            }
            fragment.arguments = args
            fragment.onPostEdited = onEdited
            fragment.onPostDeleted = onDeleted
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getInt("postId") ?: return
        val currentDescription = arguments?.getString("currentDescription") ?: ""
        val storedImageId = arguments?.getLong("existingImageId", -1L).takeIf { it != -1L }
        existingImageId = storedImageId

        val descriptionInput = view.findViewById<EditText>(R.id.editPostDescription)
        val saveButton = view.findViewById<Button>(R.id.editPostSaveButton)
        val cancelButton = view.findViewById<Button>(R.id.editPostCancelButton)
        val deleteButton = view.findViewById<Button>(R.id.editPostDeleteButton)
        imagePreview = view.findViewById(R.id.editPostImagePreview)
        addImageButton = view.findViewById(R.id.editPostAddImageButton)
        removeImageButton = view.findViewById(R.id.editPostRemoveImageButton)

        descriptionInput.setText(currentDescription)
        descriptionInput.setSelection(currentDescription.length)

        // Show existing image if the post has one
        if (existingImageId != null) {
            imagePreview.visibility = View.VISIBLE
            removeImageButton.visibility = View.VISIBLE
            Glide.with(this)
                .load("${HttpRoutes.GET_IMAGE}/$existingImageId")
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imagePreview)
        }

        addImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        removeImageButton.setOnClickListener {
            newImageUri = null
            removeExistingImage = true
            imagePreview.setImageDrawable(null)
            imagePreview.visibility = View.GONE
            removeImageButton.visibility = View.GONE
        }

        saveButton.setOnClickListener {
            val newDescription = descriptionInput.text.toString().trim()
            if (newDescription.isEmpty()) {
                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val removeIds = if (removeExistingImage && existingImageId != null) {
                        listOf(existingImageId!!)
                    } else null

                    var imageBytes: ByteArray? = null
                    var mimeType: String? = null
                    newImageUri?.let { uri ->
                        val stream = requireContext().contentResolver.openInputStream(uri)
                        imageBytes = stream?.readBytes()
                        stream?.close()
                        mimeType = requireContext().contentResolver.getType(uri)
                    }

                    val updatedPost = withContext(Dispatchers.IO) {
                        postRepository.editPost(postId, newDescription, removeIds, imageBytes, mimeType)
                    }
                    onPostEdited?.invoke(updatedPost.description ?: newDescription)
                    dismiss()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to save changes", Toast.LENGTH_SHORT).show()
                    saveButton.isEnabled = true
                }
            }
        }

        cancelButton.setOnClickListener { dismiss() }

        deleteButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteButton.isEnabled = false
                    viewLifecycleOwner.lifecycleScope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                postRepository.deletePost(postId)
                            }
                            onPostDeleted?.invoke()
                            parentFragmentManager.findFragmentByTag("PostDetailFragment")
                                ?.let { (it as? DialogFragment)?.dismiss() }
                            dismiss()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to delete post", Toast.LENGTH_SHORT).show()
                            deleteButton.isEnabled = true
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}