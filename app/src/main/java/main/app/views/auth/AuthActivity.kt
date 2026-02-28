package main.app.views.auth

import android.os.Bundle
import main.app.R
import androidx.appcompat.app.AppCompatActivity


class AuthActivity : AppCompatActivity(){

    /**
     * Creates the fragment container so we can have fragments inn them
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, LoginFragment())
                .commit()
        }
    }

}