package com.example.urbanroots.business.services;

import com.example.urbanroots.data.repository.CareTaskRepository;
import com.example.urbanroots.domain.models.CareTask;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class CareTaskService {
    private final CareTaskRepository careTaskRepository;

    public CareTaskService(CareTaskRepository careTaskRepository) {
        this.careTaskRepository = careTaskRepository;
    }

    public void addCareTask(CareTask careTask, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate care task data)
        if (careTask.getTaskId() == null || careTask.getTaskId().isEmpty()) {
            failureListener.onFailure(new Exception("Task ID cannot be empty"));
            return;
        }
        if (careTask.getType() == null || careTask.getType().isEmpty()) {
            failureListener.onFailure(new Exception("Task type cannot be empty"));
            return;
        }
        careTaskRepository.addCareTask(careTask, listener, failureListener);
    }

    public void getCareTasks(OnSuccessListener<List<CareTask>> listener, OnFailureListener failureListener) {
        careTaskRepository.getCareTasks(careTasks -> {
            // Add business logic (e.g., filter or sort tasks)
            listener.onSuccess(careTasks);
        }, failureListener);
    }

    public void updateCareTask(CareTask careTask, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic (e.g., validate care task data)
        if (careTask.getTaskId() == null || careTask.getTaskId().isEmpty()) {
            failureListener.onFailure(new Exception("Task ID cannot be empty"));
            return;
        }
        if (careTask.getType() == null || careTask.getType().isEmpty()) {
            failureListener.onFailure(new Exception("Task type cannot be empty"));
            return;
        }
        careTaskRepository.updateCareTask(careTask, listener, failureListener);
    }

    public void deleteCareTask(String taskId, OnSuccessListener<Void> listener, OnFailureListener failureListener) {
        // Add business logic if needed
        if (taskId == null || taskId.isEmpty()) {
            failureListener.onFailure(new Exception("Task ID cannot be empty"));
            return;
        }
        careTaskRepository.deleteCareTask(taskId, listener, failureListener);
    }
}