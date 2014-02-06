package com.example.kakawatalk;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

	public class GcmIntentService extends IntentService {
	    public static final int NOTIFICATION_ID = 1;
		private static final String TAG ="GcmIntentService";
	    NotificationCompat.Builder builder;

	    public static RoomActivity roomActivity;
	    private NotificationManager mNotificationManager;
	    public GcmIntentService() {
	        super("GcmIntentService");
	    }

	    @Override
	    protected void onHandleIntent(Intent intent) {
	        Bundle extras = intent.getExtras();
	        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
	        // The getMessageType() intent parameter must be the intent you received
	        // in your BroadcastReceiver.
	        String messageType = gcm.getMessageType(intent);

	        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
	            /*
	             * Filter messages based on message type. Since it is likely that GCM
	             * will be extended in the future with new message types, just ignore
	             * any message types you're not interested in, or that you don't
	             * recognize.
	             */
	            if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
	                sendNotification("Send error: " + extras.toString());
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_DELETED.equals(messageType)) {
	                sendNotification("Deleted messages on server: " +
	                        extras.toString());
	            // If it's a regular GCM message, do some work.
	            } else if (GoogleCloudMessaging.
	                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
	            	if(roomActivity != null){
	            		roomActivity.getMessage();
	            	}
	                // Post notification of received message.
	                sendNotification(extras.getString("content"));
	                Log.i(TAG, "Received: " + extras.toString());
	            }
	        }
	        // Release the wake lock provided by the WakefulBroadcastReceiver.
	        GcmBroadcastReceiver.completeWakefulIntent(intent);
	    }

	    // Put the message into a notification and post it.
	    // This is just one simple example of what you might choose to do with
	    // a GCM message.
	    private void sendNotification(String msg) {
	        mNotificationManager = (NotificationManager)
	                this.getSystemService(Context.NOTIFICATION_SERVICE);

	        Intent intent = new Intent(this, RoomActivity.class);
	        intent.putExtra("name", MainActivity.names[0]);
	        intent.putExtra("room_id", 1);
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                intent, PendingIntent.FLAG_CANCEL_CURRENT);
	        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
	        NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(this)
	        .setSmallIcon(R.drawable.ic_stat_gcm)
	        .setContentTitle("���ڿӼ�~��")
	        .setStyle(new NotificationCompat.BigTextStyle()
	        .bigText(msg))
	        .setAutoCancel(true)
	        .setVibrate(new long[]{100, 700, 100, 200, 100, 400, 100, 300})
	        .setContentText(msg);

	        mBuilder.setContentIntent(contentIntent);
	        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	    }
	}
