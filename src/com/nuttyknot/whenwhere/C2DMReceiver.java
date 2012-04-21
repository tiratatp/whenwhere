package com.nuttyknot.whenwhere;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class C2DMReceiver extends BroadcastReceiver {
	private static final int HELLO_ID = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(
				"com.google.android.c2dm.intent.REGISTRATION")) {
			handleRegistration(context, intent);
		} else if (intent.getAction().equals(
				"com.google.android.c2dm.intent.RECEIVE")) {
			handleMessage(context, intent);
		}
	}

	private void handleRegistration(Context context, Intent intent) {
		String registration = intent.getStringExtra("registration_id");
		if (intent.getStringExtra("error") != null) {
			// Registration failed, should try again later.
			Log.d("c2dm", "registration failed");
			String error = intent.getStringExtra("error");
			if (error == "SERVICE_NOT_AVAILABLE") {
				Log.d("c2dm", "SERVICE_NOT_AVAILABLE");
			} else if (error == "ACCOUNT_MISSING") {
				Log.d("c2dm", "ACCOUNT_MISSING");
			} else if (error == "AUTHENTICATION_FAILED") {
				Log.d("c2dm", "AUTHENTICATION_FAILED");
			} else if (error == "TOO_MANY_REGISTRATIONS") {
				Log.d("c2dm", "TOO_MANY_REGISTRATIONS");
			} else if (error == "INVALID_SENDER") {
				Log.d("c2dm", "INVALID_SENDER");
			} else if (error == "PHONE_REGISTRATION_ERROR") {
				Log.d("c2dm", "PHONE_REGISTRATION_ERROR");
			}
		} else if (intent.getStringExtra("unregistered") != null) {
			// unregistration done, new messages from the authorized sender will
			// be rejected
			Log.d("c2dm", "unregistered");

		} else if (registration != null) {
			Log.d("c2dm", registration);
			Editor editor = context.getSharedPreferences("c2dmPref",
					Context.MODE_PRIVATE).edit();
			editor.putString("registrationKey", registration);
			editor.commit();
			// Send the registration ID to the 3rd party site that is sending
			// the messages.
			// This should be done in a separate thread.
			// When done, remember that all registration is done.
		}
	}

	private void handleMessage(Context context, Intent intent) {
		// Do whatever you want with the message
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) context
				.getSystemService(ns);

		int icon = R.drawable.ic_launcher;
		CharSequence tickerText = "Hello";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context appContext = context.getApplicationContext();
		CharSequence contentTitle = "My notification";
		CharSequence contentText = "Hello World!";
		Intent notificationIntent = new Intent(context,
				com.nuttyknot.whenwhere.webview.WebviewActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(appContext, contentTitle, contentText,
				contentIntent);

		mNotificationManager.notify(HELLO_ID, notification);

	}
}