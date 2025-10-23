package com.example.urbanroots.business.services;

import com.example.urbanroots.data.repository.PostRepository;
import com.example.urbanroots.domain.models.Post;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class PostService {
    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void addPost(Post post, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate post data)
        if (post.getPostId() == null || post.getPostId().isEmpty()) {
            failureListener.onFailure(new Exception("Post ID cannot be empty"));
            return;
        }
        if (post.getDescription() == null || post.getDescription().isEmpty()) {
            failureListener.onFailure(new Exception("Post description cannot be empty"));
            return;
        }
        postRepository.addPost(post, listener, failureListener);
    }

    public void getPosts(OnSuccessListener<List<Post>> listener, OnFailureListener failureListener) {
        postRepository.getPosts(posts -> {
            // Add business logic (e.g., filter or sort posts)
            listener.onSuccess(posts);
        }, failureListener);
    }

    public void updatePost(Post post, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate post data)
        if (post.getPostId() == null || post.getPostId().isEmpty()) {
            failureListener.onFailure(new Exception("Post ID cannot be empty"));
            return;
        }
        if (post.getDescription() == null || post.getDescription().isEmpty()) {
            failureListener.onFailure(new Exception("Post description cannot be empty"));
            return;
        }
        postRepository.updatePost(post, listener, failureListener);
    }

    public void deletePost(String postId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic if needed
        if (postId == null || postId.isEmpty()) {
            failureListener.onFailure(new Exception("Post ID cannot be empty"));
            return;
        }
        postRepository.deletePost(postId, listener, failureListener);
    }
}