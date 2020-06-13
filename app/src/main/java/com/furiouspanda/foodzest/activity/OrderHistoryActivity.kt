package com.furiouspanda.foodzest.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.adapter.OrderHistoryAdapter
import com.furiouspanda.foodzest.model.OrderHistoryRestaurant
import com.furiouspanda.foodzest.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException


class OrderHistoryActivity : AppCompatActivity() {

    lateinit var layoutManager1: RecyclerView.LayoutManager
    lateinit var menuAdapter1: OrderHistoryAdapter
    lateinit var recyclerViewAllOrders: RecyclerView
    lateinit var toolBar: androidx.appcompat.widget.Toolbar
    lateinit var orderHistoryLayout: RelativeLayout
    lateinit var noOrders: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerViewAllOrders = findViewById(R.id.recyclerViewAllOrders)
        toolBar = findViewById(R.id.toolBar)
        orderHistoryLayout = findViewById(R.id.orderHistoryLayout)
        noOrders = findViewById(R.id.noOrders)

        setToolBar()
    }

    fun setItemsForEachRestaurant() {

        layoutManager1 = LinearLayoutManager(this)
        val orderedRestaurantList = ArrayList<OrderHistoryRestaurant>()
        val sharedPreferences = this.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        val userId = sharedPreferences.getString("user_id", "000")
        if (ConnectionManager().checkConnectivity(this)) {

            orderHistoryLayout.visibility = View.VISIBLE

            try {
                val queue = Volley.newRequestQueue(this)
                val url =
                    "http://" + getString(R.string.ip_address) + "/v2/orders/fetch_result/$userId"
                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")

                        if (success) {
                            val data = response.getJSONArray("data")
                            if (data.length() == 0) {

                                Toast.makeText(
                                    this,
                                    "No Orders Placed yet!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                noOrders.visibility = View.VISIBLE

                            } else {
                                noOrders.visibility = View.INVISIBLE

                                for (i in 0 until data.length()) {
                                    val restaurantItem = data.getJSONObject(i)
                                    val restaurantObject = OrderHistoryRestaurant(
                                        restaurantItem.getString("order_id"),
                                        restaurantItem.getString("restaurant_name"),
                                        restaurantItem.getString("total_cost"),
                                        restaurantItem.getString("order_placed_at").substring(0, 10)
                                    )

                                    orderedRestaurantList.add(restaurantObject)
                                    menuAdapter1 = OrderHistoryAdapter(this, orderedRestaurantList)
                                    recyclerViewAllOrders.adapter = menuAdapter1
                                    recyclerViewAllOrders.layoutManager = layoutManager1
                                }
                            }
                        }
                        orderHistoryLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {
                        orderHistoryLayout.visibility = View.INVISIBLE

                        Toast.makeText(
                            this,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        val headers = HashMap<String, String>()
                        headers["Content-type"] = "application/json"
                        headers["token"] = "26c5144c5b9c13"
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
        supportActionBar?.title = "My Previous Orders"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(this)) {
            setItemsForEachRestaurant()
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
