package com.example.ConstructionApp;

import android.accounts.AccountManagerFuture;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class RecentChatRecyclerAdapter
        extends FirestoreRecyclerAdapter<ChatroomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder> {

    private final Context context;

    public RecentChatRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<ChatroomModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull ChatroomModelViewHolder holder,
            int position,
            @NonNull ChatroomModel model
    ) {

        // 🔒 Always reset click (RecyclerView reuse)
        holder.itemView.setOnClickListener(null);

        if (FirebaseUtil.getOtherUserFromChatroom(model.getUserIds()) == null) {
            return;
        }

        FirebaseUtil.getOtherUserFromChatroom(model.getUserIds())
                .get()
                .addOnSuccessListener(snapshot -> {

                    UserModel otherUserModel = snapshot.toObject(UserModel.class);
                    if (otherUserModel == null) return;

                    otherUserModel.setUserId(snapshot.getId());



                    holder.usernameText.setText(otherUserModel.getUsername());

                    boolean lastMessageSentByMe =
                            model.getLastMessageSenderId() != null &&
                                    model.getLastMessageSenderId()
                                            .equals(FirebaseUtil.currentUserId());

                    holder.lastMessageText.setText(
                            lastMessageSentByMe
                                    ? "You: " + model.getLastMessage()
                                    : model.getLastMessage()
                    );

                    holder.lastMessageTime.setText(
                            FirebaseUtil.timestampToString(
                                    model.getLastMessageTimestamp()
                            )
                    );

                    String otherUserId = otherUserModel.getUserId();

                    if (otherUserId != null && !otherUserId.isEmpty()) {
                        FirebaseUtil.getOtherProfilePicStorageRef(otherUserId)
                                .getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        AndroidUtil.setProfilePic(
                                                context,
                                                uri,
                                                holder.profilePic
                                        )
                                );
                    }

                    holder.itemView.setOnClickListener(v -> {
                        Intent intent = new Intent(context, ChatActivity.class);

                        AndroidUtil.passUserModelAsIntent(
                                intent,
                                otherUserModel,
                                otherUserModel.getUserId()
                        );
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    });
                });
    }

    @NonNull
    @Override
    public ChatroomModelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_message, parent, false);
        return new ChatroomModelViewHolder(view);
    }

    static class ChatroomModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView lastMessageText;
        TextView lastMessageTime;
        ImageView profilePic;

        public ChatroomModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.txtName);
            lastMessageText = itemView.findViewById(R.id.txtLastMessage);
            lastMessageTime = itemView.findViewById(R.id.txtTime);
            profilePic = itemView.findViewById(R.id.imgProfile);
        }
    }
}
