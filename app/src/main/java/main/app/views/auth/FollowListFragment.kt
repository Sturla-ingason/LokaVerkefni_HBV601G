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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.FollowListViewModel
import main.app.adapters.FollowListAdapter

class FollowListFragment : DialogFragment() {

    private val viewModel: FollowListViewModel by viewModels()

    companion object {
        const val MODE_FOLLOWERS = "Followers"
        const val MODE_FOLLOWING = "Following"


        /**
         *
         */
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


    /**
     * inflates the dialgo with the right view or dilog_follow_list
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_follow_list, container, false)
    }


    /**
     * Called when the view is created.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userId = arguments?.getInt("userId") ?: return
        val mode = arguments?.getString("mode") ?: return
        val isOwnProfile = arguments?.getBoolean("isOwnProfile") ?: false

        view.findViewById<TextView>(R.id.followListTitle).text = mode
        view.findViewById<ImageButton>(R.id.followListCloseButton).setOnClickListener { dismiss() }

        val recyclerView = view.findViewById<RecyclerView>(R.id.followListRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val adapter = FollowListAdapter(
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
            onActionClick = { user, _ ->
                viewModel.removeUser(user, mode)
            }
        )

        recyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.users.collect { users ->
                adapter.updateData(users)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadList(userId, mode)
    }


    /**
     * Tells us what to do when the dialgo is dismissed
     */
    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        parentFragmentManager.setFragmentResult("follow_list_dismissed", Bundle())
    }


    /**
     * Starts the dialog
     */
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}