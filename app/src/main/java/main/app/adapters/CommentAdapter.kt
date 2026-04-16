package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import main.app.R
import main.app.dataModel.Comment

class CommentAdapter(
    private var commentList: List<Comment>,
    private var currentUserId: Int? = null,
    private val onDelete: ((commentId: Int) -> Unit)? = null
) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {


    /**
     * Creates a new viewholder that we can use to show a new line in the recycle viewer
     * @return View holder object inflated with the comment view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_view, parent, false)
        return ViewHolder(itemView)
    }


    /**
     * Creates and shows all the viewholder objects inn the recycle viewer
     * @param ViewHolder the container to populate with data
     * @param position where we are inn the comment array/ which comment to display next
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = commentList[position]
        holder.username.text = currentItem.username ?: "User #${currentItem.userId ?: "Unknown"}"
        holder.body.text = currentItem.comment ?: ""

        val isOwn = currentUserId != null && currentItem.userId == currentUserId
        if (isOwn && onDelete != null) {
            holder.deleteButton.visibility = View.VISIBLE
            holder.deleteButton.setOnClickListener {
                currentItem.commentID?.let { id -> onDelete.invoke(id) }
            }
        } else {
            holder.deleteButton.visibility = View.GONE
        }
    }


    /**
     *  get the ammount of comments on a post that we need to show
     */
    override fun getItemCount(): Int = commentList.size


    /**
     * updateds the data of the comments
     * @param newComments the new comment data
     */
    fun updateData(newComments: List<Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }



    /**
     * Allows the adapter to keep track of the current user id
     * @param userId the id of the user
     */
    fun setCurrentUserId(userId: Int?) {
        currentUserId = userId
        notifyDataSetChanged()
    }


    /**
     * Container for each element inn the recycle viewer
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.userNameCommenter)
        val body: TextView = itemView.findViewById(R.id.comment)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteCommentButton)
    }
}