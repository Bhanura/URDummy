package com.example.urbanroots.data.firebase;

import com.example.urbanroots.data.repository.CommentRepository;
import com.example.urbanroots.domain.models.Comment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCommentRepository implements CommentRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "comments";

    public FirebaseCommentRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void addComment(Comment comment, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(comment.getCommentId())
                .set(comment)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void getComments(OnSuccessListener<List<Comment>> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Comment> comments = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        comments.add(document.toObject(Comment.class));
                    }
                    listener.onSuccess(comments);
                })
                .addOnFailureListener(failureListener);
    }

    @Override
    public void updateComment(Comment comment, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(comment.getCommentId())
                .set(comment)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void deleteComment(String commentId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(commentId)
                .delete()
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }
}