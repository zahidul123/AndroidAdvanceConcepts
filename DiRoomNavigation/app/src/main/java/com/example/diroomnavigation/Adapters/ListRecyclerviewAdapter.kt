package com.example.diroomnavigation.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.diroomnavigation.Model.User
import com.example.diroomnavigation.R
import kotlinx.android.synthetic.main.rcv_item_layout.view.*

class ListRecyclerviewAdapter (val user: ArrayList<User>) :
    RecyclerView.Adapter<ListRecyclerviewAdapter.RcvViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = RcvViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.rcv_item_layout, parent,
            false
        )
    )


    override fun onBindViewHolder(holder: RcvViewHolder, position: Int) =holder.bind(user[position])

    override fun getItemCount(): Int =user.size

    class RcvViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(user: User) {
            itemView.textViewUserName.text = user.name
            itemView.textViewUserEmail.text = user.email
            Glide.with(itemView.imageViewAvatar.context)
                .load(user.avatar)
                .into(itemView.imageViewAvatar)

        }

    }

    fun addData(list: List<User>) {
        user.addAll(list)
    }
}