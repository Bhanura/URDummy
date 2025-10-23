package com.example.urbanroots.data.firebase;

import com.example.urbanroots.data.repository.PostRepository;
import com.example.urbanroots.domain.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebasePostRepository implements PostRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "posts";

    public FirebasePostRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void addPost(Post post, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(post.getPostId())
                .set(post)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void getPosts(OnSuccessListener<List<Post>> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Post> posts = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        posts.add(document.toObject(Post.class));
                    }
                    listener.onSuccess(posts);
                })
                .addOnFailureListener(failureListener);
    }

    @Override
    public void updatePost(Post post, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(post.getPostId())
                .set(post)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void deletePost(String postId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(postId)
                .delete()
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }
}