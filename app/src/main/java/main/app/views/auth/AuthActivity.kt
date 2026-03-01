package main.app.views.auth

import android.os.Bundle
import main.app.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        setCurrentFragment(HomePage())

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home    -> setCurrentFragment(HomePage())
                R.id.search  -> setCurrentFragment(SearchFragment())
                R.id.camera  -> setCurrentFragment(CameraFragment())  // US #2
                R.id.profile -> setCurrentFragment(ProfileFragment())
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}