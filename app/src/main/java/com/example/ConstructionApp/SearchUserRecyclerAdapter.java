package com.example.ConstructionApp;

import static com.example.ConstructionApp.FirebaseUtil.currentUserId;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.storage.StorageReference;

public class SearchUserRecyclerAdapter
        extends FirestoreRecyclerAdapter<UserModel, SearchUserRecyclerAdapter.UserModelViewHolder> {

    private final Context context;

    public SearchUserRecyclerAdapter(
            @NonNull FirestoreRecyclerOptions<UserModel> options,
            Context context
    ) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(
            @NonNull UserModelViewHolder holder,
            int position,
            @NonNull UserModel model
    ) {

        String userId = getSnapshots().getSnapshot(position).getId();

        /* ---------- USERNAME ---------- */
        String username = model.getUsername();
        if (username == null || username.trim().isEmpty()) {
            username = "Unknown user";
        }

        if (userId.equals(currentUserId())) {
            username += " (Me)";
        }

        holder.usernameText.setText(username);

        /* ---------- SUBTEXT (EMAIL / LOCATION) ---------- */
        String email = model.getEmail();
        String location = model.getLocation();

        if (email != null && !email.trim().isEmpty()) {
            holder.subText.setText(email);
            holder.subText.setVisibility(View.VISIBLE);
        } else if (location != null && !location.trim().isEmpty()) {
            holder.subText.setText(location);
            holder.subText.setVisibility(View.VISIBLE);
        } else {
            holder.subText.setText("");
            holder.subText.setVisibility(View.GONE);
        }

        /* ---------- PROFILE PICTURE ---------- */
        holder.profilePic.setImageResource(
                R.drawable.ic_profile_placeholder_foreground
        );

        StorageReference picRef =
                FirebaseUtil.getOtherProfilePicStorageRef(userId);

        if (picRef != null) {
            picRef.getDownloadUrl()
                    .addOnSuccessListener(uri ->
                            AndroidUtil.setProfilePic(
                                    context, uri, holder.profilePic
                            )
                    )
                    .addOnFailureListener(e ->
                            holder.profilePic.setImageResource(
                                    R.drawable.ic_profile_placeholder_foreground
                            )
                    );
        }

        /* ---------- CLICK ---------- */
        holder.itemView.setOnClickListener(v -> {
            FirebaseUtil.addToRecentSearch(model, userId);

            Intent intent = new Intent(context, ChatActivity.class);
            AndroidUtil.passUserModelAsIntent(intent, model, userId);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
    }

    @NonNull
    @Override
    public UserModelViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.search_user_recycler_row, parent, false);
        return new UserModelViewHolder(view);
    }

    static class UserModelViewHolder extends RecyclerView.ViewHolder {

        TextView usernameText;
        TextView subText;
        ImageView profilePic;

        public UserModelViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.user_name_text);
            subText = itemView.findViewById(R.id.phone_text);
            profilePic = itemView.findViewById(R.id.profile_pic_image_view);
        }
    }
}
