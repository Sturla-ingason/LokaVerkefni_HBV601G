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
     *  Kinda the main function of the file, helps with connections to data base through the
     *  repository classes and the event handlers for everything
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //reading back out of the bundle the infromation that we need
        val postId = arguments?.getInt("postId") ?: return
        val currentDescription = arguments?.getString("currentDescription") ?: ""
        val storedImageId = arguments?.getLong("existingImageId", -1L).takeIf { it != -1L }
        existingImageId = storedImageId


        //Conecting all the xml elements that we need
        val descriptionInput = view.findViewById<EditText>(R.id.editPostDescription)
        val saveButton = view.findViewById<Button>(R.id.editPostSaveButton)
        val cancelButton = view.findViewById<Button>(R.id.editPostCancelButton)
        val deleteButton = view.findViewById<Button>(R.id.editPostDeleteButton)
        imagePreview = view.findViewById(R.id.editPostImagePreview)
        addImageButton = view.findViewById(R.id.editPostAddImageButton)
        removeImageButton = view.findViewById(R.id.editPostRemoveImageButton)


        //Sets the current descriptoin inn the input box for change
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


        //Opens the user gallery and allows them to pick a new image
        addImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }


        //Allows the user to remove a image from the post
        removeImageButton.setOnClickListener {
            newImageUri = null
            removeExistingImage = true
            imagePreview.setImageDrawable(null)
            imagePreview.visibility = View.GONE
            removeImageButton.visibility = View.GONE
        }


        //Saves the changes made to the post inn the edit dialog
        saveButton.setOnClickListener {
            //Geting the new discription and error handeling for it
            val newDescription = descriptionInput.text.toString().trim()
            if (newDescription.isEmpty()) {
                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //When the safe button is clicked we disable it straight away so that it can not be clicked again
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

                    //Call to the api for the update post call
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

        //Closes the dialog
        cancelButton.setOnClickListener { dismiss() }

        //Event handler for the delete button
        deleteButton.setOnClickListener {
            //Creates a alert dialog to ask if the user really want's to delete the post
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