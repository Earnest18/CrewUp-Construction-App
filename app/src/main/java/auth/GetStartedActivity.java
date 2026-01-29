package auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.CreateAccount;
import com.example.ConstructionApp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import app.MainActivity;

public class GetStartedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            String uid = auth.getCurrentUser().getUid();

            checkUserRole(uid);

            return;
        }

        setContentView(R.layout.activity_getstarted);

        View main = findViewById(R.id.main);
        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(
                    v.getPaddingLeft(),
                    topInset,
                    v.getPaddingRight(),
                    v.getPaddingBottom()
            );
            return insets;
        });

        Button login = findViewById(R.id.btnLogin);
        Button signup = findViewById(R.id.btnSignUp);

        login.setOnClickListener(v ->
                startActivity(new Intent(this, auth.Login.class)));

        signup.setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccount.class)));
    }

    private void checkUserRole(String uid) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        // No user record → fallback
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                        return;
                    }

                    String role = doc.getString("Role");

                    if ("worker".equals(role)) {

                        Toast.makeText(this, "Welcome Worker!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, workers.app.MainActivity.class));

                    } else if ("client".equals(role)) {

                        Toast.makeText(this, "Welcome Client!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));

                    } else {
                        // Unknown role
                        Toast.makeText(this, "Invalid user role", Toast.LENGTH_SHORT).show();
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}