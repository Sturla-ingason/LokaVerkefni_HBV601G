package main.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import main.app.R
import main.app.dataModel.Notification

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {


    /**
     * creates a new viewholder and infaltes it with the notificaiont item view.
     * @return viewholder object
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.notification_item, parent, false)
        return ViewHolder(view)
    }


    /**
     * Shows the all the notifications inn the recycle view with the view holders containers
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]

        holder.message.text = notification.message ?: "You have a new notification"

        holder.unreadDot.visibility = if (notification.read == true) View.GONE else View.VISIBLE

        holder.icon.setImageResource(
            when (notification.type) {
                "LIKE" -> R.drawable.create
                "COMMENT" -> R.drawable.search
                "FOLLOW" -> R.drawable.profile
                else -> R.drawable.notification
            }
        )

        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }
    }


    /**
     * gets the amount of notifications that we have to show
     */
    override fun getItemCount() = notifications.size


    /**
     * updates the notification data with new inoformation
     * @param newNotifications the new notificaiton data.
     */
    fun updateData(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }



    /**
     * the container for the recycle viewer. each element.
     */
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val message: TextView = itemView.findViewById(R.id.notificationMessage)
        val unreadDot: View = itemView.findViewById(R.id.unreadDot)
        val icon: ImageView = itemView.findViewById(R.id.notificationIcon)
    }
}
