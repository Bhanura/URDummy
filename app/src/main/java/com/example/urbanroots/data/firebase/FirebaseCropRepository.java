package com.example.urbanroots.data.firebase;

import com.example.urbanroots.data.repository.CropRepository;
import com.example.urbanroots.domain.models.Crop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCropRepository implements CropRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "crops";

    public FirebaseCropRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void addCrop(Crop crop, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(crop.getCropId())
                .set(crop)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void getCrops(OnSuccessListener<List<Crop>> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Crop> crops = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        crops.add(document.toObject(Crop.class));
                    }
                    listener.onSuccess(crops);
                })
                .addOnFailureListener(failureListener);
    }

    @Override
    public void updateCrop(Crop crop, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(crop.getCropId())
                .set(crop)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void deleteCrop(String cropId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(cropId)
                .delete()
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }
}