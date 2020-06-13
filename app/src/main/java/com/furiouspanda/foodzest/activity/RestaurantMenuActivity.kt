package com.furiouspanda.foodzest.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.adapter.RestaurantMenuAdapter
import com.furiouspanda.foodzest.model.RestaurantMenu
import com.furiouspanda.foodzest.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import kotlin.collections.HashMap

class RestaurantMenuActivity : AppCompatActivity() {

    lateinit var toolBar: androidx.appcompat.widget.Toolbar
    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var menuAdapter: RestaurantMenuAdapter
    lateinit var restaurantId: String
    lateinit var restaurantName: String
    lateinit var proceedToCartLayout: RelativeLayout
    lateinit var btnProceedToCart: Button
    lateinit var menuProgressLayout: RelativeLayout
    var restaurantMenuList = arrayListOf<RestaurantMenu>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurant_menu)

        proceedToCartLayout = findViewById(R.id.relativeLayoutProceedToCart)
        btnProceedToCart = findViewById(R.id.btnProceedToCart)
        menuProgressLayout = findViewById(R.id.menuProgressLayout)
        toolBar = findViewById(R.id.toolBar)

        restaurantId = intent.getStringExtra("restaurantId")!!
        restaurantName = intent.getStringExtra("restaurantName")!!
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerViewRestaurantMenu)

        setToolBar()
    }

    fun fetchData() {
        if (ConnectionManager().checkConnectivity(this)) {

            menuProgressLayout.visibility = View.VISIBLE

            try {
                //send volley request to the given url along with restaurantId to fetch restaurantMenu
                val queue = Volley.newRequestQueue(this)
                val url = "http://" + getString(R.string.ip_address) + "/v2/restaurants/fetch_result/$restaurantId"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {
                        println("Response12menu is $it")
                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")

                        if (success) {
                            restaurantMenuList.clear()
                            val data = response.getJSONArray("data")

                            for (i in 0 until data.length()) {
                                val restaurant = data.getJSONObject(i)
                                val menuObject = RestaurantMenu(
                                    restaurant.getString("id"),
                                    restaurant.getString("name"),
                                    restaurant.getString("cost_for_one")
                                )

                                restaurantMenuList.add(menuObject)
                                menuAdapter = RestaurantMenuAdapter(
                                    this,
                                    restaurantId,
                                    restaurantName,
                                    proceedToCartLayout,
                                    btnProceedToCart,
                                    restaurantMenuList
                                )

                                recyclerView.adapter = menuAdapter
                                recyclerView.layoutManager = layoutManager
                            }
                        }
                        menuProgressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        println("Error12menu is $it")

                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                        menuProgressLayout.visibility = View.INVISIBLE
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = getString(R.string.token)
                        return headers
                    }
                }
                queue.add(jsonObjectRequest)

            } catch (e: JSONException) {
                Toast.makeText(
                    this,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                finishAffinity()
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }


    fun setToolBar() {
        setSupportActionBar(toolBar)
        supportActionBar?.title = restaurantName
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
    }


    //Once items added to cart, If user press back , items will be cleared
    override fun onBackPressed() {
        if (menuAdapter.getSelectedItemCount() > 0) {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("Alert!")
            alterDialog.setMessage("Going back will remove everything from cart")
            alterDialog.setPositiveButton("Okay") { _, _ ->
                super.onBackPressed()
            }
            alterDialog.setNegativeButton("Cancel") { _, _ ->

            }
            alterDialog.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                if (menuAdapter.getSelectedItemCount() > 0) {
                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
                    alterDialog.setTitle("Alert!")
                    alterDialog.setMessage("Going back will remove everything from cart")
                    alterDialog.setPositiveButton("Okay") { _, _ ->
                        super.onBackPressed()
                    }
                    alterDialog.setNegativeButton("Cancel") { _, _ ->

                    }
                    alterDialog.show()
                } else {
                    super.onBackPressed()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(this)) {
            if (restaurantMenuList.isEmpty())
                fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                finishAffinity()
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }
}
