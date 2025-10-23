package com.example.urbanroots.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbanroots.domain.models.CareTask;
import com.example.urbanroots.presentation.adapters.CareTaskAdapter;
import com.example.urbanroots.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment implements CareTaskAdapter.OnCareTaskClickListener {

    private CalendarView calendarView;
    private RecyclerView tasksRecyclerView;
    private CareTaskAdapter careTaskAdapter;
    private List<CareTask> careTaskList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        context = requireContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        calendarView = view.findViewById(R.id.calendarView);
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        careTaskList = new ArrayList<>();
        careTaskAdapter = new CareTaskAdapter(careTaskList, this);
        tasksRecyclerView.setAdapter(careTaskAdapter);

        // Set today's date as default
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        fetchCareTasksForDate(todayDate);

        // Handle date selection
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            fetchCareTasksForDate(selectedDate);
        });

        return view;
    }

    private void fetchCareTasksForDate(String date) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in to view tasks", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("care_tasks")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("dueDate", date)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    careTaskList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CareTask careTask = document.toObject(CareTask.class);
                        careTask.setTaskId(document.getId());
                        careTaskList.add(careTask);
                    }
                    careTaskAdapter.notifyDataSetChanged();
                    if (careTaskList.isEmpty()) {
                        Toast.makeText(context, "No tasks for " + date, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error fetching tasks: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onMarkAsDone(CareTask careTask) {
        db.collection("care_tasks").document(careTask.getTaskId())
                .update("currentStatus", "Done")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Task marked as Done", Toast.LENGTH_SHORT).show();
                    careTask.setCurrentStatus("Done");
                    careTaskAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to update task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}