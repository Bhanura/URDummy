package com.example.urbanroots.data.firebase;
// app/src/main/java/com/example/urbanroots/MyFirebaseMessagingService.java
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.example.urbanroots.R;
import com.example.urbanroots.presentation.activities.MainActivity;
import com.example.urbanroots.presentation.fragments.CropDetailsFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.HashMap;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String CHANNEL_ID = "UrbanRootsNotifications";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("FCM", "New token: " + token);
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;
        if (userId != null) {
            db.collection("farmers").document(userId)
                    .set(new HashMap<String, Object>() {{ put("fcmToken", token); }}, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> Log.d("FCM", "Token stored"))
                    .addOnFailureListener(e -> Log.e("FCM", "Error storing token", e));
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            String cropId = remoteMessage.getData().get("cropId");
            String action = remoteMessage.getData().get("action");
            sendNotification(title, body, cropId, action);
        }
    }

    private void sendNotification(String title, String messageBody, String cropId, String action) {
        Intent intent;
        if ("open_crop_details".equals(action) && cropId != null) {
            intent = new Intent(this, CropDetailsFragment.class); // Replace with your activity
            intent.putExtra("cropId", cropId);
        } else {
            intent = new Intent(this, MainActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // Replace with your icon
                .setContentTitle(title)
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "UrbanRoots Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}