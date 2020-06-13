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
import androidx.core.app.ActivityCompat

import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.utils.ConnectionManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class ForgotPasswordFragment(val contextParam: Context, val mobileNumber: String) : Fragment() {

    lateinit var etOTP: EditText
    lateinit var etNewPassword: EditText
    lateinit var etConfirmPassword: EditText
    lateinit var forgotPasswordLayout: RelativeLayout
    lateinit var btnSubmit: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)

        etOTP = view.findViewById(R.id.etOTP)
        etNewPassword = view.findViewById(R.id.etNewPassword)
        etConfirmPassword = view.findViewById(R.id.etConfirmPassword)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        forgotPasswordLayout = view.findViewById(R.id.forgotPasswordLayout)

        btnSubmit.setOnClickListener {
            if (etOTP.text.isBlank()) {
                etOTP.error = "OTP missing"
            } else {
                if (etNewPassword.text.isBlank() || etNewPassword.text.length <= 4) {
                    etNewPassword.error = "Invalid Password"
                } else {
                    if (etConfirmPassword.text.isBlank()) {
                        etConfirmPassword.error = "Confirm Password Missing"
                    } else {
                        if ((etNewPassword.text.toString().toInt() == etConfirmPassword.text.toString().toInt())
                        ) {
                            if (ConnectionManager().checkConnectivity(activity as Context)) {

                                forgotPasswordLayout.visibility = View.VISIBLE

                                try {
                                    //send mobile_number and password to get OTP
                                    val loginUser = JSONObject()
                                    loginUser.put("mobile_number", mobileNumber)
                                    loginUser.put("password", etNewPassword.text.toString())
                                    loginUser.put("otp", etOTP.text.toString())

                                    val queue = Volley.newRequestQueue(activity as Context)
                                    val url = "http://" + getString(R.string.ip_address) + "/v2/reset_password/fetch_result"

                                    val jsonObjectRequest = object : JsonObjectRequest(
                                        Method.POST,
                                        url,
                                        loginUser,
                                        Response.Listener {

                                            val response = it.getJSONObject("data")
                                            val success = response.getBoolean("success")

                                            if (success) {
                                                val serverMessage = response.getString("successMessage")

                                                Toast.makeText(
                                                    contextParam,
                                                    serverMessage,
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                passwordChanged()

                                            } else {
                                                val responseMessageServer =
                                                    response.getString("errorMessage")
                                                Toast.makeText(
                                                    contextParam,
                                                    responseMessageServer.toString(),
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            }
                                            forgotPasswordLayout.visibility = View.INVISIBLE
                                        },
                                        Response.ErrorListener {

                                            forgotPasswordLayout.visibility = View.INVISIBLE

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

                        } else {

                            etConfirmPassword.error = "Passwords don't match"

                        }
                    }
                }
            }
        }
        return view
    }

    fun passwordChanged() {
        val transaction = fragmentManager?.beginTransaction()
        transaction?.replace(
            R.id.frameLayout,
            LoginFragment(contextParam)
        )

        transaction?.commit()
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
