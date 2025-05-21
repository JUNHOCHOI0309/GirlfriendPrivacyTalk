package com.example.finalterm

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalterm.api.ApiClient
import com.example.finalterm.api.ChatRequest
import com.example.finalterm.api.ChatResponse
import com.example.finalterm.api.Message
import com.example.finalterm.data.database.ChatDatabase
import com.example.finalterm.data.entity.ChatEntity
import com.example.finalterm.utils.convertJsonToMessages
import com.example.finalterm.utils.convertMessagesToJson
import com.example.finalterm.databinding.ActivityMainBinding
import com.example.finalterm.databinding.ContentMainBinding
import com.example.finalterm.databinding.NavHeaderBinding
import com.example.finalterm.recycleView.ChatAdapter
import com.example.finalterm.recycleView.ChatListAdapter
import com.example.finalterm.recycleView.MessageData
import com.example.finalterm.utils.getCurrentTime
import com.example.finalterm.utils.getFormattedDateTime
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val chatMessages = mutableListOf<MessageData>()
    private var currentSessionId = -1

    lateinit var binding: ActivityMainBinding
    lateinit var contentBinding: ContentMainBinding
    lateinit var chatAdapter: ChatAdapter
    lateinit var sideBarBinding: NavHeaderBinding
    lateinit var chatListAdapter: ChatListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        contentBinding = ContentMainBinding.bind(binding.contentMain.root)
        sideBarBinding = NavHeaderBinding.bind(binding.sideBar.getHeaderView(0))
        setContentView(binding.root)

        //SideBar
        val main = binding.main
        val sideBar: ImageView = contentBinding.sBBtn

        //OpenAIApi
        val inputField = contentBinding.prompt
        val send = contentBinding.sendBtn
        //val responseView = contentBinding.testResponse

        //messengerBox
        val chatRecyclerView: RecyclerView = contentBinding.chatRecyclerView
        chatAdapter = ChatAdapter(chatMessages)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.adapter = chatAdapter

        sideBarBinding.chatListRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity)

        sideBar.setOnClickListener {
            if (!main.isDrawerOpen(binding.sideBar)) {
                main.openDrawer(binding.sideBar)
            }
        }

        send.setOnClickListener {
            val userInput = inputField.text.toString()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, isUser = true)
                inputField.setText("")
                sendMessageToChatGPT(userInput) { response ->
                    addMessage(response, isUser = false)
                }
            }
        }

        //Log.d("mop","Start ID : ${currentSessionId}")
        startNewChat()
        setupRecyclerView()
        setupNewChatButton()
    }

    private fun sendMessageToChatGPT(userInput: String, callback: (String) -> Unit) {
        val personaSetting = getString(R.string.ai_persona_setting)
        val messages = mutableListOf(
            Message(
                role = "system", content = personaSetting
            )
        )
        chatMessages.forEach { message ->
            messages.add(
                Message(
                    role = if (message.isUser) "user" else "assistant",
                    content = message.message
                )
            )
        }
        messages.add(Message(role = "user", content = userInput))

        val request = ChatRequest(messages = messages)
        ApiClient.apiService.getChatCompletion(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val chatResponse = response.body()
                    val reply = chatResponse?.choices?.firstOrNull()?.message?.content
                    callback(reply ?: "No response")
                } else {
                    callback("Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                callback("Failed: ${t.message}")
            }
        })
    }

    private fun addMessage(message: String, isUser: Boolean) {
        val timestamp = getCurrentTime()
        chatMessages.add(MessageData(message, isUser, timestamp))
        chatAdapter.notifyItemInserted(chatMessages.size - 1)
        contentBinding.chatRecyclerView.scrollToPosition(chatMessages.size - 1)
        //Log.d("mop","현재의 대화 ID : ${currentSessionId}")

        if(currentSessionId == -1) {
            lifecycleScope.launch {
                val newChatEntity = ChatEntity(
                    title = contentBinding.title.text.toString(),
                    timestamp = getFormattedDateTime(),
                    chatContent = convertMessagesToJson(chatMessages)
                )
                currentSessionId = ChatDatabase.getDatabase(this@MainActivity).chatDao().insertChat(newChatEntity).toInt()
                loadChatEntities()
            }
        } else updateChatContent()
    }

    //채팅 시작 초기값 설정
    private fun startNewChat() {
        lifecycleScope.launch {
            saveChatToDatabase(contentBinding.title.text.toString(), chatMessages)
            chatMessages.clear()
            chatAdapter.notifyDataSetChanged()

            loadChatEntities()
        }
    }

    //현재 채팅 세션의 내용을 업데이트
    private fun updateChatContent() {
        lifecycleScope.launch {
            val chatContent = convertMessagesToJson(chatMessages)
            ChatDatabase.getDatabase(this@MainActivity).chatDao()
                .updateChatContent(currentSessionId, chatContent)
            //Log.d("mop", "Update 진행 후 현재 ID 값 : ${currentSessionId}")
        }
    }

    //sidebar ChatItemList Click Action
    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val loadChatEntities =
                ChatDatabase.getDatabase(this@MainActivity).chatDao().getAllChats()
            chatListAdapter = ChatListAdapter(
                loadChatEntities,
                onChatClick = { chatId -> loadChatContent(chatId) },
                onDeleteClick = { chatId -> deleteChat(chatId) }
            )

            sideBarBinding.chatListRecyclerView.layoutManager =
                LinearLayoutManager(this@MainActivity)
            sideBarBinding.chatListRecyclerView.adapter = chatListAdapter
        }
    }

    //새로운 채팅 생성
    private fun setupNewChatButton() {
        sideBarBinding.newChatBtn.setOnClickListener {
            lifecycleScope.launch {
                saveChatToDatabase(contentBinding.title.text.toString(), chatMessages)
                chatMessages.clear()
                chatAdapter.notifyDataSetChanged()

                currentSessionId = -1
                loadChatEntities()
            }
        }
    }

    //특정 채팅 항목 호출
    private fun loadChatContent(chatId: Int) {
        lifecycleScope.launch {
            val chatEntity =
                ChatDatabase.getDatabase(this@MainActivity).chatDao().getChatById(chatId)
            currentSessionId = chatId
            contentBinding.title.text = chatEntity.timestamp
            val messages = convertJsonToMessages(chatEntity.chatContent)
            displayChatContent(messages)
        }
    }

    //content_main 화면 메세지 표시
    private fun displayChatContent(messages: List<MessageData>) {
        chatMessages.clear()
        chatMessages.addAll(messages)
        chatAdapter.notifyDataSetChanged()
    }

    private fun deleteChat(chatId: Int) {
        lifecycleScope.launch {
            val chatDao = ChatDatabase.getDatabase(this@MainActivity).chatDao()
            val chatToDelete = chatDao.getChatById(chatId)
            chatDao.deleteChat(chatToDelete)
            loadChatEntities()
        }
    }

    //전체 채팅 목록 요청
    private fun loadChatEntities() {
        lifecycleScope.launch {
            val chatEntities =
                ChatDatabase.getDatabase(this@MainActivity).chatDao().getAllChats()
            if (::chatListAdapter.isInitialized) {
                chatListAdapter.updateData(chatEntities)
            } else {
                chatListAdapter = ChatListAdapter(
                    chatEntities,
                    onChatClick = { chatId -> loadChatContent(chatId) },
                    onDeleteClick = { chatId -> deleteChat(chatId) }
                )
                sideBarBinding.chatListRecyclerView.adapter = chatListAdapter
            }
        }
    }

    //새로운 채팅 세션을 저장하거나, 기존 세션을 업데이트
    private suspend fun saveChatToDatabase(title: String, messages: List<MessageData>) {
        val chatContent = convertMessagesToJson(messages)
        val timestamp = getFormattedDateTime()
        val chatEntity = ChatEntity(title = title, timestamp = timestamp, chatContent = chatContent)
        if (currentSessionId == -1) {
            currentSessionId =
                ChatDatabase.getDatabase(this).chatDao().insertChat(chatEntity).toInt()
            //Log.d("mop", "Save 진행 후 현재 ID 값 : ${currentSessionId}")
        } else {
            ChatDatabase.getDatabase(this).chatDao()
                .updateChatContent(currentSessionId, chatContent)
            //Log.d("mop", "Save 진행 후 현재 ID 값 : ${currentSessionId}")
        }
    }

    override fun onStop() {
        super.onStop()
        lifecycleScope.launch {
            saveChatToDatabase(contentBinding.title.text.toString(), chatMessages)
        }
    }
}