package com.furiouspanda.foodzest.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.furiouspanda.foodzest.R
import com.furiouspanda.foodzest.activity.RestaurantMenuActivity
import com.furiouspanda.foodzest.database.RestaurantDatabase
import com.furiouspanda.foodzest.database.RestaurantEntity
import com.furiouspanda.foodzest.model.Restaurant
import com.squareup.picasso.Picasso


class DashboardFragmentAdapter(val context: Context, var itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<DashboardFragmentAdapter.ViewHolderDashboard>() {

    class ViewHolderDashboard(view: View) : RecyclerView.ViewHolder(view) {
        val imgRestaurant: ImageView = view.findViewById(R.id.imgRestaurant)
        val txtRestaurantName: TextView = view.findViewById(R.id.txtRestaurantName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtRating: TextView = view.findViewById(R.id.txtRating)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
        val txtFavorite: TextView = view.findViewById(R.id.txtFavorite)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDashboard {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_recycler_view_single_row, parent, false)

        return ViewHolderDashboard(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolderDashboard, position: Int) {

        val restaurant = itemList[position]
        val restaurantEntity = RestaurantEntity(restaurant.restaurantId, restaurant.restaurantName)

        holder.txtRestaurantName.tag = restaurant.restaurantId + ""
        holder.txtRestaurantName.text = restaurant.restaurantName
        holder.txtPrice.text = restaurant.cost_for_one + "/Person"
        holder.txtRating.text = restaurant.restaurantRating

        //Load images using Picasso
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.ic_default_image_restaurant)
            .into(holder.imgRestaurant)

        //Adding and removing from favourites using database
        holder.txtFavorite.setOnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {
                val result = DBAsyncTask(context, restaurantEntity, 2).execute().get()

                if (result) {
                    Toast.makeText(context, "${restaurant.restaurantName} added to Favorites", Toast.LENGTH_SHORT).show()
                    holder.txtFavorite.tag = "liked"
                    holder.txtFavorite.background = context.resources.getDrawable(R.drawable.ic_fav_fill)

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }

            } else {
                val result = DBAsyncTask(context, restaurantEntity, 3).execute().get()
                if (result) {

                    Toast.makeText(context, "${restaurant.restaurantName} removed from Favorites", Toast.LENGTH_SHORT).show()
                    holder.txtFavorite.tag = "unliked"
                    holder.txtFavorite.background = context.resources.getDrawable(R.drawable.ic_fav_outline)

                } else {
                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                }
            }
        }

        holder.llContent.setOnClickListener {

            println(holder.txtRestaurantName.tag.toString())
            val intent = Intent(context, RestaurantMenuActivity::class.java)
            intent.putExtra("restaurantId", holder.txtRestaurantName.tag.toString())
            intent.putExtra("restaurantName", holder.txtRestaurantName.text.toString())
            context.startActivity(intent)
        }

        val checkFav = DBAsyncTask(context, restaurantEntity, 1).execute()
        val isFav = checkFav.get()
        if (isFav) {
            holder.txtFavorite.tag = "liked"
            holder.txtFavorite.background = context.resources.getDrawable(R.drawable.ic_fav_fill)

        } else {
            holder.txtFavorite.tag = "unliked"
            holder.txtFavorite.background = context.resources.getDrawable(R.drawable.ic_fav_outline)
        }
    }

    fun filterList(filteredList: ArrayList<Restaurant>) {
        itemList = filteredList
        notifyDataSetChanged()
    }

    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            /*
            * Mode 1->check if restaurant is in favourites
            * Mode 2->Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant*/
            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                else -> return false

            }
        }
    }
}

