package com.example.urbanroots.data.repository;

import com.example.urbanroots.domain.models.CareTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public interface CareTaskRepository {
    void addCareTask(CareTask careTask, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void getCareTasks(OnSuccessListener<List<CareTask>> listener, OnFailureListener failureListener);
    void updateCareTask(CareTask careTask, OnSuccessListener<Void> listener, OnFailureListener failureListener);
    void deleteCareTask(String taskId, OnSuccessListener<Void> listener, OnFailureListener failureListener);
}