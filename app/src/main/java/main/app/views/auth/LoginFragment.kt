package main.app.views.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import main.app.R
import main.app.databinding.FragmentLoginnBinding
import main.app.serviceModel.AuthModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginnBinding? = null
    private val binding get() = _binding!!

    private val authModel = AuthModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginnBinding.inflate(inflater, container, false)
        return binding.root
    }

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

            // Show loading or disable button
            binding.LogInnButton.isEnabled = false

            viewLifecycleOwner.lifecycleScope.launch {
                val success = authModel.logInn(email, password)
                
                if (success) {
                    val intent = Intent(requireContext(), AuthActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    Toast.makeText(requireContext(), "Login failed. Please check your credentials.", Toast.LENGTH_SHORT).show()
                    binding.LogInnButton.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
