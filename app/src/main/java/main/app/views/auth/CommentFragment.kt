package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.R
import main.app.adapters.CommentAdapter
import main.app.dataModel.Comment
import main.app.repository.CommentRepository

class CommentFragment : DialogFragment() {

    private lateinit var adapter: CommentAdapter
    private lateinit var recyclerView: RecyclerView
    private val commentRepository = CommentRepository()

    /**
     *
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
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_comments, container, false)
    }


    /**
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        @Suppress("UNCHECKED_CAST")
        val comments = arguments?.getSerializable("comments") as? List<Comment> ?: emptyList()

        adapter = CommentAdapter(comments)
        recyclerView = view.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        /**
         * Event handler for button
         */
        val closeButton = view.findViewById<Button>(R.id.commentButton)
        closeButton.setOnClickListener {
            dismiss()
        }


        val postId = arguments?.getInt("postId") ?: return

        val commentToBeAdded = view.findViewById<EditText>(R.id.commentToAdd)
        val addCommentButton = view.findViewById<Button>(R.id.createCommentButton)


        /**
         *
         */
        addCommentButton.setOnClickListener {
            val text = commentToBeAdded.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    commentRepository.createComment(postId, text)
                }
                val updated = withContext(Dispatchers.IO) {
                    commentRepository.getComments(postId)
                }
                adapter.updateData(updated)
                recyclerView.scrollToPosition(adapter.itemCount - 1)
                commentToBeAdded.text.clear()
            }
        }

    }


    /**
     *
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}