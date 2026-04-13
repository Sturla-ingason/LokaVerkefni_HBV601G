package main.app.views.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import main.app.MainActivity
import main.app.R
import main.app.ViewModel.SettingsViewModel
import main.app.databinding.FragmentSettingsBinding

/**
 * Settings fragment that allows the user to edit their profile,
 * log out, or delete their account.
 */
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

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

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            binding.settingsUsernameInput.setText(user.username ?: "")
            binding.settingsEmailInput.setText(user.email ?: "")
            binding.settingsBioInput.setText(user.bio ?: "")
        }

        viewModel.loadError.observe(viewLifecycleOwner) { error ->
            error ?: return@observe
            viewModel.clearLoadError()
            Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show()
                navigateToLogin()
            } else {
                Toast.makeText(requireContext(), "Delete failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadUser()

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.saveSettingsButton.setOnClickListener {
            val username = binding.settingsUsernameInput.text.toString().trim()
            val email = binding.settingsEmailInput.text.toString().trim()
            val password = binding.settingsPasswordInput.text.toString().trim()
            val bio = binding.settingsBioInput.text.toString().trim()

            if (username.isEmpty() || email.isEmpty()) {
                Toast.makeText(requireContext(), "Username and email cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveSettings(username, email, password, bio)
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