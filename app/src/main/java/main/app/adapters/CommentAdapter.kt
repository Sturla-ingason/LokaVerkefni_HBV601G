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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_view, parent, false)
        return ViewHolder(itemView)
    }

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

    override fun getItemCount(): Int = commentList.size

    fun updateData(newComments: List<Comment>) {
        commentList = newComments
        notifyDataSetChanged()
    }

    fun setCurrentUserId(userId: Int?) {
        currentUserId = userId
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.userNameCommenter)
        val body: TextView = itemView.findViewById(R.id.comment)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteCommentButton)
    }
}