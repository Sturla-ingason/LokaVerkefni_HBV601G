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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.HomeViewModel
import main.app.ViewModel.PostDetailViewModel
import main.app.adapters.CommentAdapter
import main.app.adapters.UserAdapter
import main.app.apiConnections.HttpRoutes
import main.app.dataModel.Post
import main.app.dataModel.User

class PostDetailFragment : DialogFragment() {

    private lateinit var post: Post
    private val viewModel: PostDetailViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private lateinit var commentAdapter: CommentAdapter

    private lateinit var likeButton: Button
    private lateinit var likeCountText: TextView
    private lateinit var descriptionText: TextView
    private lateinit var editButton: Button
    private lateinit var commentsRecyclerView: RecyclerView
    private lateinit var commentInput: EditText

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
        editButton = view.findViewById(R.id.detailEditButton)
        commentsRecyclerView = view.findViewById(R.id.detailCommentsRecyclerView)
        commentInput = view.findViewById(R.id.detailCommentInput)
        val addCommentButton = view.findViewById<Button>(R.id.detailAddCommentButton)
        val closeButton = view.findViewById<android.widget.ImageButton>(R.id.detailCloseButton)

        username.text = post.username ?: "Unknown User"
        descriptionText.text = post.description ?: ""

        username.setOnClickListener {
            val userId = post.userId ?: return@setOnClickListener
            dismiss()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.flFragment, ProfileFragment.newInstance(userId))
                .addToBackStack(null)
                .commit()
        }

        editButton.setOnClickListener {
            val postId = post.postID ?: return@setOnClickListener
            EditPostFragment.newInstance(
                postId = postId,
                currentDescription = descriptionText.text.toString(),
                imageIds = post.imageIds,
                onEdited = { updatedDescription -> descriptionText.text = updatedDescription },
                onDeleted = {
                    (parentFragment as? ProfileFragment)?.refreshPosts()
                    dismiss() }
            ).show(parentFragmentManager, "EditPostFragment")
        }

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

        commentAdapter = CommentAdapter(emptyList())
        commentsRecyclerView.layoutManager = LinearLayoutManager(context)
        commentsRecyclerView.adapter = commentAdapter

        likeButton.setOnClickListener {
            val postId = post.postID ?: return@setOnClickListener
            homeViewModel.toggleLike(postId)
        }

        likeCountText.setOnClickListener {
            val postId = post.postID ?: return@setOnClickListener
            viewModel.loadLikes(postId)
        }

        addCommentButton.setOnClickListener {
            val text = commentInput.text.toString().trim()
            val postId = post.postID ?: return@setOnClickListener
            if (text.isEmpty()) return@setOnClickListener
            viewModel.addComment(postId, text)
        }

        closeButton.setOnClickListener { dismiss() }

        // Observe like state from the shared HomeViewModel (single source of truth)
        viewLifecycleOwner.lifecycleScope.launch {
            homeViewModel.posts.collect { posts ->
                val current = posts.find { it.postID == post.postID } ?: return@collect
                likeButton.text = if (current.likedByCurrentUser == true) "Unlike" else "Like"
                likeCountText.text = "${current.likeCount ?: 0} Likes"
            }
        }

        // Observe edit button visibility
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.showEditButton.collect { show ->
                editButton.visibility = if (show) View.VISIBLE else View.GONE
            }
        }

        // Observe comments list
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.comments.collect { comments ->
                commentAdapter.updateData(comments)
            }
        }

        // Observe liked users and show dialog
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.likedUsers.collect { users ->
                showLikesDialog(users)
            }
        }

        // Observe comment added to clear input and scroll
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.commentAdded.collect {
                commentInput.text.clear()
                commentsRecyclerView.scrollToPosition(commentAdapter.itemCount - 1)
            }
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Initial data loads
        viewModel.checkEditButton(post.userId)
        post.postID?.let { viewModel.loadComments(it) }
    }

    private fun showLikesDialog(likedUsers: List<User>) {
        if (likedUsers.isEmpty()) {
            Toast.makeText(context, "No likes yet", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_likes_list, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.likesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .show()

        recyclerView.adapter = UserAdapter(likedUsers) { }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}