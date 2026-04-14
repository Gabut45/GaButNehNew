package com.ujicoba.gabut.noti;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Handle message here
        String title = "FCM Message";
        String body = "No message body";

        // Jika ada notifikasi payload (remoteMessage.getNotification())
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        // Atau jika ada data payload (remoteMessage.getData())
        else if (remoteMessage.getData().size() > 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
        }

        // Tampilkan notifikasi pakai NotificationUtils
        NotificationUtils.showNotification(this, 1, title, body);
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        // TODO: Kirim token ini ke server kamu kalau perlu.
    }
}
