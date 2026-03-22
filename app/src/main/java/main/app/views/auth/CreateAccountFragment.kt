package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import main.app.R
import main.app.databinding.FragmentCreateAccountBinding
import main.app.repository.AuthRepository
import main.app.views.auth.LoginFragment

class CreateAccountFragment : Fragment(){

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AuthRepository()


    /**
     * Creates the view for CreateAccount fragment with binding
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    /**
     * Allows us to have event listeners when we create the view
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.alreadyHaveAccount.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoginFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.createAccountButton.setOnClickListener{
            val email = binding.EmailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val username = binding.usernameInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val success = authRepository.signup(email, username, password)
                if (success) {
                    Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, LoginFragment())
                        .commit()
                } else {
                    Toast.makeText(requireContext(), "Signup failed. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    }


    /**
     * what to do when we close the view
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
