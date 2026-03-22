package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import main.app.R
import main.app.dataModel.Comment

class CommentAdapter(private var commentList: List<Comment>) : RecyclerView.Adapter<CommentAdapter.ViewHolder>() {


    /**
     * sets the view for the view holder
     * @return a viewHolder with the view set
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.comment_view, parent, false)
        return ViewHolder(itemView)
    }


    /**
     *
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = commentList[position]
        holder.username.text = currentItem.username ?: "User #${currentItem.userId ?: "Unknown"}"
        holder.body.text = currentItem.comment ?: ""
    }


    /**
     * allows us to get the amount of comments under a post
     * @return the number of comments under a post
     */
    override fun getItemCount(): Int {
        return commentList.size
    }


    /**
     * Allows us to updated the comment list
     */
    fun updateData(newComments: List<Comment>) {
        commentList = newComments
    }


    /**
     * Allows us to define and find the element of each repetitve card in the
     * recycle viewer
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.userNameCommenter)
        val body = itemView.findViewById<TextView>(R.id.comment)
    }
}