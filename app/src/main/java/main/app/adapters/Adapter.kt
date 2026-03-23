package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import main.app.R
import main.app.apiConnections.HttpRoutes
import main.app.dataModel.Post
import main.app.repository.PostRepository
import main.app.views.auth.CommentFragment
import main.app.views.auth.PostDetailFragment

class Adapter(
    private var postlist: List<Post>,
    private val scope: CoroutineScope,
    private val fragmentManager: FragmentManager
): RecyclerView.Adapter<Adapter.ViewHolder>() {

    private val postRepository = PostRepository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = postlist[position]

        holder.title.text = currentItem.username ?: "User #${currentItem.userId ?: "Unknown"}"
        holder.body.text = currentItem.description ?: "No content available"
        holder.likeCounter.text = (currentItem.likeCount ?: 0).toString()

        // Handle Image loading with Glide
        if (!currentItem.imageIds.isNullOrEmpty()) {
            holder.postImage.visibility = View.VISIBLE
            val imageUrl = "${HttpRoutes.GET_IMAGE}/${currentItem.imageIds[0]}"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.postImage)
        } else {
            holder.postImage.visibility = View.GONE
        }

        var isLiked = currentItem.likedByCurrentUser ?: false
        holder.likeButton.text = if (isLiked) "Unlike" else "Like"

        // Open Post Detail on Item Click
        holder.itemView.setOnClickListener {
            val detailFragment = PostDetailFragment.newInstance(currentItem)
            detailFragment.show(fragmentManager, "PostDetailFragment")
        }

        holder.likeButton.setOnClickListener {
            val postId = currentItem.postID ?: return@setOnClickListener
            if (isLiked) {
                isLiked = false
                holder.likeButton.text = "Like"
                holder.likeCounter.text = ((holder.likeCounter.text.toString().toIntOrNull() ?: 1) - 1).toString()
                scope.launch(Dispatchers.IO) { postRepository.unlikePost(postId) }
            } else {
                isLiked = true
                holder.likeButton.text = "Unlike"
                holder.likeCounter.text = ((holder.likeCounter.text.toString().toIntOrNull() ?: 0) + 1).toString()
                scope.launch(Dispatchers.IO) { postRepository.likePost(postId) }
            }
        }

        holder.commentButton.setOnClickListener {
            val postId = currentItem.postID ?: return@setOnClickListener
            val dialog = CommentFragment.newInstance(postId, currentItem.comments ?: emptyList())
            dialog.show(fragmentManager, "CommentFragment")
        }
    }

    override fun getItemCount(): Int {
        return postlist.size
    }

    fun updateData(newPosts: List<Post>) {
        postlist = newPosts
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.postTitle)
        val body = itemView.findViewById<TextView>(R.id.postBody)
        val postImage = itemView.findViewById<ImageView>(R.id.postImage)
        val likeButton = itemView.findViewById<Button>(R.id.likeButton)
        val likeCounter = itemView.findViewById<TextView>(R.id.likeCounter)
        val commentButton = itemView.findViewById<Button>(R.id.commentButton)
    }
}
