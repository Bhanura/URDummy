package com.example.urbanroots.business.services;

import com.example.urbanroots.data.repository.CommentRepository;
import com.example.urbanroots.domain.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class CommentService {
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public void addComment(Comment comment, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate comment data)
        if (comment.getCommentId() == null || comment.getCommentId().isEmpty()) {
            failureListener.onFailure(new Exception("Comment ID cannot be empty"));
            return;
        }
        if (comment.getCommentText() == null || comment.getCommentText().isEmpty()) {
            failureListener.onFailure(new Exception("Comment text cannot be empty"));
            return;
        }
        commentRepository.addComment(comment, listener, failureListener);
    }

    public void getComments(OnSuccessListener<List<Comment>> listener, OnFailureListener failureListener) {
        commentRepository.getComments(comments -> {
            // Add business logic (e.g., filter or sort comments)
            listener.onSuccess(comments);
        }, failureListener);
    }

    public void updateComment(Comment comment, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate comment data)
        if (comment.getCommentId() == null || comment.getCommentId().isEmpty()) {
            failureListener.onFailure(new Exception("Comment ID cannot be empty"));
            return;
        }
        if (comment.getCommentText() == null || comment.getCommentText().isEmpty()) {
            failureListener.onFailure(new Exception("Comment text cannot be empty"));
            return;
        }
        commentRepository.updateComment(comment, listener, failureListener);
    }

    public void deleteComment(String commentId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic if needed
        if (commentId == null || commentId.isEmpty()) {
            failureListener.onFailure(new Exception("Comment ID cannot be empty"));
            return;
        }
        commentRepository.deleteComment(commentId, listener, failureListener);
    }
}