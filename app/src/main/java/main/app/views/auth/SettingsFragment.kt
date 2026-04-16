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
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import main.app.MainActivity
import main.app.ViewModel.SettingsViewModel
import main.app.apiConnections.HttpRoutes
import main.app.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    private var originalUsername: String = ""
    private var originalEmail: String = ""
    private var originalBio: String = ""
    private var selectedImageUri: Uri? = null


    /**
     * 
     */
    private val pickProfileImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            selectedImageUri = uri
            binding.settingsProfileImage.setImageURI(uri)
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

        viewModel.loadUser()

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            originalUsername = user.username ?: ""
            originalEmail = user.email ?: ""
            originalBio = user.bio ?: ""
            binding.settingsUsernameInput.setText(originalUsername)
            binding.settingsEmailInput.setText(originalEmail)
            binding.settingsBioInput.setText(originalBio)

            if (user.imageId != null && user.imageId != 0) {
                Glide.with(this)
                    .load("${HttpRoutes.GET_IMAGE}/${user.imageId}")
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(binding.settingsProfileImage)
            }
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { success ->
            success ?: return@observe
            viewModel.clearUpdateResult()
            if (success) {
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            } else {
                Toast.makeText(requireContext(), "Update failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.logoutComplete.observe(viewLifecycleOwner) { done ->
            done ?: return@observe
            viewModel.clearLogoutComplete()
            navigateToLogin()
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            success ?: return@observe
            viewModel.clearDeleteResult()
            if (success) {
                navigateToLogin()
            } else {
                Toast.makeText(requireContext(), "Failed to delete account.", Toast.LENGTH_SHORT).show()
            }
        }

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
            viewModel.logout()
        }

        binding.deleteAccountButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This action cannot be undone.")
                .setPositiveButton("Delete") { _, _ -> viewModel.deleteAccount() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun saveSettings() {
        val username = binding.settingsUsernameInput.text.toString().trim()
        val email = binding.settingsEmailInput.text.toString().trim()
        val password = binding.settingsPasswordInput.text.toString()
        val bio = binding.settingsBioInput.text.toString().trim()

        val textChanged = username != originalUsername ||
                email != originalEmail ||
                bio != originalBio ||
                password.isNotEmpty()
        val pictureChanged = selectedImageUri != null

        if (!textChanged && !pictureChanged) {
            Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
            return
        }

        if (pictureChanged) {
            val uri = selectedImageUri!!
            val imageBytes = requireContext().contentResolver
                .openInputStream(uri)?.use { it.readBytes() }
            val mimeType = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            if (imageBytes != null) {
                viewModel.updateProfilePicture(imageBytes, mimeType)
            }
        }

        if (textChanged) {
            viewModel.saveSettings(username, email, password, bio)
        }
    }

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