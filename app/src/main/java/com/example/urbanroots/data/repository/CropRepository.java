package com.example.urbanroots.data.repository;

import com.example.urbanroots.domain.models.Crop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public interface CropRepository {
    void addCrop(Crop crop, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void getCrops(OnSuccessListener<List<Crop>> listener, OnFailureListener failureListener);
    void updateCrop(Crop crop, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void deleteCrop(String cropId, OnSuccessListener<Void> listener, OnFailureListener failureListener);
}