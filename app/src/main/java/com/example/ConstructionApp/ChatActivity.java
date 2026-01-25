package com.example.ConstructionApp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {

    private String otherUserId;
    private UserModel otherUser;
    private String chatroomId;
    private ChatRecyclerAdapter adapter;

    private EditText messageInput;
    private ImageButton sendMessageBtn, backBtn;
    private TextView otherUsername;
    private RecyclerView recyclerView;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Edge-to-edge (required for IME)
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_chat);

        otherUserId = getIntent().getStringExtra("userId");

        if (otherUserId == null || otherUserId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Views
        messageInput = findViewById(R.id.chat_message_input);
        sendMessageBtn = findViewById(R.id.message_send_btn);
        backBtn = findViewById(R.id.back_btn);
        otherUsername = findViewById(R.id.other_username);
        recyclerView = findViewById(R.id.chat_recycler_view);
        profilePic = findViewById(R.id.profile_pic_image_view);

        View root = findViewById(R.id.main);
        View bottomLayout = findViewById(R.id.bottom_layout);

        // ✅ INSETS HANDLING (keyboard + system bars)
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());

            boolean isKeyboardVisible =
                    insets.isVisible(WindowInsetsCompat.Type.ime());

            int keyboardHeight = isKeyboardVisible
                    ? imeInsets.bottom - systemBars.bottom
                    : 0;

            keyboardHeight = Math.max(0, keyboardHeight);

            // Status bar safe area
            v.setPadding(
                    v.getPaddingLeft(),
                    systemBars.top,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );

            // Move input above keyboard
            bottomLayout.setTranslationY(-keyboardHeight);

            // Gesture navigation safe area
            bottomLayout.setPadding(
                    bottomLayout.getPaddingLeft(),
                    bottomLayout.getPaddingTop(),
                    bottomLayout.getPaddingRight(),
                    systemBars.bottom
            );

            // Resize chat list
            recyclerView.setPadding(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getPaddingRight(),
                    keyboardHeight + systemBars.bottom
            );

            return insets;
        });

        // 🔙 Back button
        backBtn.setOnClickListener(v -> finish());

        // ✅ Chat setup (THIS WAS MISSING)
        chatroomId = FirebaseUtil.getChatroomId(
                FirebaseUtil.currentUserId(),
                otherUserId
        );

        loadOtherUser();
        setupChatRecyclerView();
        getOrCreateChatroomModel();

        // 📤 Send message
        sendMessageBtn.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToUser(message);
            }
        });
    }

        /* ---------------- LOAD USER ---------------- */

        private void loadOtherUser () {
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

                        otherUser.setUserId(snapshot.getId());
                        otherUsername.setText(otherUser.getUsername());

                        String url = snapshot.getString("profilePicUrl");
                        if (url != null && !url.isEmpty()) {
                            Glide.with(this)
                                    .load(url)
                                    .circleCrop()
                                    .into(profilePic);
                        }
                    });
        }


    /* ---------------- CHAT LIST ---------------- */

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
    }

    /* ---------------- SEND MESSAGE ---------------- */

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

    /* ---------------- CREATE CHATROOM ---------------- */

    private void getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        ChatroomModel chatroomModel = new ChatroomModel(
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

    /* ---------------- LIFECYCLE ---------------- */

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
