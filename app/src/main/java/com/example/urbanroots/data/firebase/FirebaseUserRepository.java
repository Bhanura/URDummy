package com.example.urbanroots.data.firebase;

import com.example.urbanroots.data.repository.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseUserRepository implements UserRepository {
    private final FirebaseFirestore db;
    private static final String ADMINS_COLLECTION = "admins";
    private static final String USER_CROPS_COLLECTION = "user_crops";

    public FirebaseUserRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void isAdmin(String userId, OnSuccessListener<Boolean> listener, OnFailureListener failureListener) {
        db.collection(ADMINS_COLLECTION)
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> listener.onSuccess(documentSnapshot.exists()))
                .addOnFailureListener(failureListener);
    }

    @Override
    public void checkUserCrop(String userId, String cropId, OnSuccessListener<Boolean> listener, OnFailureListener failureListener) {
        db.collection(USER_CROPS_COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("cropId", cropId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> listener.onSuccess(!queryDocumentSnapshots.isEmpty()))
                .addOnFailureListener(failureListener);
    }
}