package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import main.app.R
import main.app.dataModel.User

class FollowListAdapter(
    private var users: MutableList<User>,
    private val mode: String,
    private val isOwnProfile: Boolean,
    private val onUsernameClick: (User) -> Unit,
    private val onActionClick: (User, Button) -> Unit
) : RecyclerView.Adapter<FollowListAdapter.ViewHolder>() {


    /**
     *
     */
    companion object {
        const val MODE_FOLLOWERS = "Followers"
        const val MODE_FOLLOWING = "Following"
    }


    /**
     * Container for the data for the recycle viewer
     * @param view the view that is used inn the viewholder
     */
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val picture: ImageView = view.findViewById(R.id.followListItemPicture)
        val username: TextView = view.findViewById(R.id.followListItemUsername)
        val actionButton: Button = view.findViewById(R.id.followListItemActionButton)
    }


    /**
     * infaltes a new viewholder with a view for the recycle viewer
     * a new row for the recycle viewer
     * @param parent what view to use
     * @param viewType what view it is
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.follow_list_item, parent, false)
        return ViewHolder(view)
    }


    /**
     * Shows all the user viewholders that are available and shows them
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.username.text = user.username
        holder.picture.setImageResource(android.R.drawable.ic_menu_gallery)

        if (isOwnProfile) {
            holder.actionButton.visibility = View.VISIBLE
            holder.actionButton.text = if (mode == MODE_FOLLOWERS) "Remove" else "Unfollow"
            holder.actionButton.setOnClickListener {
                holder.actionButton.isEnabled = false
                onActionClick(user, holder.actionButton)
            }
        } else {
            holder.actionButton.visibility = View.GONE
        }

        holder.username.setOnClickListener { onUsernameClick(user) }
        holder.itemView.setOnClickListener { onUsernameClick(user) }
    }


    /**
     * Get the ammount of users that we have to handle
     */
    override fun getItemCount() = users.size


    /**
     * removes a user from the list
     */
    fun removeUser(user: User) {
        val index = users.indexOfFirst { it.userID == user.userID }
        if (index != -1) {
            users.removeAt(index)
            notifyItemRemoved(index)
        }
    }


    /**
     * updated the data of the user list
     * @param new user list data
     */
    fun updateData(newUsers: List<User>) {
        users = newUsers.toMutableList()
        notifyDataSetChanged()
    }
}