package com.example.urbanroots.presentation.fragments;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.example.urbanroots.business.services.CropService;
import com.example.urbanroots.business.services.UserService;
import com.example.urbanroots.data.firebase.FirebaseCropRepository;
import com.example.urbanroots.data.firebase.FirebaseUserRepository;
import com.example.urbanroots.domain.models.Crop;
import com.example.urbanroots.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CropDetailsFragment extends Fragment {

    private CropService cropService;
    private UserService userService;
    private FirebaseAuth mAuth;
    private TextView cropNameTextView, priceTextView, price50gTextView, descriptionTextView,
            statusTextView, soilTypeTextView, sunlightHoursTextView, wateringIntervalTextView, harvestTimeTextView;
    private MaterialButton addToGardenButton;
    private ProgressBar progressBar;
    private Context context;
    private Crop crop;
    private boolean isAdmin;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize services
        cropService = new CropService(new FirebaseCropRepository());
        userService = new UserService(new FirebaseUserRepository());
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crop_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();

        // Initialize UI elements
        cropNameTextView = view.findViewById(R.id.cropNameTextView);
        priceTextView = view.findViewById(R.id.priceTextView);
        price50gTextView = view.findViewById(R.id.price50gTextView);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
        statusTextView = view.findViewById(R.id.statusTextView);
        soilTypeTextView = view.findViewById(R.id.soilTypeTextView);
        sunlightHoursTextView = view.findViewById(R.id.sunlightHoursTextView);
        wateringIntervalTextView = view.findViewById(R.id.wateringIntervalTextView);
        harvestTimeTextView = view.findViewById(R.id.harvestTimeTextView);
        addToGardenButton = view.findViewById(R.id.addToGardenButton);
        progressBar = view.findViewById(R.id.progressBar);

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

            cropNameTextView.setText(crop.getCropName());
            priceTextView.setText(String.format("Price: Rs.%.2f/kg", crop.getPrice()));
            price50gTextView.setText(String.format("Price for 50g: Rs.%.2f", crop.getPrice() * 0.05));
            descriptionTextView.setText(String.format("Description: %s", crop.getDescription()));
            statusTextView.setText(String.format("Status: %s", crop.getStatus()));
            soilTypeTextView.setText(String.format("Soil Type: %s", crop.getSoilType()));
            sunlightHoursTextView.setText(String.format("Sunlight: %s", crop.getSunlightHours()));
            wateringIntervalTextView.setText(String.format("Watering: %s", crop.getWateringInterval()));
            harvestTimeTextView.setText(String.format("Harvest Time: %s", crop.getHarvestTime()));
        } else {
            Toast.makeText(context, "No crop data provided", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigateUp();
            return;
        }

        // Check admin status using UserService
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            checkAdminStatus(user.getUid());
        } else {
            addToGardenButton.setVisibility(View.VISIBLE);
            addToGardenButton.setOnClickListener(v -> addToGarden());
        }
    }

    private void checkAdminStatus(String userId) {
        // Check cached admin status first
        SharedPreferences prefs = context.getSharedPreferences("UrbanRoots", Context.MODE_PRIVATE);
        boolean cachedAdmin = prefs.getBoolean("isAdmin_" + userId, false);
        if (cachedAdmin) {
            isAdmin = true;
            addToGardenButton.setVisibility(View.GONE);
            return;
        }

        // Use UserService to check admin status
        userService.isAdmin(userId,
                isAdminResult -> {
                    isAdmin = isAdminResult;
                    prefs.edit().putBoolean("isAdmin_" + userId, isAdmin).apply();
                    addToGardenButton.setVisibility(isAdmin ? View.GONE : View.VISIBLE);
                    if (!isAdmin) {
                        addToGardenButton.setOnClickListener(v -> addToGarden());
                    }
                },
                e -> {
                    Toast.makeText(context, "Error checking admin status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    addToGardenButton.setVisibility(View.VISIBLE);
                    addToGardenButton.setOnClickListener(v -> addToGarden());
                });
    }

    private void addToGarden() {
        if (crop == null) {
            Toast.makeText(context, "No crop selected", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "You must be logged in to add a crop", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).navigate(R.id.action_cropDetailsFragment_to_loginFragment);
            return;
        }

        addToGardenButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        // Check if the crop is already selected using UserService
        userService.checkUserCrop(user.getUid(), crop.getCropId(),
                exists -> {
                    if (isAdded()) {
                        if (exists) {
                            Toast.makeText(context, "Crop already added to your garden", Toast.LENGTH_SHORT).show();
                            addToGardenButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        } else {
                            // Navigate to PaymentFragment
                            Bundle args = new Bundle();
                            args.putString("cropId", crop.getCropId());
                            args.putString("cropName", crop.getCropName());
                            args.putDouble("price", crop.getPrice());
                            args.putString("description", crop.getDescription());
                            args.putString("status", crop.getStatus());
                            args.putString("soilType", crop.getSoilType());
                            args.putString("sunlightHours", crop.getSunlightHours());
                            args.putString("wateringInterval", crop.getWateringInterval());
                            args.putString("harvestTime", crop.getHarvestTime());
                            NavController navController = Navigation.findNavController(requireView());
                            navController.navigate(R.id.action_cropDetailsFragment_to_paymentFragment, args);
                            addToGardenButton.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                },
                e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error checking crop selection: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        addToGardenButton.setEnabled(true);
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}