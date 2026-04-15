package main.app.views.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.ProfileViewModel
import main.app.adapters.Adapter
import main.app.apiConnections.HttpRoutes
import main.app.databinding.FragmentProfileBinding


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var adapter: Adapter
    private var targetUserId: Int? = null

    companion object {
        fun newInstance(userId: Int? = null): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            if (userId != null) {
                args.putInt("userId", userId)
            }
            fragment.arguments = args
            return fragment
        }
    }


    /**
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }


    /**
     *
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.init(requireContext())

        val passedUserId = arguments?.getInt("userId")
        targetUserId = if (passedUserId == null || passedUserId == -1) null else passedUserId

        adapter = Adapter(
            emptyList(),
            viewLifecycleOwner.lifecycleScope,
            childFragmentManager,
            onLikeToggle = { postId -> viewModel.toggleLike(postId) }
        )
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleViewProfile)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Observe posts
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.posts.collect { posts ->
                adapter.updateData(posts)
            }
        }

        // Observe profile data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.user.collect { user ->
                user ?: return@collect
                binding.username.text = user.username
                binding.followersCount.text = (user.followerCount ?: 0).toString()
                binding.followingCount.text = (user.followingCount ?: 0).toString()
                if (!user.bio.isNullOrBlank()) {
                    binding.bio.text = user.bio
                    binding.bio.visibility = View.VISIBLE
                }
                user.userID?.let { uid ->
                    val isOwnProfile = targetUserId == null
                    binding.followersCount.setOnClickListener {
                        FollowListFragment.newInstance(uid, FollowListFragment.MODE_FOLLOWERS, isOwnProfile)
                            .show(parentFragmentManager, "FollowListFragment")
                    }
                    binding.followingCount.setOnClickListener {
                        FollowListFragment.newInstance(uid, FollowListFragment.MODE_FOLLOWING, isOwnProfile)
                            .show(parentFragmentManager, "FollowListFragment")
                    }
                }

                if (user.imageId != null) {
                    val imageUrl = "${HttpRoutes.GET_IMAGE}/${user.imageId}"
                    Glide.with(this@ProfileFragment)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.profileImage)
                } else {
                    binding.profileImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            }
        }

        childFragmentManager.setFragmentResultListener("post_detail_dismissed", viewLifecycleOwner) { _, _ ->
            viewModel.loadPosts(targetUserId)
        }

        parentFragmentManager.setFragmentResultListener("follow_list_dismissed", viewLifecycleOwner) { _, _ ->
            viewModel.loadProfileData(targetUserId)
        }

        // Observe errors
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.error.collect { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }

        // Show offline stored data if using cached data
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isOffline.collect { offline ->
                if (offline) Toast.makeText(context, "You are offline — showing cached profile", Toast.LENGTH_LONG).show()
            }
        }

        // Navigate to settings page when gear icon is tapped
        binding.settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Show/hide buttons depending on own vs other profile
        if (targetUserId != null) {
            binding.settingsButton.visibility = View.GONE
            binding.backButton.visibility = View.VISIBLE
            binding.followButton.visibility = View.VISIBLE
            binding.blockButton.visibility = View.VISIBLE

            binding.backButton.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            binding.followButton.setOnClickListener {
                viewModel.toggleFollow(targetUserId!!)
            }

            binding.blockButton.setOnClickListener {
                val isBlocked = viewModel.isBlocked.value
                val action = if (isBlocked) "Unblock" else "Block"
                AlertDialog.Builder(requireContext())
                    .setTitle("$action user")
                    .setMessage("Are you sure you want to $action this user?")
                    .setPositiveButton(action) { _, _ -> viewModel.toggleBlock(targetUserId!!) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Only observe follow/block state when viewing another user's profile
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isFollowing.collect { isFollowing ->
                    binding.followButton.text = if (isFollowing) "Unfollow" else "Follow"
                }
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.isBlocked.collect { isBlocked ->
                    binding.blockButton.text = if (isBlocked) "Unblock" else "Block"
                    binding.followButton.visibility = if (isBlocked) View.GONE else View.VISIBLE
                }
            }

            viewModel.loadFollowBlockState(targetUserId!!)
        }

        viewModel.loadProfileData(targetUserId)
    }

    fun refreshPosts() {
        viewModel.loadPosts(targetUserId)
    }

    override fun onResume() {
        super.onResume()
        // Reload posts every time the fragment becomes visible so new posts appear immediately
        viewModel.loadPosts(targetUserId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}