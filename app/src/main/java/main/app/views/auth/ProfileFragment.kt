package main.app.views.auth

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import main.app.R
import main.app.adapters.Adapter
import main.app.databinding.FragmentProfileBinding
import main.app.repository.PostRepository
import main.app.repository.UserRepository


/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val postRepository = PostRepository()
    private val userRepository = UserRepository()
    private lateinit var adapter: Adapter

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

        adapter = Adapter(emptyList(), viewLifecycleOwner.lifecycleScope, childFragmentManager)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleViewProfile)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        val userId = arguments?.getInt("userId", -1).takeIf { it != -1 }

        fetchPosts(userId)
        fetchProfileData(userId)

        // Navigate to settings page when gear icon is tapped
        binding.settingsButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, SettingsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Hide settings button if viewing another user's profile
        if (userId != null) {
            binding.settingsButton.visibility = View.GONE
        }

    }

    /**
     *
     */
    private fun fetchPosts(userId: Int?) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // If we have a userId, we might need a different repo method, 
                // but for now we'll stick to the current logic or assume getPostByUser handles it
                val posts = postRepository.getPostByUser() 
                adapter.updateData(posts)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun fetchProfileData(userId: Int?) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // If userId is provided, we should fetch that specific user's data
                // This requires updating UserRepository to accept a userId
                val user = if (userId != null) {
                    // Placeholder for fetching specific user profile
                    userRepository.getUser() 
                } else {
                    userRepository.getUser()
                }
                
                binding.username.text = user.username
                // Update follower/following logic if needed
                binding.followersCount.text = (user.followerCount ?: 0).toString()
                binding.followingCount.text = (user.followingCount ?: 0).toString()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}