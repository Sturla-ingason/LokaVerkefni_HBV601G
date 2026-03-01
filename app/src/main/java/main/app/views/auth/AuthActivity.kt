package main.app.views.auth

import android.os.Bundle
import main.app.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class AuthActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        val homePageFragment = HomePage()
        val searchFragment = SearchFragment()
        val profileFragment = ProfileFragment()
        val createPostFragment = CreatePostFragment()

        setCurrentFragment(homePageFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(homePageFragment)
                R.id.profile -> setCurrentFragment(profileFragment)
                R.id.search -> setCurrentFragment(searchFragment)
                R.id.create -> setCurrentFragment(createPostFragment)
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