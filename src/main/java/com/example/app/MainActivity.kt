package com.example.app

import android.os.Bundle
import android.view.Menu
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // AppBarConfiguration helps manage behavior of the Navigation UI components
    private lateinit var appBarConfiguration: AppBarConfiguration

    // ViewBinding instance to access views from activity_main.xml more safely and easily
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the view using ViewBinding instead of using findViewById
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the custom toolbar (app bar) defined in the XML layout as the ActionBar
        setSupportActionBar(binding.appBarMain.toolbar)

        // Set a click listener on the FloatingActionButton to show a Snackbar message
        binding.appBarMain.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null) // Action is optional and currently does nothing
                .show()
        }

        // Get references to the DrawerLayout and NavigationView from the binding
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        // Set up the NavController for managing navigation within the app using the Navigation Component
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Define top-level destinations (they won't show the "up" arrow, just the hamburger menu)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,       // Represents the Home destination
                R.id.nav_gallery,    // Represents the Gallery destination
                R.id.nav_slideshow   // Represents the Slideshow destination
            ),
            drawerLayout           // Connect DrawerLayout with Navigation Controller
        )

        // Link the ActionBar (Toolbar) with the NavController using the AppBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Connect the NavigationView (side menu) with the NavController
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; adds items to the app bar if present
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        // Handle navigation when the up button (←) is pressed in the toolbar
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Either navigate up in the nav stack or fall back to default behavior
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
