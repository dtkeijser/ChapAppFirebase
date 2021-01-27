package com.example.chapapp.views

import com.example.chapapp.R
import com.example.chapapp.models.ChatMessage
import com.example.chapapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.latest_message_item.view.*

class LatestMessageRow(val chatMessage: ChatMessage): Item<ViewHolder>() {

    var chatPartnerUser:  User? = null
    override fun getLayout(): Int {
        return R.layout.latest_message_item
    }

    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_latest_massage_latest_message.text = chatMessage.text

        val chatPatnerId: String

        if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatPatnerId = chatMessage.toId
        } else {
            chatPatnerId = chatMessage.fromId
        }
        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPatnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.tv_username_latest_message_item.text = chatPartnerUser?.username

                val targetImageView = viewHolder.itemView.iv_latest_mesages
                Picasso.get().load(chatPartnerUser?.profileImageUrL).into(targetImageView)
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }
}