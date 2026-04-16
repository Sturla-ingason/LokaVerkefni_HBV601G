package main.app.views.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import main.app.R
import main.app.ViewModel.AuthState
import main.app.ViewModel.AuthViewModel
import main.app.databinding.FragmentLoginnBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginnBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()


    /**
     *
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginnBinding.inflate(inflater, container, false)
        return binding.root
    }


    /**
     * What to do when the view is created
     * sets the event handler for dontHaveAccountButton and LogInnButton
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dontHaveAccountButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateAccountFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.LogInnButton.setOnClickListener {
            val email = binding.EmailInput.text.toString()
            val password = binding.passwordInput.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            authViewModel.login(email, password)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.loginState.collect { state ->
                when (state) {
                    is AuthState.Loading -> binding.LogInnButton.isEnabled = false
                    is AuthState.Success -> {
                        val intent = Intent(requireContext(), AuthActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                    is AuthState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        binding.LogInnButton.isEnabled = true
                    }
                    is AuthState.Idle -> binding.LogInnButton.isEnabled = true
                }
            }
        }
    }


    /**
     * What to do when the view is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}