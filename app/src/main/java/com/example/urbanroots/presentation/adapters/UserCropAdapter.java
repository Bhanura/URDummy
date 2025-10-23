package com.example.urbanroots.presentation.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbanroots.domain.models.Crop;
import com.example.urbanroots.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class UserCropAdapter extends RecyclerView.Adapter<UserCropAdapter.UserCropViewHolder> {

    private List<DocumentSnapshot> userCropList;
    private FirebaseFirestore db;

    public UserCropAdapter(List<DocumentSnapshot> userCropList) {
        this.userCropList = userCropList;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public UserCropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_crop, parent, false);
        return new UserCropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserCropViewHolder holder, int position) {
        DocumentSnapshot userCropDoc = userCropList.get(position);
        String cropId = userCropDoc.getString("cropId");

        // Fetch crop details from crops collection
        db.collection("crops").document(cropId).get()
                .addOnSuccessListener(cropDoc -> {
                    if (cropDoc.exists()) {
                        Crop crop = cropDoc.toObject(Crop.class);
                        if (crop != null) {
                            crop.setCropId(cropDoc.getId());
                            holder.cropNameTextView.setText(crop.getCropName());
                            holder.priceTextView.setText(String.format("Rs.%.2f/kg", crop.getPrice()));
                            holder.descriptionTextView.setText(crop.getDescription());
                            holder.statusTextView.setText(crop.getStatus());

                            // Pass crop details and timestamps to UserCropDetailsFragment
                            holder.viewDetailsButton.setOnClickListener(v -> {
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
                                bundle.putLong("plantedTimestamp", userCropDoc.getLong("plantedTimestamp") != null ? userCropDoc.getLong("plantedTimestamp") : 0L);
                                bundle.putLong("timestamp", userCropDoc.getLong("timestamp") != null ? userCropDoc.getLong("timestamp") : 0L);
                                NavController navController = Navigation.findNavController(holder.itemView);
                                navController.navigate(R.id.action_dashboardFragment_to_userCropDetailsFragment, bundle);
                            });
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return userCropList.size();
    }

    static class UserCropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView, priceTextView, descriptionTextView, statusTextView;
        Button viewDetailsButton;

        UserCropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}