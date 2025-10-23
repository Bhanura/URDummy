package com.example.urbanroots.business.services;

import com.example.urbanroots.data.repository.CropRepository;
import com.example.urbanroots.domain.models.Crop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class CropService {
    private final CropRepository cropRepository;

    public CropService(CropRepository cropRepository) {
        this.cropRepository = cropRepository;
    }

    public void addCrop(Crop crop, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic here (e.g., validate crop data)
        if (crop.getCropName() == null || crop.getCropName().isEmpty()) {
            failureListener.onFailure(new Exception("Crop name cannot be empty"));
            return;
        }
        cropRepository.addCrop(crop, listener, failureListener);
    }

    public void getCrops(OnSuccessListener<List<Crop>> listener, OnFailureListener failureListener) {
        cropRepository.getCrops(crops -> {
            // Add business logic (e.g., filter or sort crops)
            listener.onSuccess(crops);
        }, failureListener);
    }

    public void updateCrop(Crop crop, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate crop data)
        if (crop.getCropName() == null || crop.getCropName().isEmpty()) {
            failureListener.onFailure(new Exception("Crop name cannot be empty"));
            return;
        }
        cropRepository.updateCrop(crop, listener, failureListener);
    }

    public void deleteCrop(String cropId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic if needed
        if (cropId == null || cropId.isEmpty()) {
            failureListener.onFailure(new Exception("Crop ID cannot be empty"));
            return;
        }
        cropRepository.deleteCrop(cropId, listener, failureListener);
    }
}