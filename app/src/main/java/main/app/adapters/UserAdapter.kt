package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import main.app.R
import main.app.apiConnections.HttpRoutes
import main.app.dataModel.User

class UserAdapter(
    private var users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {


    /**
     * container object for the recycle viewer that we can populate
     * @param view  the view to get informatoin from
     */
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.userProfilePicture)
        val username: TextView = view.findViewById(R.id.userItemUsername)
    }


    /**
     * Creates a new viewholder with the user_item view inflated
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }


    /**
     * Gets the view holder and shows each user in the viewholder and displayes the viewholder.
     * wit the data from the user.
     */
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.username.text = user.username

        if (user.imageId != null) {
            val imageUrl = "${HttpRoutes.GET_IMAGE}/${user.imageId}"
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(holder.profileImage)
        } else {
            holder.profileImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onUserClick(user)
        }
    }


    /**
     * Gets the amount of users that are inn the list
     */
    override fun getItemCount() = users.size


    /**
     * updates the list of users with new data
     * @param newUsers the list of new useers data
     */
    fun updateData(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }
}
