package com.example.urbanroots.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;

import com.example.urbanroots.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationsFragment extends Fragment {
    private TextView notificationTextView;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        notificationTextView = view.findViewById(R.id.notification_text);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Replace with actual document ID (e.g., "1Wc4IPjvLKVYF8bvL1j0")
        String documentId = "1Wc4IPjvLKVYF8bvL1j0"; // You can pass this dynamically if needed

        // Fetch the document from Firestore
        fetchNextWateringTimestamp(documentId);

        return view;
    }

    private void fetchNextWateringTimestamp(String documentId) {
        DocumentReference docRef = db.collection("user_crops").document(documentId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Check if nextWateringTimestamp exists in the document
                if (documentSnapshot.contains("nextWateringTimestamp")) {
                    // Get the timestamp (assuming it's stored as a Firestore Timestamp)
                    com.google.firebase.Timestamp timestamp = documentSnapshot.getTimestamp("nextWateringTimestamp");
                    if (timestamp != null) {
                        // Convert Firestore Timestamp to Date
                        Date date = timestamp.toDate();
                        // Format the date
                        String formattedDate = new SimpleDateFormat("MMMM dd, yyyy 'at' h:mm:ss a", Locale.getDefault())
                                .format(date);
                        // Update the TextView
                        notificationTextView.setText("Next watering: " + formattedDate);
                    } else {
                        notificationTextView.setText("Next watering timestamp is null");
                    }
                } else {
                    // Field does not exist
                    notificationTextView.setText("No watering schedule available");
                }
            } else {
                // Document does not exist
                notificationTextView.setText("Crop data not found");
            }
        }).addOnFailureListener(e -> {
            // Handle errors (e.g., network issues)
            notificationTextView.setText("Error fetching data: " + e.getMessage());
        });
    }

    // Handle FCM notifications (unchanged)
    public void updateNotification(String title, String body) {
        notificationTextView.setText(title + ": " + body);
    }
}