package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.CommentViewModel
import main.app.adapters.CommentAdapter
import main.app.dataModel.Comment

class CommentFragment : DialogFragment() {

    private val viewModel: CommentViewModel by viewModels()
    private lateinit var adapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView

    /**
     * Companion object to let us safely pass information into the CommentFragment.
     */
    companion object {
        fun newInstance(postId: Int, comments: List<Comment>): CommentFragment {
            val fragment = CommentFragment()
            val args = Bundle()
            args.putInt("postId", postId)
            args.putSerializable("comments", ArrayList(comments))
            fragment.arguments = args
            return fragment
        }
    }


    /**
     * Creates the view for comment fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }


    /**
     * When the view is created we initialize the handlers for buttons and input fields here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val postId = arguments?.getInt("postId") ?: return

        @Suppress("UNCHECKED_CAST")
        val initialComments = arguments?.getSerializable("comments") as? List<Comment> ?: emptyList()

        adapter = CommentAdapter(initialComments)
        recyclerView = view.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val commentToBeAdded = view.findViewById<EditText>(R.id.commentToAdd)
        val addCommentButton = view.findViewById<Button>(R.id.createCommentButton)
        val closeButton = view.findViewById<Button>(R.id.commentButton)

        closeButton.setOnClickListener { dismiss() }

        // Observe comments list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.comments.collect { comments ->
                adapter.updateData(comments)
            }
        }

        // Scroll to bottom and clear input after a comment is added
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.commentAdded.collect {
                recyclerView.scrollToPosition(adapter.itemCount - 1)
                commentToBeAdded.text.clear()
            }
        }

        // Show error toasts
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Load fresh comments from the server on open
        viewModel.loadComments(postId)

        /**
         * On click listener for the add comment button.
         * User clicks it when ready to add the comment to a post.
         */
        addCommentButton.setOnClickListener {
            val text = commentToBeAdded.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            viewModel.addComment(postId, text)
        }
    }


    /**
     * What to do when we start the fragment
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}