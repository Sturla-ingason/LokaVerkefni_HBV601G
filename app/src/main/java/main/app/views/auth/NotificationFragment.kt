package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.NotificationViewModel
import main.app.adapters.NotificationAdapter

class NotificationFragment : Fragment() {

    private val viewModel: NotificationViewModel by activityViewModels()
    private lateinit var adapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val badge = view.findViewById<TextView>(R.id.unreadCountBadge)

        adapter = NotificationAdapter(emptyList()) { notification ->
            val id = notification.id ?: return@NotificationAdapter
            if (notification.read != true) {
                viewModel.markAsRead(id)
            }
        }

        val recyclerView: RecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.notifications.collect { notifications ->
                adapter.updateData(notifications)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.unreadCount.collect { count ->
                if (count > 0) {
                    badge.visibility = View.VISIBLE
                    badge.text = count.toString()
                } else {
                    badge.visibility = View.GONE
                }
            }
        }

        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }
}
