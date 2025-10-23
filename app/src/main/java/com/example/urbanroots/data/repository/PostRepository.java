package com.example.urbanroots.data.repository;

import com.example.urbanroots.domain.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public interface PostRepository {
    void addPost(Post post, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void getPosts(OnSuccessListener<List<Post>> listener, OnFailureListener failureListener);
    void updatePost(Post post, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void deletePost(String postId, OnSuccessListener<Void> listener, OnFailureListener failureListener);
}