package com.example.urbanroots.data.firebase;

import com.example.urbanroots.data.repository.CareTaskRepository;
import com.example.urbanroots.domain.models.CareTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FirebaseCareTaskRepository implements CareTaskRepository {
    private final FirebaseFirestore db;
    private static final String COLLECTION_NAME = "care_tasks";

    public FirebaseCareTaskRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void addCareTask(CareTask careTask, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(careTask.getTaskId())
                .set(careTask)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void getCareTasks(OnSuccessListener<List<CareTask>> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<CareTask> careTasks = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        careTasks.add(document.toObject(CareTask.class));
                    }
                    listener.onSuccess(careTasks);
                })
                .addOnFailureListener(failureListener);
    }

    @Override
    public void updateCareTask(CareTask careTask, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(careTask.getTaskId())
                .set(careTask)
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }

    @Override
    public void deleteCareTask(String taskId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        db.collection(COLLECTION_NAME)
                .document(taskId)
                .delete()
                .addOnSuccessListener(listener)
                .addOnFailureListener(failureListener);
    }
}