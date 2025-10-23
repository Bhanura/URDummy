package com.example.urbanroots.presentation.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbanroots.domain.models.Crop;
import com.example.urbanroots.presentation.adapters.CropAdapter;
import com.example.urbanroots.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FindCropsFragment extends Fragment implements CropAdapter.AdminCropActionListener {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView cropsRecyclerView;
    private CropAdapter cropAdapter;
    private List<Crop> cropList;
    private Context context;
    private boolean isAdmin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_find_crops, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = requireContext();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cropsRecyclerView = view.findViewById(R.id.cropsRecyclerView);
        cropsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cropList = new ArrayList<>();
        cropAdapter = new CropAdapter(cropList, isAdmin, this);
        cropsRecyclerView.setAdapter(cropAdapter);

        // Check admin status
        isAdmin(isAdminResult -> {
            isAdmin = isAdminResult;
            cropAdapter.setAdminMode(isAdmin);
            fetchCrops();
        });
    }

    private void isAdmin(OnAdminCheckListener listener) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            listener.onResult(false);
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences("UrbanRoots", Context.MODE_PRIVATE);
        boolean cachedAdmin = prefs.getBoolean("isAdmin_" + user.getUid(), false);
        if (cachedAdmin) {
            listener.onResult(true);
            return;
        }
        db.collection("admins").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isAdmin = documentSnapshot.exists();
                    prefs.edit().putBoolean("isAdmin_" + user.getUid(), isAdmin).apply();
                    listener.onResult(isAdmin);
                })
                .addOnFailureListener(e -> listener.onResult(false));
    }

    private interface OnAdminCheckListener {
        void onResult(boolean isAdmin);
    }

    private void fetchCrops() {
        db.collection("crops").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cropList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Crop crop = document.toObject(Crop.class);
                        crop.setCropId(document.getId());
                        cropList.add(crop);
                    }
                    cropAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error fetching crops: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onEdit(Crop crop) {
        if (isAdmin) {
            Bundle bundle = new Bundle();
            bundle.putString("cropId", crop.getCropId());
            bundle.putString("cropName", crop.getCropName());
            bundle.putDouble("price", crop.getPrice());
            bundle.putString("description", crop.getDescription());
            bundle.putString("status", crop.getStatus());
            bundle.putString("soilType", crop.getSoilType());
            bundle.putString("sunlightHours", crop.getSunlightHours());
            bundle.putString("wateringInterval", crop.getWateringInterval());
            bundle.putString("harvestTime", crop.getHarvestTime());
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_findCropsFragment_to_adminFragment, bundle);
        }
    }

    @Override
    public void onDelete(Crop crop) {
        if (isAdmin) {
            db.collection("seeds").whereEqualTo("cropId", crop.getCropId()).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            if (isAdded()) {
                                Toast.makeText(context, "Cannot delete crop with associated seeds", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            db.collection("crops").document(crop.getCropId()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        if (isAdded()) {
                                            Toast.makeText(context, "Crop deleted successfully", Toast.LENGTH_SHORT).show();
                                            fetchCrops();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        if (isAdded()) {
                                            Toast.makeText(context, "Error deleting crop: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(context, "Error checking dependencies: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}