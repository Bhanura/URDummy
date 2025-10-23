package com.example.urbanroots.domain.models;

import com.google.firebase.firestore.PropertyName;

import java.util.Date;

public class Post {
    @PropertyName("postId")
    private String postId;
    @PropertyName("userId")
    private String userId;
    @PropertyName("userName")
    private String userName;
    @PropertyName("description")
    private String description;
    @PropertyName("timestamp")
    private Date timestamp;
    @PropertyName("likeCount")
    private int likeCount;
    @PropertyName("commentCount")
    private int commentCount;

    public Post() {
        // Required for Firestore
    }

    public Post(String postId, String userId, String userName, String description, Date timestamp, int likeCount, int commentCount) {
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.description = description;
        this.timestamp = timestamp;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    // Getters and setters
    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
}