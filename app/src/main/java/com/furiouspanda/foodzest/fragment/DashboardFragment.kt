package com.furiouspanda.foodzest.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.adapter.DashboardFragmentAdapter
import com.furiouspanda.foodzest.model.Restaurant
import com.furiouspanda.foodzest.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.sort_radio_button.view.*
import org.json.JSONException
import java.util.*
import kotlin.Comparator
import kotlin.collections.HashMap


class DashboardFragment(val contextParam: Context) : Fragment() {

    lateinit var recyclerView: RecyclerView
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var dashboardAdapter: DashboardFragmentAdapter
    lateinit var etSearch: EditText
    lateinit var radioButtonView: View
    lateinit var dashboardProgressLayout: RelativeLayout
    lateinit var cantFind: RelativeLayout

    var restaurantInfoList = arrayListOf<Restaurant>()

    //sort according to ratings
    var ratingComparator = Comparator<Restaurant> { rest1, rest2 ->

        if (rest1.restaurantRating.compareTo(rest2.restaurantRating, true) == 0) {
            rest1.restaurantName.compareTo(rest2.restaurantName, true)
        } else {
            rest1.restaurantRating.compareTo(rest2.restaurantRating, true)
        }
    }

    //sort according to cost(decreasing)
    var costComparator = Comparator<Restaurant> { rest1, rest2 ->

        rest1.cost_for_one.compareTo(rest2.cost_for_one, true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        layoutManager = LinearLayoutManager(activity)
        recyclerView = view.findViewById(R.id.recyclerViewDashboard)
        etSearch = view.findViewById(R.id.etSearch)
        dashboardProgressLayout = view.findViewById(R.id.dashboardProgressLayout)
        cantFind = view.findViewById(R.id.cantFind)

        fun filterFun(strTyped: String) {
            val filteredList = arrayListOf<Restaurant>()

            for (item in restaurantInfoList) {
                if (item.restaurantName.toLowerCase(Locale.ROOT)
                        .contains(strTyped.toLowerCase(Locale.ROOT))
                ) {
                    filteredList.add(item)
                }
            }

            if (filteredList.size == 0) {
                cantFind.visibility = View.VISIBLE
            } else {
                cantFind.visibility = View.INVISIBLE
            }

            dashboardAdapter.filterList(filteredList)

        }

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(strTyped: Editable?) {
                filterFun(strTyped.toString())
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        return view
    }

    fun fetchData() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {

            dashboardProgressLayout.visibility = View.VISIBLE
            try {
                val queue = Volley.newRequestQueue(activity as Context)
                val url = "http://" + getString(R.string.ip_address) + "/v2/restaurants/fetch_result"

                val jsonObjectRequest = object : JsonObjectRequest(
                    Method.GET,
                    url,
                    null,
                    Response.Listener {

                        println("Response12 is $it")
                        val response = it.getJSONObject("data")
                        val success = response.getBoolean("success")
                        if (success) {

                            val data = response.getJSONArray("data")
                            for (i in 0 until data.length()) {
                                val restaurantJsonObject = data.getJSONObject(i)
                                val restaurantObject = Restaurant(
                                    restaurantJsonObject.getString("id"),
                                    restaurantJsonObject.getString("name"),
                                    restaurantJsonObject.getString("rating"),
                                    restaurantJsonObject.getString("cost_for_one"),
                                    restaurantJsonObject.getString("image_url")
                                )
                                restaurantInfoList.add(restaurantObject)
                                dashboardAdapter = DashboardFragmentAdapter(activity as Context, restaurantInfoList)
                                recyclerView.adapter = dashboardAdapter
                                recyclerView.layoutManager = layoutManager
                            }
                        }
                        dashboardProgressLayout.visibility = View.INVISIBLE
                    },
                    Response.ErrorListener {

                        dashboardProgressLayout.visibility = View.INVISIBLE

                        Toast.makeText(
                            activity as Context,
                            "Some Error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
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
                    activity as Context,
                    "Some Unexpected error occurred!!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dashboard, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.sort -> {
                radioButtonView = View.inflate(
                    contextParam,
                    R.layout.sort_radio_button,
                    null
                )
                androidx.appcompat.app.AlertDialog.Builder(activity as Context)
                    .setTitle("Sort By?")
                    .setView(radioButtonView)
                    .setPositiveButton("OK") { _, _ ->
                        if (radioButtonView.radio_high_to_low.isChecked) {
                            Collections.sort(restaurantInfoList, costComparator)
                            restaurantInfoList.reverse()
                            dashboardAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_low_to_high.isChecked) {
                            Collections.sort(restaurantInfoList, costComparator)
                            dashboardAdapter.notifyDataSetChanged()
                        }
                        if (radioButtonView.radio_rating.isChecked) {
                            Collections.sort(restaurantInfoList, ratingComparator)
                            restaurantInfoList.reverse()
                            dashboardAdapter.notifyDataSetChanged()
                        }
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                    }
                    .create()
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        if (ConnectionManager().checkConnectivity(activity as Context)) {
            if (restaurantInfoList.isEmpty())
                fetchData()
        } else {

            val alterDialog = androidx.appcompat.app.AlertDialog.Builder(activity as Context)
            alterDialog.setTitle("No Internet")
            alterDialog.setMessage("Check Internet Connection!")
            alterDialog.setPositiveButton("Open Settings") { _, _ ->
                val settingsIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(settingsIntent)
            }
            alterDialog.setNegativeButton("Exit") { _, _ ->
                ActivityCompat.finishAffinity(activity as Activity)
            }
            alterDialog.setCancelable(false)
            alterDialog.create()
            alterDialog.show()
        }
        super.onResume()
    }
}





