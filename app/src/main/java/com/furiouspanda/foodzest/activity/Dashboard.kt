package com.furiouspanda.foodzest.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.fragment.*
import com.google.android.material.navigation.NavigationView

class Dashboard : AppCompatActivity() {

    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: androidx.appcompat.widget.Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var drawerLayout: DrawerLayout
    lateinit var txtUser: TextView
    lateinit var txtNumber: TextView
    lateinit var sharedPreferences: SharedPreferences
    var previousMenuItemSelected: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sharedPreferences = getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        toolbar = findViewById(R.id.toolBar)
        frameLayout = findViewById(R.id.frameLayout)
        navigationView = findViewById(R.id.navigationView)
        drawerLayout = findViewById(R.id.drawerLayout)
        val headerView = navigationView.getHeaderView(0)
        txtUser = headerView.findViewById(R.id.txtUser)
        txtNumber = headerView.findViewById(R.id.txtNumber)

        navigationView.menu.getItem(0).isCheckable = true
        navigationView.menu.getItem(0).isChecked = true

        setToolBar()

        txtUser.text = sharedPreferences.getString("name", "UserName")
        txtNumber.text = "+91- ${sharedPreferences.getString("mobile_number", "9999999999")}"

        //Hamburger icon setup for navigation drawer
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@Dashboard,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        navigationView.setNavigationItemSelectedListener {

            if (previousMenuItemSelected != null) {
                previousMenuItemSelected?.isChecked = false
            }

            previousMenuItemSelected = it
            it.isCheckable = true
            it.isChecked = true


            when (it.itemId) {
                R.id.homee -> {
                    openDashboard()
                    drawerLayout.closeDrawers()
                }
                R.id.myProfile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayout,
                            ProfileFragment(this)
                        ).commit()

                    supportActionBar?.title = "My Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.favoriteRestaurants -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayout,
                            FavouriteRestaurantFragment(this)
                        ).commit()

                    supportActionBar?.title = "Favorites"
                    drawerLayout.closeDrawers()
                }
                R.id.orderHistory -> {
                    val intent = Intent(this, OrderHistoryActivity::class.java)
                    drawerLayout.closeDrawers()
                    Toast.makeText(this@Dashboard, "Order History", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
                R.id.faqs -> {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frameLayout,
                            FaqsFragment()
                        ).commit()

                    supportActionBar?.title = "FAQs"
                    drawerLayout.closeDrawers()
                    Toast.makeText(this@Dashboard, "FAQs", Toast.LENGTH_SHORT).show()
                }
                R.id.logout -> {
                    drawerLayout.closeDrawers()

                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    alterDialog.setMessage("Do you wish to log out?")
                    alterDialog.setPositiveButton("Yes") { _, _ ->
                        sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()
                        ActivityCompat.finishAffinity(this)
                    }
                    alterDialog.setNegativeButton("No") { _, _ ->

                    }
                    alterDialog.create()
                    alterDialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }

        openDashboard()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is DashboardFragment -> {
                navigationView.menu.getItem(0).isChecked = true
                openDashboard()
            }
            else -> super.onBackPressed()
        }
    }

    //setup custom toolbar
    fun setToolBar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "All Restaurants"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
    }

    fun openDashboard() {
        supportFragmentManager.beginTransaction().replace(
            R.id.frameLayout,
            DashboardFragment(this)
        ).commit()

        supportActionBar?.title = "All Restaurants"
        navigationView.setCheckedItem(R.id.homee)
    }

    override fun onResume() {
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        )
        super.onResume()
    }
}
