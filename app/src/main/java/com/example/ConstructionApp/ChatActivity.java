package com.example.ConstructionApp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    private String otherUserId;
    private UserModel otherUser;

    private String chatroomId;
    private ChatroomModel chatroomModel;
    private ChatRecyclerAdapter adapter;

    private EditText messageInput;
    private ImageButton sendMessageBtn, backBtn;
    private TextView otherUsername;
    private RecyclerView recyclerView;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 🔑 Get userId from Intent
        otherUserId = getIntent().getStringExtra("userId");

        Log.d("CHAT_DEBUG", "ChatActivity started with userId=" + otherUserId);

        // 🚨 HARD STOP — prevents ALL Firestore crashes
        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔧 Initialize views
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        profilePic = findViewById(R.id.profile_pic_image_view);

        backBtn.setOnClickListener(v -> {
            Intent in = new Intent(this, Home.class);
            startActivity(in);
        });

        // 🔗 Chatroom ID
        chatroomId = FirebaseUtil.getChatroomId(
                FirebaseUtil.currentUserId(),
                otherUserId
        );

        loadOtherUser();
        setupChatRecyclerView();
        getOrCreateChatroomModel();

        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToUser(message);
            }
        });
    }

    // 🔍 Load other user's data
    private void loadOtherUser() {
        FirebaseUtil.getUserReference(otherUserId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    otherUser = snapshot.toObject(UserModel.class);
                    if (otherUser == null) return;

                    otherUsername.setText(otherUser.getUsername());

                    FirebaseUtil.getOtherProfilePicStorageRef(otherUserId)
                            .getDownloadUrl()
                            .addOnSuccessListener(uri ->
                                    AndroidUtil.setProfilePic(this, uri, profilePic)
                            );
                });
    }

    // 💬 Messages list
    private void setupChatRecyclerView() {
        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<ChatMessageModel> options =
                new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                        .setQuery(query, ChatMessageModel.class)
                        .build();

        adapter = new ChatRecyclerAdapter(options, this);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);

        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    // ✉️ Send message
    private void sendMessageToUser(String message) {
        Timestamp now = Timestamp.now();

        FirebaseUtil.getChatroomReference(chatroomId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    ChatroomModel model = snapshot.toObject(ChatroomModel.class);

                    if (model == null) {
                        model = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(
                                        FirebaseUtil.currentUserId(),
                                        otherUserId
                                ),
                                message,
                                FirebaseUtil.currentUserId(),
                                now
                        );
                    } else {
                        model.setLastMessage(message);
                        model.setLastMessageSenderId(FirebaseUtil.currentUserId());
                        model.setLastMessageTimestamp(now);
                    }

                    FirebaseUtil.getChatroomReference(chatroomId)
                            .set(model)
                            .addOnSuccessListener(unused -> {

                                ChatMessageModel chatMessage =
                                        new ChatMessageModel(
                                                message,
                                                FirebaseUtil.currentUserId(),
                                                now
                                        );

                                FirebaseUtil.getChatroomMessageReference(chatroomId)
                                        .add(chatMessage)
                                        .addOnSuccessListener(ref ->
                                                messageInput.setText("")
                                        );
                            });
                });
    }

    // 🏗 Create chatroom if missing
    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    chatroomModel = snapshot.toObject(ChatroomModel.class);

                    if (chatroomModel == null) {
                        chatroomModel = new ChatroomModel(
                                chatroomId,
                                Arrays.asList(
                                        FirebaseUtil.currentUserId(),
                                        otherUserId
                                ),
                                "",
                                "",
                                Timestamp.now()
                        );

                        FirebaseUtil.getChatroomReference(chatroomId)
                                .set(chatroomModel);
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) adapter.stopListening();
    }
}
