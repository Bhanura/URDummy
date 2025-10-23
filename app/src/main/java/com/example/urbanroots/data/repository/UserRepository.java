package com.example.urbanroots.data.repository;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public interface UserRepository {
    void isAdmin(String userId, OnSuccessListener<Boolean> listener, OnFailureListener failureListener);
    void checkUserCrop(String userId, String cropId, OnSuccessListener<Boolean> listener, OnFailureListener failureListener);
}