package com.example.urbanroots.presentation.fragments;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.urbanroots.domain.models.Crop;
import com.example.urbanroots.presentation.adapters.CropAdapter;
import com.example.urbanroots.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.tasks.Tasks;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {

    private RecyclerView cropsRecyclerView;
    private CropAdapter cropAdapter;
    private List<Crop> cropList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView precipitationText, humidityText, windText, highLowText;
    private TextView day1Date, day1Temp, day2Date, day2Temp, day3Date, day3Temp;
    private ImageView day1Icon, day2Icon, day3Icon;
    private static final String API_KEY = "2b8c282772efd7b38ef0fb26d2daaa02"; // Replace with your API key
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        context = requireContext();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);


        // Save FCM token
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult();
                    // Save to farmers collection
                    db.collection("farmers").document(user.getUid())
                            .set(new HashMap<String, Object>() {{
                                put("fcmToken", token);
                            }}, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "Token saved for farmer: " + user.getUid()))
                            .addOnFailureListener(e -> Log.e("FCM", "Error saving token", e));
                } else {
                    Log.e("FCM", "Failed to get token", task.getException());
                }
            });
        } else {
            Log.e("FCM", "No user logged in");
            Toast.makeText(context, "Please log in to continue", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).navigate(R.id.action_dashboardFragment_to_loginFragment);
        }



        // Initialize existing views
        cropsRecyclerView = view.findViewById(R.id.cropsRecyclerView);
        cropsRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        cropList = new ArrayList<>();
        cropAdapter = new CropAdapter(cropList);
        cropsRecyclerView.setAdapter(cropAdapter);

        MaterialButton adminPanelButton = view.findViewById(R.id.adminPanelButton);
        MaterialButton addCropButton = view.findViewById(R.id.addCropButton);
        MaterialButton viewCalendarButton = view.findViewById(R.id.viewCalendarButton);

        // Initialize weather views
        precipitationText = view.findViewById(R.id.precipitationText);
        humidityText = view.findViewById(R.id.humidityText);
        windText = view.findViewById(R.id.windText);
        highLowText = view.findViewById(R.id.highLowText);
        day1Date = view.findViewById(R.id.day1Date);
        day1Temp = view.findViewById(R.id.day1Temp);
        day1Icon = view.findViewById(R.id.day1Icon);
        day2Date = view.findViewById(R.id.day2Date);
        day2Temp = view.findViewById(R.id.day2Temp);
        day2Icon = view.findViewById(R.id.day2Icon);
        day3Date = view.findViewById(R.id.day3Date);
        day3Temp = view.findViewById(R.id.day3Temp);
        day3Icon = view.findViewById(R.id.day3Icon);

        // Admin check and button handling
        isAdmin(isAdmin -> {
            if (isAdmin) {
                adminPanelButton.setVisibility(View.VISIBLE);
                addCropButton.setVisibility(View.GONE);
                adminPanelButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_dashboardFragment_to_adminFragment);
                });
            } else {
                adminPanelButton.setVisibility(View.GONE);
                addCropButton.setVisibility(View.VISIBLE);
                addCropButton.setOnClickListener(v -> {
                    NavController navController = Navigation.findNavController(v);
                    navController.navigate(R.id.action_dashboardFragment_to_findCropsFragment);
                });
            }
        });

        // Handle calendar button click
        viewCalendarButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.action_dashboardFragment_to_calendarFragment);
        });

        // Fetch crops and weather
        fetchUserCrops(view);
        fetchUserLocation();

        return view;
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

    private void fetchUserCrops(View view) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(context, "Please log in to view your crops", Toast.LENGTH_SHORT).show();
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_dashboardFragment_to_loginFragment);
            return;
        }

        db.collection("user_crops")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cropList.clear();
                    List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String cropId = document.getString("cropId");
                        Long timestamp = document.getLong("timestamp");
                        if (cropId != null) {
                            tasks.add(db.collection("crops").document(cropId).get()
                                    .addOnSuccessListener(cropDoc -> {
                                        if (cropDoc.exists()) {
                                            Crop crop = cropDoc.toObject(Crop.class);
                                            if (crop != null) {
                                                crop.setCropId(cropDoc.getId());
                                                crop.setTimestamp(timestamp); // Set timestamp from user_crops
                                                cropList.add(crop);
                                            }
                                        }
                                    }));
                        }
                    }

                    Tasks.whenAllSuccess(tasks).addOnSuccessListener(documentSnapshots -> {
                        if (isAdded()) {
                            cropAdapter.notifyDataSetChanged();
                            if (cropList.isEmpty()) {
                                Toast.makeText(context, "No valid crops found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(e -> {
                        if (isAdded()) {
                            Toast.makeText(context, "Error fetching crop details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(context, "Error fetching your crops: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchUserLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                // Restrict to Sri Lanka (approximate bounds: lat 5.5 to 9.9, lon 79.5 to 82.0)
                if (location.getLatitude() >= 5.5 && location.getLatitude() <= 9.9 &&
                        location.getLongitude() >= 79.5 && location.getLongitude() <= 82.0) {
                    fetchWeatherData(location.getLatitude(), location.getLongitude());
                } else {
                    Toast.makeText(context, "Location outside Sri Lanka", Toast.LENGTH_SHORT).show();
                    // Fallback to Colombo coordinates
                    fetchWeatherData(6.9271, 79.8612);
                }
            } else {
                Toast.makeText(context, "Unable to get location", Toast.LENGTH_SHORT).show();
                // Fallback to Colombo coordinates
                fetchWeatherData(6.9271, 79.8612);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            // Fallback to Colombo coordinates
            fetchWeatherData(6.9271, 79.8612);
        });
    }
//    String url = "https://api.openweathermap.org/data/3.0/onecall?lat=6.9271&lon=79.8612&exclude=minutely,hourly,alerts&appid=2b8c282772efd7b38ef0fb26d2daaa02&units=metric";
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchUserLocation();
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show();
                // Fallback to Colombo coordinates
                fetchWeatherData(6.9271, 79.8612);
            }
        }
    }
//        String url = "https://api.openweathermap.org/data/3.0/onecall?lat=6.9271&lon=79.8612&exclude=minutely,hourly,alerts&appid=2b8c282772efd7b38ef0fb26d2daaa02&units=metric";
    private void fetchWeatherData(double lat, double lon) {
//        String url = "https://api.openweathermap.org/data/3.0/onecall?lat=6.9271&lon=79.8612&exclude=minutely,hourly,alerts&appid=2b8c282772efd7b38ef0fb26d2daaa02&units=metric";
        String url = "https://api.openweathermap.org/data/3.0/onecall?lat=" + lat + "&lon=" + lon + "&exclude=minutely,hourly,alerts&appid=" + API_KEY + "&units=metric";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(context, "Error fetching weather: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, "Error fetching weather data", Toast.LENGTH_SHORT).show());
                    }
                    return;
                }

                String responseData = response.body().string();
                try {
                    JSONObject json = new JSONObject(responseData);

                    // Today's weather
                    JSONObject current = json.getJSONObject("current");
                    double precipitation = current.has("rain") ? current.getJSONObject("rain").optDouble("1h", 0) : 0;
                    int humidity = current.getInt("humidity");
                    double windSpeed = current.getDouble("wind_speed");
                    double tempMax = current.getDouble("temp");
                    double tempMin = current.getDouble("feels_like"); // Approximation for low
                    String weatherCondition = current.getJSONArray("weather").getJSONObject(0).getString("main");

                    // 3-day forecast
                    JSONArray daily = json.getJSONArray("daily");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

                    requireActivity().runOnUiThread(() -> {
                        // Update today's weather
                        precipitationText.setText(String.format(Locale.getDefault(), "Precipitation: %.1f mm", precipitation));
                        humidityText.setText(String.format(Locale.getDefault(), "Humidity: %d%%", humidity));
                        windText.setText(String.format(Locale.getDefault(), "Wind: %.1f km/h", windSpeed * 3.6)); // Convert m/s to km/h
                        highLowText.setText(String.format(Locale.getDefault(), "H: %.1f°C / L: %.1f°C", tempMax, tempMin));

                        // Update 3-day forecast
                        for (int i = 1; i <= 3; i++) {
                            try {
                                JSONObject day = daily.getJSONObject(i);
                                long dt = day.getLong("dt") * 1000;
                                String date = dateFormat.format(new Date(dt));
                                double dayTemp = day.getJSONObject("temp").getDouble("day");
                                String dayCondition = day.getJSONArray("weather").getJSONObject(0).getString("main");

                                if (i == 1) {
                                    day1Date.setText(date);
                                    day1Temp.setText(String.format(Locale.getDefault(), "%.1f°C", dayTemp));
                                    day1Icon.setImageResource(getWeatherIcon(dayCondition));
                                } else if (i == 2) {
                                    day2Date.setText(date);
                                    day2Temp.setText(String.format(Locale.getDefault(), "%.1f°C", dayTemp));
                                    day2Icon.setImageResource(getWeatherIcon(dayCondition));
                                } else if (i == 3) {
                                    day3Date.setText(date);
                                    day3Temp.setText(String.format(Locale.getDefault(), "%.1f°C", dayTemp));
                                    day3Icon.setImageResource(getWeatherIcon(dayCondition));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (Exception e) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(context, "Error parsing weather data", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private int getWeatherIcon(String condition) {
        switch (condition.toLowerCase()) {
            case "rain":
                return R.drawable.ic_rain;
            case "clouds":
                return R.drawable.ic_cloudy;
            case "clear":
                return R.drawable.ic_sunny;
            default:
                return R.drawable.ic_weather_placeholder;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }
}