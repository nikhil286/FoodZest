package com.furiouspanda.foodzest.fragment


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat

import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.activity.Dashboard
import com.furiouspanda.foodzest.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class RegisterFragment(val contextParam: Context) : Fragment() {

    lateinit var etName: EditText
    lateinit var etEmail: EditText
    lateinit var etMobileNumber: EditText
    lateinit var etDeliveryAddress: EditText
    lateinit var etPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var btnRegister: Button
    lateinit var toolBar: Toolbar
    lateinit var registerProgressLayout: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_register, container, false)

        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etMobileNumber = view.findViewById(R.id.etMobileNumber)
        etDeliveryAddress = view.findViewById(R.id.etDeliveryAddress)
        etPassword = view.findViewById(R.id.etPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnRegister = view.findViewById(R.id.btnRegister)
        toolBar = view.findViewById(R.id.toolBar)
        registerProgressLayout = view.findViewById(R.id.registerProgressLayout)

        setToolBar()

        btnRegister.setOnClickListener{
            val sharedPreferences = contextParam.getSharedPreferences(
                getString(R.string.shared_preferences),
                Context.MODE_PRIVATE
            )

            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply()

            if (ConnectionManager().checkConnectivity(activity as Context)) {

                if (checkForErrors()) {

                    registerProgressLayout.visibility = View.VISIBLE
                    try {

                        val registerUser = JSONObject()
                        registerUser.put("name", etName.text)
                        registerUser.put("mobile_number", etMobileNumber.text)
                        registerUser.put("password", etPassword.text)
                        registerUser.put("address", etDeliveryAddress.text)
                        registerUser.put("email", etEmail.text)

                        val queue = Volley.newRequestQueue(activity as Context)
                        val url = "http://" + getString(R.string.ip_address) + "/v2/register/fetch_result"

                        val jsonObjectRequest = object : JsonObjectRequest(
                            Method.POST,
                            url,
                            registerUser,
                            Response.Listener {
                                println("Response12 is $it")

                                val response = it.getJSONObject("data")
                                val success = response.getBoolean("success")

                                if (success) {
                                    val data = response.getJSONObject("data")
                                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply()
                                    sharedPreferences.edit().putString("user_id", data.getString("user_id")).apply()
                                    sharedPreferences.edit().putString("name", data.getString("name")).apply()
                                    sharedPreferences.edit().putString("email", data.getString("email")).apply()
                                    sharedPreferences.edit().putString("mobile_number", data.getString("mobile_number")).apply()
                                    sharedPreferences.edit().putString("address", data.getString("address")).apply()

                                    Toast.makeText(
                                        contextParam,
                                        "Registered successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    userSuccessfullyRegistered()

                                } else {
                                    val responseMessageServer =
                                        response.getString("errorMessage")
                                    Toast.makeText(
                                        contextParam,
                                        responseMessageServer.toString(),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                registerProgressLayout.visibility = View.INVISIBLE
                            },
                            Response.ErrorListener {
                                println("Error12 is $it")
                                registerProgressLayout.visibility = View.INVISIBLE

                                Toast.makeText(
                                    contextParam,
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
                            contextParam,
                            "Some unexpected error occurred!!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
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
                alterDialog.create()
                alterDialog.show()
            }
        }
        return view
    }

    fun userSuccessfullyRegistered() {
        val intent = Intent(activity as Context, Dashboard::class.java)
        startActivity(intent)
        activity?.finish()
    }

    fun checkForErrors(): Boolean {
        var noErrors = 0
        if (etName.text.isBlank()) {
            etName.error = "Name Missing!"
        } else {
            noErrors++
        }

        if (etMobileNumber.text.isBlank() || etMobileNumber.text.length != 10) {
            etMobileNumber.error = "Invalid Mobile Number!"
        } else {
            noErrors++
        }

        if (etEmail.text.isBlank()) {
            etEmail.error = "Email Missing!"
        } else {
            noErrors++
        }

        if (etDeliveryAddress.text.isBlank()) {
            etDeliveryAddress.error = "Address Missing!"
        } else {
            noErrors++
        }

        if (etConfirmPassword.text.isBlank()) {
            etConfirmPassword.error = "Field Missing!"
        } else {
            noErrors++
        }

        if (etPassword.text.isBlank() || etPassword.text.length <= 4) {
            etPassword.error = "Invalid Password!"
        } else {
            noErrors++
        }

        if (etPassword.text.isNotBlank() && etConfirmPassword.text.isNotBlank()) {
            if (etPassword.text.toString().toInt() == etConfirmPassword.text.toString().toInt()
            ) {
                noErrors++
            } else {
                etConfirmPassword.error = "Password don't match"
            }
        }

        return noErrors == 7

    }

    fun setToolBar() {

        (activity as AppCompatActivity).setSupportActionBar(toolBar)
        (activity as AppCompatActivity).supportActionBar?.title = "Register Yourself"
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_back_arrow)
    }

    override fun onResume() {

        if (!ConnectionManager().checkConnectivity(activity as Context)) {

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
