package com.example.finalterm.recycleView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalterm.databinding.ChatItemBinding

class ChatAdapter (private val messages: List<MessageData>):
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>(){
    class ChatViewHolder(val binding: ChatItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ChatItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val message = messages[position]
        with(holder.binding) {
            if(message.isUser){
                userMessageText.visibility = View.VISIBLE
                aiMessageText.visibility = View.GONE
                userMessageTime.visibility = View.VISIBLE
                aiMessageTime.visibility = View.GONE
                aiLabel.visibility = View.GONE
                userMessageText.text = message.message
                userMessageTime.text = message.timestamp

            } else {
                aiMessageText.visibility = View.VISIBLE
                userMessageText.visibility = View.GONE
                aiMessageTime.visibility = View.VISIBLE
                userMessageTime.visibility = View.GONE
                aiLabel.visibility = View.VISIBLE
                aiMessageText.text = message.message
                aiMessageTime.text = message.timestamp
            }
        }
    }

    override fun getItemCount() = messages.size }