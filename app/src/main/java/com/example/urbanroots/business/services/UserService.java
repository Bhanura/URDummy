package com.example.urbanroots.business.services;

import com.example.urbanroots.data.repository.UserRepository;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void isAdmin(String userId, OnSuccessListener<Boolean> listener, OnFailureListener failureListener) {
        if (userId == null || userId.isEmpty()) {
            failureListener.onFailure(new Exception("User ID cannot be empty"));
            return;
        }
        userRepository.isAdmin(userId, listener, failureListener);
    }

    public void checkUserCrop(String userId, String cropId, OnSuccessListener<Boolean> listener, OnFailureListener failureListener) {
        if (userId == null || userId.isEmpty()) {
            failureListener.onFailure(new Exception("User ID cannot be empty"));
            return;
        }
        if (cropId == null || cropId.isEmpty()) {
            failureListener.onFailure(new Exception("Crop ID cannot be empty"));
            return;
        }
        userRepository.checkUserCrop(userId, cropId, listener, failureListener);
    }
}