package main.app.views.auth

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import main.app.R
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import main.app.ViewModel.NotificationViewModel


class AuthActivity : AppCompatActivity(){

    /**
     * Called when the activity is created, sets upp the bottom
     * navigation and observes the notifications count to show
     * the notification badge or not
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottomNavigationView)

        if(savedInstanceState == null) {
            setCurrentFragment(HomePage())
        }

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home -> setCurrentFragment(HomePage())
                R.id.profile -> setCurrentFragment(ProfileFragment())
                R.id.search -> setCurrentFragment(SearchFragment())
                R.id.create -> setCurrentFragment(CreatePostFragment())
                R.id.notifications -> setCurrentFragment(NotificationFragment())
            }
            true
        }

        val notificationViewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
        notificationViewModel.loadUnreadCount()

        lifecycleScope.launch {
            notificationViewModel.unreadCount.collect { count ->
                val badge = bottomNavigationView.getOrCreateBadge(R.id.notifications)
                if (count > 0) {
                    badge.isVisible = true
                    badge.number = count
                } else {
                    badge.isVisible = false
                }
            }
        }
    }


    /**
     * Replaces the current fragment in the main container with the given fragment
     * used to switch between fragments that the bottom navigation selects
     *
     * @param fragment the fragment to dispaly inn the main container
     */
    private fun setCurrentFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .commit()
    }

}