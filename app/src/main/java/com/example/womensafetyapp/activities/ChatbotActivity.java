package com.example.womensafetyapp.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.womensafetyapp.R;
import com.example.womensafetyapp.adapters.ChatAdapter;
import com.example.womensafetyapp.models.ChatMessage;

import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        initializeViews();
        setupChat();
        setupInputListener();
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
    }

    private void setupChat() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        // Add welcome message
        addBotMessage("Hello! I'm your safety assistant. How can I help you today?");
    }

    private void setupInputListener() {
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendButton.setEnabled(s.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            messageInput.setText("");
            processUserMessage(message);
        }
    }

    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    private void processUserMessage(String message) {
        // Simple response logic - you can replace this with actual AI integration
        String response;
        message = message.toLowerCase();

        if (message.contains("help") || message.contains("emergency")) {
            response = "I can help you with that. Would you like me to activate emergency mode?";
        } else if (message.contains("location") || message.contains("where")) {
            response = "I can help you share your location with emergency contacts. Would you like to do that?";
        } else if (message.contains("contact") || message.contains("call")) {
            response = "I can help you contact your emergency contacts. Would you like me to do that?";
        } else if (message.contains("thank") || message.contains("thanks")) {
            response = "You're welcome! Is there anything else I can help you with?";
        } else {
            response = "I'm here to help with your safety. You can ask me about emergency mode, location sharing, or contacting emergency contacts.";
        }

        // Simulate typing delay
        new android.os.Handler().postDelayed(() -> addBotMessage(response), 1000);
    }
} 