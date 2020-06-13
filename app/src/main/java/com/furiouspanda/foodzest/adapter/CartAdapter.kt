package com.furiouspanda.foodzest.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.model.CartItems


class CartAdapter(val context: Context, val cartItems: ArrayList<CartItems>) :
    RecyclerView.Adapter<CartAdapter.ViewHolderCart>() {


    class ViewHolderCart(view: View) : RecyclerView.ViewHolder(view) {
        val txtOrderItem: TextView = view.findViewById(R.id.txtOrderItem)
        val txtOrderItemPrice: TextView = view.findViewById(R.id.txtOrderItemPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCart {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.cart_recycler_view_single_row, parent, false)
        return ViewHolderCart(view)
    }

    override fun getItemCount(): Int {
        return cartItems.size
    }

    override fun onBindViewHolder(holder: ViewHolderCart, position: Int) {
        val cartItem = cartItems[position]
        holder.txtOrderItem.text = cartItem.itemName
        holder.txtOrderItemPrice.text = "Rs. ${cartItem.itemPrice}"
    }
}