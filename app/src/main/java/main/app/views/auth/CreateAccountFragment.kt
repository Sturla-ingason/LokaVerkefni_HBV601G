package main.app.views.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import main.app.R
import main.app.databinding.FragmentCreateAccountBinding
import main.app.views.auth.LoginFragment

class CreateAccountFragment : Fragment(){

    private var _binding: FragmentCreateAccountBinding? = null
    private val binding get() = _binding!!


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

    }


    /**
     * what to do when we close the view
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}