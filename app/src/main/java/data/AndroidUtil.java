package data;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.ConstructionApp.R;

import models.UserModel;

public class AndroidUtil {

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void passUserModelAsIntent(
            Intent intent,
            UserModel model,
            String userId
    ) {
        intent.putExtra("userId", userId);
        intent.putExtra("username", model.getUsername());
    }

    public static String getUserIdFromIntent(Intent intent) {
        return intent.getStringExtra("userId");
    }

    public static String getUsernameFromIntent(Intent intent) {
        return intent.getStringExtra("username");
    }

    public static void setProfilePic(
            Context context,
            Uri imageUri,
            ImageView imageView
    ) {
        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_profile_placeholder_foreground)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }
}

