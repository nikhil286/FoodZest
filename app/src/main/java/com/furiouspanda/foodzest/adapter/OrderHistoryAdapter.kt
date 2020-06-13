package com.furiouspanda.foodzest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.model.CartItems
import com.furiouspanda.foodzest.model.OrderHistoryRestaurant
import com.furiouspanda.foodzest.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class OrderHistoryAdapter(
    val context: Context,
    val orderedRestaurantList: ArrayList<OrderHistoryRestaurant>
) : RecyclerView.Adapter<OrderHistoryAdapter.ViewHolderOrderHistoryRestaurant>() {

    class ViewHolderOrderHistoryRestaurant(view: View) : RecyclerView.ViewHolder(view) {
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val recyclerViewItemsOrdered: RecyclerView = view.findViewById(R.id.recyclerViewItemsOrdered)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolderOrderHistoryRestaurant {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.order_history_recycler_view_single_row, parent, false)

        return ViewHolderOrderHistoryRestaurant(view)
    }

    override fun getItemCount(): Int {
        return orderedRestaurantList.size
    }

    override fun onBindViewHolder(holder: ViewHolderOrderHistoryRestaurant, position: Int) {

        val restaurantObject = orderedRestaurantList[position]
        holder.txtRestaurantName.text = restaurantObject.restaurantName
        var formatDate = restaurantObject.orderPlacedAt
        formatDate = formatDate.replace("-", "/")
        formatDate = formatDate.substring(0, 6) + "20" + formatDate.substring(6, 8)
        holder.txtDate.text = formatDate

        val layoutManager = LinearLayoutManager(context)
        var orderedItemAdapter: CartAdapter

        if (ConnectionManager().checkConnectivity(context)) {

            try {
                val orderItemsPerRestaurant = ArrayList<CartItems>()
                val sharedPreferences = context.getSharedPreferences(
                    context.getString(R.string.shared_preferences),
                    Context.MODE_PRIVATE
                )

                val userId = sharedPreferences.getString("user_id", "0")
                val queue = Volley.newRequestQueue(context)
                val url = "http://" + context.getString(R.string.ip_address) + "/v2/orders/fetch_result/$userId"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        println(it)
                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")
                        if (success) {

                            val data = response.getJSONArray("data")
                            val restaurantObjectRetrieved = data.getJSONObject(position)
                            orderItemsPerRestaurant.clear()
                            val foodOrdered = restaurantObjectRetrieved.getJSONArray("food_items")

                            for (j in 0 until foodOrdered.length())
                            {
                                val eachFoodItem = foodOrdered.getJSONObject(j)
                                val itemObject = CartItems(
                                    eachFoodItem.getString("food_item_id"),
                                    eachFoodItem.getString("name"),
                                    eachFoodItem.getString("cost"), "000"
                                )

                                orderItemsPerRestaurant.add(itemObject)
                            }

                            orderedItemAdapter = CartAdapter(context, orderItemsPerRestaurant)
                            holder.recyclerViewItemsOrdered.adapter = orderedItemAdapter
                            holder.recyclerViewItemsOrdered.layoutManager = layoutManager
                        }
                    },
                    Response.ErrorListener {
                        println("Error12menu is $it")

                        Toast.makeText(
                            context,
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
                    context,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}