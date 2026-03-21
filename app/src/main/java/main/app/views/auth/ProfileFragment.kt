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

        adapter = Adapter(emptyList())
        val recyclerView: RecyclerView = view.findViewById(R.id.recycleViewProfile)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        fetchPosts()
        fetchProfileData()

    }

    /**
     *
     */
    private fun fetchPosts() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val posts = postRepository.getPosts()
                adapter.updateData(posts)
            } catch (e: Exception) {
                // Handle error (e.g., show a Toast or an error message in the UI)
                e.printStackTrace()
            }
        }
    }

    private fun fetchProfileData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val followerCount = userRepository.getFollowerCount()
                val followingCount = userRepository.getFollowingCount()
                binding.followersCount.text = followerCount.toString()
                binding.followingCount.text = followingCount.toString()

                val user = userRepository.getUser()
                binding.username.text = user.username
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}