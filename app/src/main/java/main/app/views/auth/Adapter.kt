package main.app.views.auth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import main.app.R
import main.app.dataModel.Post

class Adapter(private val postlist: ArrayList<Post>): RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_view, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return postlist.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = postlist[position]
        holder.title.text = currentItem.title
        holder.body.text = currentItem.body
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val title = itemView.findViewById<TextView>(R.id.postTitle)
        val body = itemView.findViewById<TextView>(R.id.postBody)
    }

}