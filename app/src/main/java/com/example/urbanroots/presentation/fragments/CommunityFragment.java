package com.example.urbanroots.presentation.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbanroots.domain.models.Post;
import com.example.urbanroots.presentation.adapters.PostAdapter;
import com.example.urbanroots.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CommunityFragment extends Fragment implements PostAdapter.OnPostInteractionListener {
    private static final String TAG = "CommunityFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView postsRecyclerView;
    private FloatingActionButton fabCreatePost;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fabCreatePost = view.findViewById(R.id.fab_create_post);
        postsRecyclerView = view.findViewById(R.id.posts_recycler_view);
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList, requireContext(), this);
        postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        postsRecyclerView.setAdapter(postAdapter);

        // Load posts
        loadPosts();

        // Create post via FAB
        fabCreatePost.setOnClickListener(v -> showCreatePostDialog());
    }

    private void loadPosts() {
        postList.clear();
        Log.d(TAG, "Starting to load posts");
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error loading posts: " + e.getMessage(), e);
                        Toast.makeText(requireContext(), "Error loading posts: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "No posts found");
                        return;
                    }
                    Log.d(TAG, "Found " + snapshots.size() + " posts");
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            try {
                                Post post = dc.getDocument().toObject(Post.class);
                                postList.add(post);
                                Log.d(TAG, "Added post: " + post.getPostId() + ", Description: " + post.getDescription());
                            } catch (Exception ex) {
                                Log.e(TAG, "Error deserializing post: " + dc.getDocument().getId(), ex);
                            }
                        }
                    }
                    Log.d(TAG, "Post list size: " + postList.size());
                    postAdapter.notifyDataSetChanged();
                });
    }

    private void showCreatePostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_post, null);
        TextInputEditText postDescriptionEditText = dialogView.findViewById(R.id.post_description_edit_text);

        builder.setView(dialogView)
                .setTitle("Create Post")
                .setPositiveButton("Post", (dialog, which) -> {
                    String description = postDescriptionEditText.getText().toString().trim();
                    if (description.isEmpty()) {
                        Toast.makeText(requireContext(), "Please enter a post description", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mAuth.getCurrentUser() == null) {
                        Log.e(TAG, "No authenticated user found");
                        Toast.makeText(requireContext(), "Please log in to create a post", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String userId = mAuth.getCurrentUser().getUid();
                    Log.d(TAG, "Fetching farmer document for userId: " + userId);
                    db.collection("farmers").document(userId).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (!documentSnapshot.exists()) {
                                    Log.e(TAG, "Farmer document not found for userId: " + userId);
                                    Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                String userName = documentSnapshot.getString("Name");
                                if (userName == null) {
                                    Log.e(TAG, "Name field is null for userId: " + userId);
                                    userName = "Anonymous";
                                }
                                Log.d(TAG, "Creating post with userName: " + userName);
                                String postId = UUID.randomUUID().toString();
                                Post post = new Post(postId, userId, userName, description, new Date(), 0, 0);
                                db.collection("posts").document(postId)
                                        .set(post)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Post created: " + postId);
                                            Toast.makeText(requireContext(), "Post created", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Failed to create post: " + e.getMessage(), e);
                                            Toast.makeText(requireContext(), "Failed to create post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to fetch user data: " + e.getMessage(), e);
                                Toast.makeText(requireContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onEditPost(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_post, null);
        TextInputEditText editDescription = dialogView.findViewById(R.id.edit_post_description);
        editDescription.setText(post.getDescription());
        builder.setView(dialogView)
                .setTitle("Edit Post")
                .setPositiveButton("Save", (dialog, which) -> {
                    String newDescription = editDescription.getText().toString().trim();
                    if (!newDescription.isEmpty()) {
                        db.collection("posts").document(post.getPostId())
                                .update("description", newDescription)
                                .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Post updated", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to update post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDeletePost(Post post) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("posts").document(post.getPostId())
                            .delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Post deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to delete post: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}