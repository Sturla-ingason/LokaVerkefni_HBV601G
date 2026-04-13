package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import main.app.R
import main.app.adapters.FollowListAdapter
import main.app.repository.UserRepository

class FollowListFragment : DialogFragment() {

    private val userRepository = UserRepository()

    companion object {
        const val MODE_FOLLOWERS = "Followers"
        const val MODE_FOLLOWING = "Following"

        fun newInstance(userId: Int, mode: String, isOwnProfile: Boolean): FollowListFragment {
            val fragment = FollowListFragment()
            val args = Bundle()
            args.putInt("userId", userId)
            args.putString("mode", mode)
            args.putBoolean("isOwnProfile", isOwnProfile)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_follow_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt("userId") ?: return
        val mode = arguments?.getString("mode") ?: return
        val isOwnProfile = arguments?.getBoolean("isOwnProfile") ?: false

        view.findViewById<TextView>(R.id.followListTitle).text = mode
        view.findViewById<ImageButton>(R.id.followListCloseButton).setOnClickListener { dismiss() }

        val recyclerView = view.findViewById<RecyclerView>(R.id.followListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Declare as lateinit so the action callback can reference it
        lateinit var adapter: FollowListAdapter

        adapter = FollowListAdapter(
            users = mutableListOf(),
            mode = mode,
            isOwnProfile = isOwnProfile,
            onUsernameClick = { user ->
                val uid = user.userID ?: return@FollowListAdapter
                dismiss()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.flFragment, ProfileFragment.newInstance(uid))
                    .addToBackStack(null)
                    .commit()
            },
            onActionClick = { user, button ->
                val uid = user.userID ?: return@FollowListAdapter
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            if (mode == MODE_FOLLOWING) userRepository.unfollowUser(uid)
                            else userRepository.removeFollower(uid)
                        }
                        adapter.removeUser(user)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Action failed", Toast.LENGTH_SHORT).show()
                        button.isEnabled = true
                    }
                }
            }
        )

        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val users = withContext(Dispatchers.IO) {
                    if (mode == MODE_FOLLOWERS) userRepository.getFollowers(userId)
                    else userRepository.getFollowing(userId)
                }
                adapter.updateData(users)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load $mode", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}