package com.furiouspanda.foodzest.fragment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.furiouspanda.foodzest.R

class ProfileFragment(val contextParam: Context) : Fragment() {

    lateinit var txtName: TextView
    lateinit var txtEmail: TextView
    lateinit var txtMobileNumber: TextView
    lateinit var txtAddress: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        txtName = view.findViewById(R.id.txtName)
        txtEmail = view.findViewById(R.id.txtEmail)
        txtMobileNumber = view.findViewById(R.id.txtMobileNumber)
        txtAddress = view.findViewById(R.id.txtAddress)

        val sharedPreferences = contextParam.getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )

        txtName.text = sharedPreferences.getString("name", "")
        txtEmail.text = sharedPreferences.getString("email", "")
        txtMobileNumber.text = "+91-" + sharedPreferences.getString("mobile_number", "")
        txtAddress.text = sharedPreferences.getString("address", "")

        return view
    }

}
