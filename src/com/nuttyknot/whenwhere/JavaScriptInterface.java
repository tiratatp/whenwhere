package com.nuttyknot.whenwhere;

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nuttyknot.whenwhere.WebviewActivity;

public class JavaScriptInterface {
	private Context mContext;
	private WebView browser;
	private Handler handler;

	private SharedPreferences mPrefs;
	private Facebook facebook = new Facebook("352831231424321");

	/** Instantiate the interface and set the context */
	JavaScriptInterface(Context c, WebView w, Handler h) {
		mContext = c;
		browser = w;
		handler = h;
	}

	/** Show a toast from the web page */
	public void showToast(String toast) {
		Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	}

	public void createWhere(int event_id) {
		Intent intent = new Intent(mContext, WhereActivity.class);
		intent.putExtra("event_id", event_id);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mContext.startActivity(intent);
	}

	public void createWhere() {
		Intent intent = new Intent(mContext, WhereActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		mContext.startActivity(intent);
	}

	public void loadPage(String in) {
		loadUrl("file:///android_asset/" + in);
	}

	public void loadUrl(final String in) {
		handler.post(new Runnable() {
			public void run() {
				browser.loadUrl(in);
			}
		});
	}

	public void submit(String name, String email, String action, String when,
			String event_id) {
		JSONObject jsonInput = new JSONObject();
		try {
			jsonInput = ((WebviewActivity) mContext).getStoredVariable();
			jsonInput.put("type", "rsvp");
			jsonInput.put("name", name);
			jsonInput.put("email", email);
			jsonInput.put("action", action);
			jsonInput.put("event_id", event_id);
			jsonInput.put("when", when);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// String url =
		// "https://mrstrountlyinhedgmadendi:mLRN14gNMLEoDqlaIvtU7VXq@nuttyknot.cloudant.com/whenwhere";
		String url = "https://nuttyknot.cloudant.com/whenwhere";
		Log.d("com.nuttyknot.whenwhere", jsonInput.toString());
		JSONObject json = RestClient.connect(url, jsonInput);
		try {
			loadUrl("javascript:callback('" + json.get("id") + "')");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void shareLink(final String img_url) {
		String message = "Join me at " + img_url;
		Log.d("com.nuttyknot.whenwhere", message);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, message);

		mContext.startActivity(Intent.createChooser(share, "Share this!"));
	}

	public void showContact(final String email) {
		Intent i = new Intent();
		i.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
		i.setData(Uri.fromParts("mailto", email, null));

		mContext.startActivity(i);
	}

	public void queryGraph(final String endpoint) {
		try {
			Log.d("Facebook", "Getting \"" + endpoint + "\"");
			String json = facebook.request(endpoint);
			loadUrl("javascript:facebook_callback('" + json + "')");
			Log.d("Facebook", json);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void onFacebookLoggedIn() {
		loadUrl("javascript:facebook_login_callback()");
	}

	public void facebookSSO() {
		mPrefs = ((Activity) mContext).getPreferences(Context.MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

		if (!facebook.isSessionValid()) {
			Log.d("Facebook SSO", "Invalid Session!");
			facebook.authorize((Activity) mContext, new String[] { "email",
					"publish_actions", "publish_stream" },
					new DialogListener() {

						@Override
						public void onComplete(Bundle values) {
							// TODO Auto-generated method stub
							Log.d("Facebook SSO", "Complete!");
							Log.d("Facebook SSO", values.toString());
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString("access_token",
									facebook.getAccessToken());
							editor.putLong("access_expires",
									facebook.getAccessExpires());
							editor.commit();

							onFacebookLoggedIn();
						}

						@Override
						public void onFacebookError(FacebookError e) {
							// TODO Auto-generated method stub
							Log.d("Facebook SSO", e.toString());

						}

						@Override
						public void onError(DialogError e) {
							// TODO Auto-generated method stub
							Log.d("Facebook SSO", e.toString());

						}

						@Override
						public void onCancel() {
							// TODO Auto-generated method stub
							Log.d("Facebook SSO", "Cancel!");

						}

					});
		} else {
			Log.d("Facebook SSO", "Session Already Exist!");
			onFacebookLoggedIn();
		}
	}

	public void scanQR() {
		IntentIntegrator integrator = new IntentIntegrator((Activity) mContext);
		integrator.initiateScan();
	}

	protected void authorizeCallback(int requestCode, int resultCode,
			Intent intent) {
		Log.d("misc", "authorizeCallback");
		facebook.authorizeCallback(requestCode, resultCode, intent);
	}
}