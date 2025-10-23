package com.example.urbanroots.presentation.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.urbanroots.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserCropDetailsFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView cropNameTextView, plantedDateTextView, expectedHarvestTextView, wateringIntervalTextView,
            descriptionTextView, soilTypeTextView, sunlightHoursTextView, purchaseDateTextView;
    private MaterialButton plantNowButton;
    private ProgressBar progressBar;
    private Context context;
    private String cropId, userCropDocId;
    private Long plantedTimestamp, purchaseTimestamp;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_crop_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cropNameTextView = view.findViewById(R.id.cropNameTextView);
        plantedDateTextView = view.findViewById(R.id.plantedDateTextView);
        expectedHarvestTextView = view.findViewById(R.id.expectedHarvestTextView);
        wateringIntervalTextView = view.findViewById(R.id.wateringIntervalTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        soilTypeTextView = view.findViewById(R.id.soilTypeTextView);
        sunlightHoursTextView = view.findViewById(R.id.sunlightHoursTextView);
        purchaseDateTextView = view.findViewById(R.id.purchaseDateTextView);
        plantNowButton = view.findViewById(R.id.plantNowButton);
        progressBar = view.findViewById(R.id.progressBar);

        Bundle args = getArguments();
        if (args != null && args.containsKey("cropId")) {
            cropId = args.getString("cropId");
            cropNameTextView.setText(args.getString("cropName"));
            descriptionTextView.setText(String.format("Description: %s", args.getString("description")));
            soilTypeTextView.setText(String.format("Soil Type: %s", args.getString("soilType")));
            sunlightHoursTextView.setText(String.format("Sunlight: %s", args.getString("sunlightHours")));
            wateringIntervalTextView.setText(String.format("Watering: %s", args.getString("wateringInterval")));
            purchaseTimestamp = args.getLong("timestamp", 0L);

            // Fetch userCropDocId and plantedTimestamp from Firestore
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                db.collection("user_crops")
                        .whereEqualTo("userId", user.getUid())
                        .whereEqualTo("cropId", cropId)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                userCropDocId = queryDocumentSnapshots.getDocuments().get(0).getId();
                                // Get plantedTimestamp from Firestore, not Bundle
                                plantedTimestamp = queryDocumentSnapshots.getDocuments().get(0).getLong("plantedTimestamp");
                            }
                            updateUI(user);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Error fetching crop data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        });
            } else {
                updateUI(null);
            }
        } else {
            Toast.makeText(context, "No crop data provided", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
        }

        plantNowButton.setOnClickListener(v -> plantCrop());
    }

    private void updateUI(FirebaseUser user) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        if (plantedTimestamp != null && plantedTimestamp > 0) {
            plantedDateTextView.setText(String.format("Planted Date: %s", dateFormat.format(plantedTimestamp)));
            plantNowButton.setVisibility(View.GONE);

            String harvestTime = getArguments().getString("harvestTime");
            if (harvestTime != null && harvestTime.contains("days")) {
                try {
                    int days = Integer.parseInt(harvestTime.replaceAll("[^0-9]", ""));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(plantedTimestamp);
                    calendar.add(Calendar.DAY_OF_YEAR, days);
                    expectedHarvestTextView.setText(String.format("Expected Harvest: %s", dateFormat.format(calendar.getTime())));
                } catch (NumberFormatException e) {
                    expectedHarvestTextView.setText("Expected Harvest: Not available");
                }
            } else {
                expectedHarvestTextView.setText("Expected Harvest: Not available");
            }
        } else {
            plantedDateTextView.setText("Planted Date: Not planted");
            expectedHarvestTextView.setText("Expected Harvest: Not available");
            plantNowButton.setVisibility(user != null && userCropDocId != null ? View.VISIBLE : View.GONE);
        }

        if (purchaseTimestamp > 0) {
            purchaseDateTextView.setText(String.format("Purchase Date: %s", dateFormat.format(purchaseTimestamp)));
        } else {
            purchaseDateTextView.setText("Purchase Date: Not available");
        }
    }

    private void plantCrop() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || userCropDocId == null) {
            Toast.makeText(context, "Unable to plant crop", Toast.LENGTH_SHORT).show();
            return;
        }

        plantNowButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        long newTimestamp = System.currentTimeMillis();
        Map<String, Object> updates = new HashMap<>();
        updates.put("plantedTimestamp", newTimestamp);

        db.collection("user_crops").document(userCropDocId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    plantedTimestamp = newTimestamp;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    plantedDateTextView.setText(String.format("Planted Date: %s", dateFormat.format(plantedTimestamp)));
                    plantNowButton.setVisibility(View.GONE);

                    String harvestTime = getArguments().getString("harvestTime");
                    if (harvestTime != null && harvestTime.contains("days")) {
                        try {
                            int days = Integer.parseInt(harvestTime.replaceAll("[^0-9]", ""));
                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(plantedTimestamp);
                            calendar.add(Calendar.DAY_OF_YEAR, days);
                            expectedHarvestTextView.setText(String.format("Expected Harvest: %s", dateFormat.format(calendar.getTime())));
                        } catch (NumberFormatException e) {
                            expectedHarvestTextView.setText("Expected Harvest: Not available");
                        }
                    }

                    Toast.makeText(context, "Crop planted successfully!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error planting crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    plantNowButton.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}