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

import java.util.List;

public class CropAdapter extends RecyclerView.Adapter<CropAdapter.CropViewHolder> {

    private List<Crop> cropList;
    private boolean isAdmin;
    private AdminCropActionListener adminListener;

    public CropAdapter(List<Crop> cropList) {
        this(cropList, false, null);
    }

    public CropAdapter(List<Crop> cropList, boolean isAdmin, AdminCropActionListener adminListener) {
        this.cropList = cropList;
        this.isAdmin = isAdmin;
        this.adminListener = adminListener;
    }

    public void setAdminMode(boolean isAdmin) {
        this.isAdmin = isAdmin;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_crop, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {
        Crop crop = cropList.get(position);
        holder.cropNameTextView.setText(crop.getCropName());
        holder.priceTextView.setText(String.format("Rs.%.2f/kg", crop.getPrice()));
        holder.descriptionTextView.setText(crop.getDescription());
        holder.statusTextView.setText(crop.getStatus());

        if (isAdmin) {
            holder.editButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.selectButton.setVisibility(View.GONE);
            holder.editButton.setOnClickListener(v -> {
                if (adminListener != null) {
                    adminListener.onEdit(crop);
                }
            });
            holder.deleteButton.setOnClickListener(v -> {
                if (adminListener != null) {
                    adminListener.onDelete(crop);
                }
            });
        } else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.selectButton.setVisibility(View.VISIBLE);
            holder.selectButton.setText("View Details");
            holder.selectButton.setOnClickListener(v -> {
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
                NavController navController = Navigation.findNavController(holder.itemView);
                int currentDestinationId = navController.getCurrentDestination().getId();
                if (currentDestinationId == R.id.dashboardFragment) {
                    navController.navigate(R.id.action_dashboardFragment_to_userCropDetailsFragment, bundle);
                } else if (currentDestinationId == R.id.findCropsFragment) {
                    navController.navigate(R.id.action_findCropsFragment_to_cropDetailsFragment, bundle);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return cropList.size();
    }

    static class CropViewHolder extends RecyclerView.ViewHolder {
        TextView cropNameTextView, priceTextView, descriptionTextView, statusTextView;
        Button editButton, deleteButton, selectButton;

        CropViewHolder(@NonNull View itemView) {
            super(itemView);
            cropNameTextView = itemView.findViewById(R.id.cropNameTextView);
            priceTextView = itemView.findViewById(R.id.priceTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            selectButton = itemView.findViewById(R.id.selectButton);
        }
    }

    public interface AdminCropActionListener {
        void onEdit(Crop crop);
        void onDelete(Crop crop);
    }
}