package com.example.chapapp.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.chapapp.NewMessageActivity
import com.example.chapapp.R
import com.example.chapapp.models.ChatMessage

import com.example.chapapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row_left.view.*
import kotlinx.android.synthetic.main.chat_to_row_right.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object {
        val TAG = "ChatLogActivity"
    }


    val adapter = GroupAdapter<ViewHolder>()
    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        rv_chat_log.adapter = adapter
         toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)

            supportActionBar?.title = toUser?.username

        //    setupDummyData()
        ListenForMessages()

        btn_send_chat_log.setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            performSendMessage()
        }
    }

    private fun ListenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
        ref.addChildEventListener(object : ChildEventListener {

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        //val toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatToItem(chatMessage.text, toUser!!))
                    }
                }
                rv_chat_log.scrollToPosition(adapter.itemCount -1)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun performSendMessage() {

        val text = et_chat_log.text.toString()
        // Pushes to messages
       // val reference = FirebaseDatabase.getInstance().getReference("/messages").push()



        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user!!.uid

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        if (fromId == null) return
        val chatMessage =
            ChatMessage(reference.key!!, text, fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Saved our chat message: ${reference.key}")
                et_chat_log.text.clear()
                rv_chat_log.scrollToPosition(adapter.itemCount -1)
            }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)

    }

//    private fun setupDummyData() {
//        val adapter = GroupAdapter<ViewHolder>()
//        adapter.add(ChatFromItem("from MESSAGES"))
//        adapter.add(ChatToItem("TO MESSAGE"))
//        adapter.add(ChatFromItem("YEAHHHHHHHHH"))
//        adapter.add(ChatToItem("HELLOOOO"))
//        adapter.add(ChatFromItem("BOOOOOOOOOOOOOOOOOOOOOOOOOOOOO"))
//
//
//        rv_chat_log.adapter = adapter
//    }
}

class ChatFromItem(val text: String,  val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_from_row_right.text = text

        //load image into chat
        val uri = user.profileImageUrL
        val targetImageView = viewHolder.itemView.iv_chat_from_row_left
        Picasso.get().load(uri).into(targetImageView)


    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row_left
    }

}

class ChatToItem(val text: String, val user: User) : Item<ViewHolder>() {
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.tv_chat_to_row_right.text = text

        val uri = user.profileImageUrL
        val targetImageView = viewHolder.itemView.iv_chat_to_row_right
        Picasso.get().load(uri).into(targetImageView)


    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row_right
    }

}