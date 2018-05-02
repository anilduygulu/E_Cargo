package com.ecargo.ecargo_grid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by batintaskin on 28/12/16.
 */

public class FcmMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMsgService";
    private String messega,kisiid;
    JSONObject notification;

    public void onMessageReceived(RemoteMessage remoteMessage) {

     Map data = remoteMessage.getData();
/*        Log.d("Remote_Message_ToString",remoteMessage.getData().toString());
        messega = data.get("message").toString();
        kisiid = data.get("keyword").toString();

        Notibas(messega,kisiid);

        Log.d("Deneme",messega+kisiid);

*/

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData().toString());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        /*
        String title = "title";
        String message = remoteMessage.getNotification().getBody();
        */
        try {
            notification = new JSONObject(remoteMessage.getNotification().getBody());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String title = null;
        String message = null;
        try {
            title = notification.getString("title");
            message = notification.getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "FCMmessage: " + title + message);

      //  Log.d(TAG, "From: " + remoteMessage.getFrom());
        //Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

      //  Log.d(TAG, "FCMmessage: " + data);
      //  String kisiid = data.get("title").toString();
      // String  message = data.get("message").toString();



    Intent intent = new Intent(this,MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_ONE_SHOT);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
    notificationBuilder.setContentTitle(title);
    notificationBuilder.setContentText(message);
    notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);
    notificationBuilder.setAutoCancel(true);
    notificationBuilder.setContentIntent(pendingIntent);
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(0,notificationBuilder.build());


}
}
