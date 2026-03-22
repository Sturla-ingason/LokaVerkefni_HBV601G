package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import main.app.R
import main.app.dataModel.Post
import main.app.repository.PostRepository
import main.app.views.auth.CommentFragment

class Adapter(
    private var postlist: List<Post>,
    private val scope: CoroutineScope,
    private val fragmentManager: FragmentManager
): RecyclerView.Adapter<Adapter.ViewHolder>() {

    private val postRepository = PostRepository()

    /**
     * Inflates the post_view layout and wraps it in a ViewHolder.
     * Called by the RecyclerView when it needs a new container for a post item.
     * @param parent the RecyclerView that this view will be attached to
     * @param viewType the type of view (unused here, only one type of post view)
     * @return a new ViewHolder containing the inflated post view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_view, parent, false)
        return ViewHolder(itemView)
    }



    /**
     * Binds post data to the ViewHolder at the given scroll position.
     * Sets the title and body text views, falling back through
     * multiple field names if some are null.
     * @param holder the ViewHolder to populate
     * @param position the index of the post in the list
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = postlist[position]

        holder.title.text = currentItem.username ?: "User #${currentItem.userId ?: "Unknown"}"
        holder.body.text = currentItem.description ?: "No content available"
        holder.likeCounter.text = (currentItem.likeCount ?: 0).toString()

        var isLiked = currentItem.likedByCurrentUser ?: false
        holder.likeButton.text = if (isLiked) "Unlike" else "Like"


        /**
         * Event handler for liking and unlikeing a post
         */
        holder.likeButton.setOnClickListener {
            val postId = currentItem.postID ?: return@setOnClickListener
            if (isLiked) {
                isLiked = false
                holder.likeButton.text = "Like"
                holder.likeCounter.text = ((holder.likeCounter.text.toString().toIntOrNull() ?: 1) - 1).toString()
                scope.launch(Dispatchers.IO) { postRepository.unlikePost(postId) }
            } else {
                isLiked = true
                holder.likeButton.text = "Dislike"
                holder.likeCounter.text = ((holder.likeCounter.text.toString().toIntOrNull() ?: 0) + 1).toString()
                scope.launch(Dispatchers.IO) { postRepository.likePost(postId) }
            }
        }


        /**
         * event handler for comment button
         */
        holder.commentButton.setOnClickListener {
            val postId = currentItem.postID ?: return@setOnClickListener
            val dialog = CommentFragment.newInstance(postId, currentItem.comments ?: emptyList())
            dialog.show(fragmentManager, "CommentFragment")
        }
    }


    /**
     *  auka shit sem ég tók út til að prófa
     *         // Try various common field names for title/username
     *         holder.title.text = currentItem.title
     *             ?: currentItem.username
     *             ?: "User #${currentItem.userId ?: currentItem.id ?: "Unknown"}"
     *
     *         // Try various common field names for post content
     *         holder.body.text = currentItem.body
     *             ?: currentItem.postText
     *             ?: currentItem.description
     *             ?: currentItem.content
     *             ?: "No content available"
     */



    /**
     * Allows us to see how many posts are in the list total
     * @return postlist size
     */
    override fun getItemCount(): Int {
        return postlist.size
    }


    /**
     *
     */
    fun updateData(newPosts: List<Post>) {
        postlist = newPosts
        notifyDataSetChanged()
    }


    /**
     * Holds references to the views within a single post item.
     * Avoids repeated calls to findViewById when the RecyclerView recycles items.
     * @param itemView the inflated post_view layout for this item
     */
    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.postTitle)
        val body = itemView.findViewById<TextView>(R.id.postBody)

        val likeButton = itemView.findViewById<Button>(R.id.likeButton)

        val likeCounter = itemView.findViewById<TextView>(R.id.likeCounter)

        val commentButton = itemView.findViewById<Button>(R.id.commentButton)
    }

}