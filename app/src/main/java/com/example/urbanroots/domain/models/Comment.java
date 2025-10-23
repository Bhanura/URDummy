package com.example.urbanroots.domain.models;

import java.util.Date;

public class Comment {
    private String commentId;
    private String postId;
    private String userId;
    private String userName;
    private String commentText;
    private Date timestamp;

    public Comment() {
        // Required for Firestore
    }

    public Comment(String commentId, String postId, String userId, String userName, String commentText, Date timestamp) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.commentText = commentText;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getCommentText() { return commentText; }
    public void setCommentText(String commentText) { this.commentText = commentText; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}