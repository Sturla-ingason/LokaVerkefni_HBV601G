package main.app.views.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import main.app.MainActivity
import main.app.apiConnections.HttpRoutes
import main.app.databinding.FragmentSettingsBinding
import main.app.repository.AuthRepository
import main.app.repository.UserRepository

/**
 * Settings fragment that allows the user to edit their profile,
 * log out, or delete their account.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()

    // Stores the current user ID so we can check it on delete
    private var currentUserId: Int? = null


    private var originalUsername: String = ""
    private var originalEmail: String = ""
    private var originalBio: String = ""
    private var selectedImageUri: Uri? = null


    private val pickProfileImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            selectedImageUri = uri
            binding.settingsProfileImage.setImageURI(uri)
            binding.settingsPasswordInput.setText("")
        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.changeProfilePictureButton.setOnClickListener {
            pickProfileImage.launch("image/*")
        }

        binding.saveSettingsButton.setOnClickListener {
            saveSettings()
        }

        binding.logoutButton.setOnClickListener {
            logout()
        }

        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }
    }

    /**
     * Fetches current user data and populates the form fields.
     */
    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = userRepository.getUser()
                currentUserId = user.userID

                originalUsername = user.username ?: ""
                originalEmail = user.email ?: ""
                originalBio = user.bio ?: ""

                binding.settingsUsernameInput.setText(originalUsername)
                binding.settingsEmailInput.setText(originalEmail)
                binding.settingsBioInput.setText(originalBio)
                binding.settingsPasswordInput.setText("")

                if (user.imageId != null && user.imageId != 0) {
                    val imageUrl = "${HttpRoutes.GET_IMAGE}/${user.imageId}"
                    Glide.with(this@SettingsFragment)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(binding.settingsProfileImage)
                } else {
                    binding.settingsProfileImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Validates input and sends updated profile to the API.
     * Password is only sent if the user typed a new one.
     */
    private fun saveSettings() {
        val username = binding.settingsUsernameInput.text.toString().trim()
        val email = binding.settingsEmailInput.text.toString().trim()
        val password = binding.settingsPasswordInput.text.toString().trim()
        val bio = binding.settingsBioInput.text.toString().trim()

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Username and email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        val textChanged =
            username != originalUsername ||
                    email != originalEmail ||
                    bio != originalBio ||
                    password.isNotEmpty()

        val pictureChanged = selectedImageUri != null

        if (!textChanged && !pictureChanged) {
            Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                var profileSuccess = true
                var pictureSuccess = true

                if (textChanged) {
                    profileSuccess = userRepository.updateUser(
                        username = username,
                        email = email,
                        password = password,
                        bio = bio
                    )
                }

                if (pictureChanged) {
                    val uri = selectedImageUri!!
                    val imageBytes = requireContext().contentResolver
                        .openInputStream(uri)
                        ?.use { it.readBytes() }

                    val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"

                    pictureSuccess = if (imageBytes != null) {
                        userRepository.updateProfilePicture(imageBytes, mimeType)
                    } else {
                        false
                    }
                }

                when {
                    !profileSuccess -> {
                        Toast.makeText(requireContext(), "Profile text update failed", Toast.LENGTH_SHORT).show()
                    }
                    !pictureSuccess -> {
                        Toast.makeText(requireContext(), "Profile picture update failed", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()

                        originalUsername = username
                        originalEmail = email
                        originalBio = bio

                        binding.settingsPasswordInput.setText("")
                        selectedImageUri = null

                        parentFragmentManager.popBackStack()
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Logs the user out and navigates back to the login screen.
     */
    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) { }
            navigateToLogin()
        }
    }

    /**
     * Deletes the account after a confirmation dialog.
     * Blocks deletion for the protected test accounts by ID (alice=3, bob=4, carol=5, dave=6).
     */
    private fun deleteAccount() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val success = userRepository.deleteUser()
                        if (success) {
                            Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                            navigateToLogin()
                        } else {
                            Toast.makeText(requireContext(), "Delete failed. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Delete failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Navigates back to the login screen (MainActivity) and finishes AuthActivity.
     */
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}