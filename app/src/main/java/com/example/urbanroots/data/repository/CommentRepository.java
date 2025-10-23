package com.example.urbanroots.data.repository;

import com.example.urbanroots.domain.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public interface CommentRepository {
    void addComment(Comment comment, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void getComments(OnSuccessListener<List<Comment>> listener, OnFailureListener failureListener);
    void updateComment(Comment comment, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void deleteComment(String commentId, OnSuccessListener<Void> listener, OnFailureListener failureListener);
}