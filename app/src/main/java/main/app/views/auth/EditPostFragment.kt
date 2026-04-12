package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.R
import main.app.repository.PostRepository

class EditPostFragment : DialogFragment() {

    private val postRepository = PostRepository()
    private var onPostEdited: ((String) -> Unit)? = null
    private var onPostDeleted: (() -> Unit)? = null

    companion object {
        fun newInstance(postId: Int, currentDescription: String, onEdited: (String) -> Unit, onDeleted: () -> Unit = {}): EditPostFragment {
            val fragment = EditPostFragment()
            val args = Bundle()
            args.putInt("postId", postId)
            args.putString("currentDescription", currentDescription)
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

        val descriptionInput = view.findViewById<EditText>(R.id.editPostDescription)
        val saveButton = view.findViewById<Button>(R.id.editPostSaveButton)
        val cancelButton = view.findViewById<Button>(R.id.editPostCancelButton)
        val deleteButton = view.findViewById<Button>(R.id.editPostDeleteButton)

        descriptionInput.setText(currentDescription)
        descriptionInput.setSelection(currentDescription.length)

        saveButton.setOnClickListener {
            val newDescription = descriptionInput.text.toString().trim()
            if (newDescription.isEmpty()) {
                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveButton.isEnabled = false
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val updatedPost = withContext(Dispatchers.IO) {
                        postRepository.editPost(postId, newDescription)
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
                            // Dismiss both this dialog and the post detail dialog behind it
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