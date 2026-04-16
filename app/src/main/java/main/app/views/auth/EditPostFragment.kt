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
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import main.app.R
import main.app.ViewModel.EditPostViewModel
import main.app.apiConnections.HttpRoutes

class EditPostFragment : DialogFragment() {

    private val viewModel: EditPostViewModel by viewModels()
    private var onPostEdited: ((String, List<Long>?) -> Unit)? = null
    private var onPostDeleted: (() -> Unit)? = null

    // Image state — kept in the fragment since it comes from user interaction and the gallery picker
    private var existingImageId: Long? = null
    private var removeExistingImage = false
    private var newImageUri: Uri? = null

    private lateinit var imagePreview: ImageView
    private lateinit var addImageButton: Button
    private lateinit var removeImageButton: Button
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button

    /**
     * Allows a user to pick the new image from the gallery for the post.
     * (if we pick a new image it cancells the removal of the olde one since we are replacing it either way)
     */
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        newImageUri = uri
        removeExistingImage = false
        imagePreview.setImageURI(uri)
        imagePreview.visibility = View.VISIBLE
        removeImageButton.visibility = View.VISIBLE
    }


    /**
     * Commpanion object that helps us safely pass data to create a new EditPost Fragment
     * @return returns a new EditPostFragment
     */
    companion object {
        fun newInstance(
            postId: Int,
            currentDescription: String,
            imageIds: List<Long>?,
            onEdited: (String, List<Long>?) -> Unit,
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


    /**
     * Creates the view and loads upp the xml file from dialog_edit_post
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_edit_post, container, false)
    }


    /**
     * Kinda the main function of the file, sets up observers and event handlers.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Read arguments
        val postId = arguments?.getInt("postId") ?: return
        val currentDescription = arguments?.getString("currentDescription") ?: ""
        existingImageId = arguments?.getLong("existingImageId", -1L).takeIf { it != -1L }

        // Connect XML elements
        val descriptionInput = view.findViewById<EditText>(R.id.editPostDescription)
        saveButton = view.findViewById(R.id.editPostSaveButton)
        val cancelButton = view.findViewById<Button>(R.id.editPostCancelButton)
        deleteButton = view.findViewById(R.id.editPostDeleteButton)
        imagePreview = view.findViewById(R.id.editPostImagePreview)
        addImageButton = view.findViewById(R.id.editPostAddImageButton)
        removeImageButton = view.findViewById(R.id.editPostRemoveImageButton)

        // Pre-fill description
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

        // Observe save result
        viewModel.saveResult.observe(viewLifecycleOwner) { updatedPost ->
            updatedPost ?: return@observe
            viewModel.clearSaveResult()
            onPostEdited?.invoke(updatedPost.description ?: "", updatedPost.imageIds)
            dismiss()
        }

        // Observe delete result
        viewModel.deleteComplete.observe(viewLifecycleOwner) { done ->
            done ?: return@observe
            viewModel.clearDeleteComplete()
            onPostDeleted?.invoke()
            parentFragmentManager.findFragmentByTag("PostDetailFragment")
                ?.let { (it as? DialogFragment)?.dismiss() }
            dismiss()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, ProfileFragment())
                .commit()
        }

        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { message ->
            message ?: return@observe
            viewModel.clearError()
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            saveButton.isEnabled = true
            deleteButton.isEnabled = true
        }

        // Opens the user gallery and allows them to pick a new image
        addImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }

        // Allows the user to remove an image from the post
        removeImageButton.setOnClickListener {
            newImageUri = null
            removeExistingImage = true
            imagePreview.setImageDrawable(null)
            imagePreview.visibility = View.GONE
            removeImageButton.visibility = View.GONE
        }

        // Saves the changes made to the post
        saveButton.setOnClickListener {
            val newDescription = descriptionInput.text.toString().trim()
            if (newDescription.isEmpty()) {
                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveButton.isEnabled = false
            val removeIds = if ((removeExistingImage || newImageUri != null) && existingImageId != null) {
                listOf(existingImageId!!)
            } else null
            viewModel.savePost(postId, newDescription, removeIds, newImageUri)
        }

        // Closes the dialog
        cancelButton.setOnClickListener { dismiss() }

        // Deletes the post after confirmation
        deleteButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteButton.isEnabled = false
                    viewModel.deletePost(postId)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }


    /**
     * Called when we need to start the dialog.
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}