package com.example.finalterm.recycleView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalterm.data.entity.ChatEntity
import com.example.finalterm.databinding.ChatListItemBinding

class ChatListAdapter (
    private val chatEntities: List<ChatEntity>,
    private val onChatClick: (Int) -> Unit,
    private val onDeleteClick: (Int) -> Unit
): RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder>(){

    class ChatListViewHolder(val binding: ChatListItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val binding = ChatListItemBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ChatListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chat = chatEntities[position]
        with(holder.binding) {
            chatTitle.text = chat.timestamp
            root.setOnClickListener { onChatClick(chat.id) }
            deleteBtn.setOnClickListener { onDeleteClick(chat.id) }
        }
    }

    fun updateData(newChatEntity: List<ChatEntity>) {
        (chatEntities as MutableList).clear()
        chatEntities.addAll(newChatEntity)
        notifyDataSetChanged()
    }

    override fun getItemCount() = chatEntities.size
}