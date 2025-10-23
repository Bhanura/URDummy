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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.urbanroots.domain.models.Crop;
import com.example.urbanroots.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class PaymentFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView paymentCropName, paymentAmount;
    private TextInputEditText cardNumberEditText, cardholderNameEditText, expiryDateEditText, cvvEditText;
    private MaterialButton confirmPaymentButton;
    private ProgressBar paymentProgressBar;
    private Context context;
    private Crop crop;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        paymentCropName = view.findViewById(R.id.paymentCropName);
        paymentAmount = view.findViewById(R.id.paymentAmount);
        cardNumberEditText = view.findViewById(R.id.cardNumberEditText);
        cardholderNameEditText = view.findViewById(R.id.cardholderNameEditText);
        expiryDateEditText = view.findViewById(R.id.expiryDateEditText);
        cvvEditText = view.findViewById(R.id.cvvEditText);
        confirmPaymentButton = view.findViewById(R.id.confirmPaymentButton);
        paymentProgressBar = view.findViewById(R.id.paymentProgressBar);

        // Get crop data from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("cropId")) {
            crop = new Crop();
            crop.setCropId(args.getString("cropId"));
            crop.setCropName(args.getString("cropName"));
            crop.setPrice(args.getDouble("price"));
            crop.setDescription(args.getString("description"));
            crop.setStatus(args.getString("status"));
            crop.setSoilType(args.getString("soilType"));
            crop.setSunlightHours(args.getString("sunlightHours"));
            crop.setWateringInterval(args.getString("wateringInterval"));
            crop.setHarvestTime(args.getString("harvestTime"));

            paymentCropName.setText(String.format("Crop: %s", crop.getCropName()));
            paymentAmount.setText(String.format("Amount: $%.2f", crop.getPrice() * 0.05)); // Assuming 50g
        } else {
            Toast.makeText(context, "No crop data provided", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(view);
            navController.navigateUp();
            return;
        }

        confirmPaymentButton.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "You must be logged in to make a payment", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardNumber = cardNumberEditText.getText().toString().trim();
        String cardholderName = cardholderNameEditText.getText().toString().trim();
        String expiryDate = expiryDateEditText.getText().toString().trim();
        String cvv = cvvEditText.getText().toString().trim();

        // Basic validation (since it's fake, keep it simple)
        if (cardNumber.length() < 12 || cardholderName.isEmpty() || !expiryDate.matches("\\d{2}/\\d{2}") || cvv.length() != 3) {
            Toast.makeText(context, "Please enter valid payment details", Toast.LENGTH_SHORT).show();
            return;
        }

        confirmPaymentButton.setEnabled(false);
        paymentProgressBar.setVisibility(View.VISIBLE);

        // Save payment to Firebase
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("userId", user.getUid());
        paymentData.put("amount", crop.getPrice() * 0.05); // 50g price
        paymentData.put("paymentType", "Card");
        paymentData.put("info", "Purchase of " + crop.getCropName());
        paymentData.put("paymentDate", System.currentTimeMillis());
        paymentData.put("status", "Completed");

        db.collection("Payments").add(paymentData)
                .addOnSuccessListener(documentReference -> {
                    // Add crop to user_crops
                    addCropToUserCrops(user);
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Payment failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        confirmPaymentButton.setEnabled(true);
                        paymentProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void addCropToUserCrops(FirebaseUser user) {
        db.collection("user_crops")
                .whereEqualTo("userId", user.getUid())
                .whereEqualTo("cropId", crop.getCropId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        if (isAdded()) {
                            Toast.makeText(context, "Crop already added to your garden", Toast.LENGTH_SHORT).show();
                            confirmPaymentButton.setEnabled(true);
                            paymentProgressBar.setVisibility(View.GONE);
                            navigateToDashboard();
                        }
                    } else {
                        Map<String, Object> userCropData = new HashMap<>();
                        userCropData.put("userId", user.getUid());
                        userCropData.put("cropId", crop.getCropId());
                        userCropData.put("timestamp", System.currentTimeMillis());

                        db.collection("user_crops").add(userCropData)
                                .addOnSuccessListener(documentReference -> {
                                    if (isAdded()) {
                                        Toast.makeText(context, "Payment successful! Crop added to your garden", Toast.LENGTH_SHORT).show();
                                        navigateToDashboard();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) {
                                        Toast.makeText(context, "Error adding crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        confirmPaymentButton.setEnabled(true);
                                        paymentProgressBar.setVisibility(View.GONE);
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error checking crop selection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        confirmPaymentButton.setEnabled(true);
                        paymentProgressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void navigateToDashboard() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_paymentFragment_to_dashboardFragment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}