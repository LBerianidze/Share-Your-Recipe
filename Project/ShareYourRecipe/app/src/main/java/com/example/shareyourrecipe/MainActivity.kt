package com.example.shareyourrecipe

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.shareyourrecipe.databinding.ActivityMainBinding
import com.example.shareyourrecipe.model.Category
import com.example.shareyourrecipe.model.User
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    val mutableDict = mutableMapOf(-1 to "პოპულარული", -2 to "ჩემი რეცეპტები")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        binding.appBarMain.fab.setOnClickListener { view ->
            val navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_content_main)
            navController.navigate(R.id.nav_recipeadd)

        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
        setUpMenu(navView)
        setUpUserProfileHeader()

    }

    private fun setUpUserProfileHeader() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        binding.navView.getHeaderView(0).findViewById<Button>(R.id.nav_header_profile_login)
            .setOnClickListener {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        if (uid == null) {
            return;
        }
        val usersRef = FirebaseDatabase.getInstance().getReference("users")
        val userRef = usersRef.child(uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userModel = dataSnapshot.getValue(User::class.java)
                if (userModel != null) {
                    val textView: TextView = binding.navView.getHeaderView(0)
                        .findViewById<TextView>(R.id.nav_header_profile_user_name);
                    textView.setText(userModel.firstName + " " + userModel.lastName)
                    val emailTextView: TextView = binding.navView.getHeaderView(0)
                        .findViewById<TextView>(R.id.nav_header_profile_email);
                    emailTextView.setText(currentUser.email)

                    binding.navView.getHeaderView(0)
                        .findViewById<LinearLayout>(R.id.nav_header_profile_data_layout).visibility =
                        View.VISIBLE;
                    binding.navView.getHeaderView(0)
                        .findViewById<Button>(R.id.nav_header_profile_login).visibility = View.GONE;

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occurred during the data retrieval
            }
        })

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var handled = false;
        val bundle = Bundle()
        val toolBarText: String
        when (item.itemId) {
            R.id.nav_home -> {
                bundle.putInt("categoryId", -1)

                toolBarText = "პოპულარული";
            }

            R.id.nav_my_recipes -> {
                bundle.putInt("categoryId", -1)
                bundle.putBoolean("currentUser", true)

                toolBarText = "ჩემი რეცეპტები";
            }

            else -> {
                bundle.putInt("categoryId", item.itemId - 79000)

                toolBarText = mutableDict[item.itemId - 79000]!!;
            }
        }

        val currentDestination = navController.currentDestination
        val currentDestinationId = currentDestination?.id

        if (currentDestinationId != null) {
            navController.navigate(currentDestinationId, bundle)
        }

        val menu = binding.navView.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            menuItem.isChecked = false
        }

        val submenu: SubMenu = binding.navView.menu.findItem(R.id.categories).subMenu!!
        for (i in 0 until submenu.size()) {
            val menuItem = submenu.getItem(i)
            menuItem.isChecked = false
        }
        item.isChecked = true
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        setToolbarHeaderTitle(toolBarText)
        return handled
    }

    private fun setUpMenu(navView: NavigationView) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            binding.navView.menu.findItem(R.id.nav_my_recipes).isVisible = false
        }
        val categoriesMenu: SubMenu? = navView.menu.findItem(R.id.categories).subMenu;

        val categoriesDbReference: DatabaseReference =
            FirebaseDatabase.getInstance().getReference("categories")
        val categoriesListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (categorySnapshot in dataSnapshot.children) {
                    val category = categorySnapshot.getValue(Category::class.java)
                    categoriesMenu!!.add(
                        Menu.NONE,
                        79000 + category!!.id,
                        Menu.NONE,
                        category!!.name
                    )
                    mutableDict[category.id] = category.name;
                }
                navView.invalidate()
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        categoriesDbReference.addValueEventListener(categoriesListener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun setToolbarHeaderTitle(title: String) {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = title
    }
}