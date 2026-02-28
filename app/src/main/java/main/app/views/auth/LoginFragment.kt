package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import main.app.R
import main.app.databinding.FragmentLoginnBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginnBinding? = null
    private val binding get() = _binding!!


    /**
     * Creates the view for loginn fragment
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        saveInstanceBundle: Bundle?
    ): View {

        _binding = FragmentLoginnBinding.inflate(inflater, container, false)
        return binding.root

    }


    /**
     * Adds event listeners for the buttons
     * allows us to switch to another fragment
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.dontHaveAccountButton.setOnClickListener{
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateAccountFragment())
                .addToBackStack(null)
                .commit()
        }

    }


    /**
     * Destroys the fragment for loginn
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}