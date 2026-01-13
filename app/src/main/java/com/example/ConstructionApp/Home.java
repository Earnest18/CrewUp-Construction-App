package com.example.ConstructionApp;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Home extends Fragment {

    private RecyclerView recyclerPosts;
    private ArrayList<Post> posts;
    private PostAdapter adapter;
    private FirebaseFirestore db;

    public Home() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        requireActivity().getWindow().getInsetsController().setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
        );

        View main = view.findViewById(R.id.main);

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

        // RecyclerView setup
        recyclerPosts = view.findViewById(R.id.recyclerPosts);
        recyclerPosts.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new PostAdapter(posts);
        recyclerPosts.setAdapter(adapter);

        // Firestore
        db = FirebaseFirestore.getInstance();
        posts = new ArrayList<>();

        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    posts.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String title = doc.getString("title");
                        String content = doc.getString("content");
                        long timestamp = doc.getLong("timestamp") != null
                                ? doc.getLong("timestamp")
                                : 0;

                        posts.add(new Post(
                                "Earnest Reyes",
                                title,
                                content,
                                formatTimestamp(timestamp)
                        ));
                    }
                    adapter.notifyDataSetChanged();
                });

        return view;
    }




    // Helper to format timestamp nicely
    private String formatTimestamp(long millis) {
        if (millis == 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault());
        return sdf.format(new Date(millis));
    }
}
