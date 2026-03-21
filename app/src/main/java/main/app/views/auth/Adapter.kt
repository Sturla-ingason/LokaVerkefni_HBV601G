package main.app.views.auth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import main.app.R
import main.app.dataModel.Post

class Adapter(private var postlist: List<Post>): RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return postlist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = postlist[position]
        
        // Try various common field names for title/username
        holder.title.text = currentItem.title 
            ?: currentItem.username 
            ?: "User #${currentItem.userId ?: currentItem.id ?: "Unknown"}"

        // Try various common field names for post content
        holder.body.text = currentItem.body 
            ?: currentItem.postText 
            ?: currentItem.description 
            ?: currentItem.content 
            ?: "No content available"
    }

    fun updateData(newPosts: List<Post>) {
        postlist = newPosts
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.postTitle)
        val body = itemView.findViewById<TextView>(R.id.postBody)
    }

}
