package com.example.urbanroots.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.urbanroots.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView nameText, emailText, locationText, farmerIdText;
    private ProgressBar progressBar;
    private Context context;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Inflating fragment_profile");
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: Initializing ProfileFragment");

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI elements
        nameText = view.findViewById(R.id.name_text);
        emailText = view.findViewById(R.id.email_text);
        locationText = view.findViewById(R.id.location_text);
        farmerIdText = view.findViewById(R.id.farmer_id_text);
        progressBar = view.findViewById(R.id.progress_bar);

        // Edit Profile Button
        MaterialButton editProfileButton = view.findViewById(R.id.edit_profile_button);
        if (editProfileButton != null) {
            editProfileButton.setOnClickListener(v -> {
                Log.d(TAG, "Navigating to EditProfileFragment");
                NavController navController = Navigation.findNavController(view);
                navController.navigate(R.id.action_profileFragment_to_editProfileFragment);
            });
        }

        // Fetch and display profile data
        loadProfileData();
    }

    private void loadProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "loadProfileData: No user logged in");
            if (isAdded()) {
                Toast.makeText(context, "User not logged in", Toast.LENGTH_LONG).show();
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_profileFragment_to_loginFragment);
            }
            return;
        }

        String userEmail = currentUser.getEmail();
        Log.d(TAG, "loadProfileData: Fetching data for email: " + userEmail);
        emailText.setText(userEmail != null ? userEmail : "N/A");

        progressBar.setVisibility(View.VISIBLE);

        // Query Firestore for the user's profile data
        db.collection("farmers")
                .whereEqualTo("Email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    progressBar.setVisibility(View.GONE);
                    Log.d(TAG, "Firestore query completed. Documents found: " + queryDocumentSnapshots.size());
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming one document per user email
                        QuerySnapshot snapshot = queryDocumentSnapshots;
                        String name = snapshot.getDocuments().get(0).getString("Name");
                        String location = snapshot.getDocuments().get(0).getString("Location");
                        String farmerId = snapshot.getDocuments().get(0).getString("FarmerId");

                        Log.d(TAG, "Data retrieved - Name: " + name + ", Location: " + location + ", FarmerId: " + farmerId);
                        nameText.setText(name != null ? name : "N/A");
                        locationText.setText(location != null ? location : "N/A");
                        farmerIdText.setText(farmerId != null ? farmerId : "N/A");
                    } else {
                        Log.w(TAG, "No profile data found for email: " + userEmail);
                        if (isAdded()) {
                            Toast.makeText(context, "Profile data not found", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Error fetching profile data: ", e);
                    if (isAdded()) {
                        Toast.makeText(context, "Failed to load profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: Cleaning up");
        context = null; // Prevent memory leaks
    }
}