package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.R
import main.app.adapters.CommentAdapter
import main.app.adapters.UserAdapter
import main.app.apiConnections.HttpRoutes
import main.app.dataModel.Post
import main.app.repository.CommentRepository
import main.app.repository.PostRepository
import main.app.repository.UserRepository

class PostDetailFragment : DialogFragment() {

    private lateinit var post: Post
    private val postRepository = PostRepository()
    private val commentRepository = CommentRepository()
    private val userRepository = UserRepository()
    private lateinit var commentAdapter: CommentAdapter

    private lateinit var likeButton: Button
    private lateinit var likeCountText: TextView
    private lateinit var descriptionText: TextView
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
        descriptionText = view.findViewById(R.id.detailDescription)
        val detailImage = view.findViewById<ImageView>(R.id.detailImage)
        likeButton = view.findViewById(R.id.detailLikeButton)
        likeCountText = view.findViewById(R.id.detailLikeCount)
        val editButton = view.findViewById<Button>(R.id.detailEditButton)
        val commentsRecyclerView = view.findViewById<RecyclerView>(R.id.detailCommentsRecyclerView)
        val commentInput = view.findViewById<EditText>(R.id.detailCommentInput)
        val addCommentButton = view.findViewById<Button>(R.id.detailAddCommentButton)
        val closeButton = view.findViewById<android.widget.ImageButton>(R.id.detailCloseButton)

        username.text = post.username ?: "Unknown User"
        descriptionText.text = post.description ?: ""

        // Show edit button if this is the current user's post
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val currentUser = withContext(Dispatchers.IO) { userRepository.getUser() }
                if (currentUser.userID == post.userId) {
                    editButton.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // If we can't fetch user, just leave the edit button hidden
            }
        }

        editButton.setOnClickListener {
            val postId = post.postID ?: return@setOnClickListener
            val editFragment = EditPostFragment.newInstance(
                postId = postId,
                currentDescription = descriptionText.text.toString(),
                onEdited = { updatedDescription ->
                    descriptionText.text = updatedDescription
                },
                onDeleted = {
                    dismiss()
                }
            )
            editFragment.show(parentFragmentManager, "EditPostFragment")
        }
        
        // Load image if available
        if (!post.imageIds.isNullOrEmpty()) {
            detailImage.visibility = View.VISIBLE
            val imageUrl = "${HttpRoutes.GET_IMAGE}/${post.imageIds!![0]}"
            Glide.with(this)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(detailImage)
        } else {
            detailImage.visibility = View.GONE
        }

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

        // Like Count Click Logic - Show list of users who liked
        likeCountText.setOnClickListener {
            showLikesDialog()
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
        
        refreshComments()
    }

    private fun showLikesDialog() {
        val postId = post.postID ?: return
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val likedUsers = withContext(Dispatchers.IO) {
                    postRepository.getLikes(postId)
                }
                
                if (likedUsers.isEmpty()) {
                    Toast.makeText(context, "No likes yet", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_likes_list, null)
                val recyclerView = dialogView.findViewById<RecyclerView>(R.id.likesRecyclerView)
                recyclerView.layoutManager = LinearLayoutManager(context)
                
                AlertDialog.Builder(requireContext())
                    .setView(dialogView)
                    .setNegativeButton("Close", null)
                    .show()

                // Pass an empty listener to UserAdapter to remove navigation
                val adapter = UserAdapter(likedUsers) { }
                recyclerView.adapter = adapter
                
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load likes", Toast.LENGTH_SHORT).show()
            }
        }
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
