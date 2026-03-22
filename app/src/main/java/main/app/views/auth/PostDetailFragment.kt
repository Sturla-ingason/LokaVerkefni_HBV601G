package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
import main.app.dataModel.Post
import main.app.repository.CommentRepository
import main.app.repository.PostRepository

class PostDetailFragment : DialogFragment() {

    private lateinit var post: Post
    private val postRepository = PostRepository()
    private val commentRepository = CommentRepository()
    private lateinit var commentAdapter: CommentAdapter
    
    private lateinit var likeButton: Button
    private lateinit var likeCountText: TextView
    private var isLiked: Boolean = false
    private var currentLikeCount: Int = 0

    companion object {
        fun newInstance(post: Post): PostDetailFragment {
            val fragment = PostDetailFragment()
            val args = Bundle()
            args.putSerializable("post", post)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        post = arguments?.getSerializable("post") as Post
        isLiked = post.likedByCurrentUser ?: false
        currentLikeCount = post.likeCount ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val username = view.findViewById<TextView>(R.id.detailUsername)
        val description = view.findViewById<TextView>(R.id.detailDescription)
        likeButton = view.findViewById(R.id.detailLikeButton)
        likeCountText = view.findViewById(R.id.detailLikeCount)
        val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.detailCommentsRecyclerView)
        val commentInput = view.findViewById<EditText>(R.id.detailCommentInput)
        val addCommentButton = view.findViewById<Button>(R.id.detailAddCommentButton)
        val closeButton = view.findViewById<Button>(R.id.detailCloseButton)

        username.text = post.username ?: "Unknown User"
        description.text = post.description ?: ""
        updateLikeUI()

        // Like Button Logic
        likeButton.setOnClickListener {
            val postId = post.postID ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (isLiked) {
                        postRepository.unlikePost(postId)
                        isLiked = false
                        currentLikeCount--
                    } else {
                        postRepository.likePost(postId)
                        isLiked = true
                        currentLikeCount++
                    }
                    updateLikeUI()
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to update like", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Like Count Click Logic
        likeCountText.setOnClickListener {
            // Here you could show another dialog with the list of users who liked the post
            Toast.makeText(context, "Feature: See who liked (Coming soon)", Toast.LENGTH_SHORT).show()
        }

        // Comments RecyclerView
        commentAdapter = CommentAdapter(post.comments ?: emptyList())
        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.adapter = commentAdapter

        // Add Comment Logic
        addCommentButton.setOnClickListener {
            val text = commentInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            val postId = post.postID ?: return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        commentRepository.createComment(postId, text)
                    }
                    val updatedComments = withContext(Dispatchers.IO) {
                        commentRepository.getComments(postId)
                    }
                    commentAdapter.updateData(updatedComments)
                    commentInput.text.clear()
                    commentsRecyclerView.scrollToPosition(commentAdapter.itemCount - 1)
                } catch (e: Exception) {
                    Toast.makeText(context, "Failed to post comment", Toast.LENGTH_SHORT).show()
                }
            }
        }

        closeButton.setOnClickListener { dismiss() }
        
        // Load fresh comments if needed
        refreshComments()
    }

    private fun refreshComments() {
        val postId = post.postID ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val freshComments = withContext(Dispatchers.IO) {
                    commentRepository.getComments(postId)
                }
                commentAdapter.updateData(freshComments)
            } catch (e: Exception) {
                // Fail silently or log
            }
        }
    }

    private fun updateLikeUI() {
        likeButton.text = if (isLiked) "Unlike" else "Like"
        likeCountText.text = "$currentLikeCount Likes"
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
