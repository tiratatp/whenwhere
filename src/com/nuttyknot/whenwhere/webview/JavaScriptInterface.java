package com.nuttyknot.whenwhere.webview;

import java.util.Date;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
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
import com.nuttyknot.whenwhere.RestClient;

public class JavaScriptInterface {
	private Context mContext;
	private WebView browser;
	private Handler handler;

	private SharedPreferences mPrefs;
	public static final Facebook facebook = new Facebook("352831231424321");
	private ProgressDialog progressDialog;

	/** Instantiate the interface and set the context */
	JavaScriptInterface(Context c, WebView w, Handler h) {
		mContext = c;
		browser = w;
		handler = h;
	}

	public boolean isInsideWebView() {
		return true;
	}

	/** Show a toast from the web page */
	public void showToast(String toast) {
		Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
	}

	private void loadUrl(final String in) {
		handler.post(new Runnable() {
			public void run() {
				browser.loadUrl(in);
			}
		});
	}

	public void submit(String name, String email, String action, String when,
			String event_id) {
		Date now = new Date();
		String dateString = String.format("%tF", now);
		JSONObject jsonInput = new JSONObject();
		try {
			jsonInput = ((WebviewActivity) mContext).getStoredVariable();
			jsonInput.put("type", "rsvp");
			jsonInput.put("name", name);
			jsonInput.put("email", email);
			jsonInput.put("action", action);
			jsonInput.put("event_id", event_id);
			jsonInput.put("when", when);
			jsonInput.put("create_at", dateString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// String url =
		// "https://mrstrountlyinhedgmadendi:mLRN14gNMLEoDqlaIvtU7VXq@nuttyknot.cloudant.com/whenwhere";
		String url = "https://nuttyknot.cloudant.com/whenwhere";
		Log.d("com.nuttyknot.whenwhere", jsonInput.toString());
		JSONObject json = RestClient.connect_sync(url, jsonInput, "POST");
		try {
			loadUrl("javascript:whenwhere.callback('" + json.get("id") + "')");
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
		String output_json = "";
		try {
			Log.d("Facebook", "Getting \"" + endpoint + "\"");
			output_json = facebook.request(endpoint);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			output_json = "{'error':'" + e.toString() + "'}";
		} finally {
			Log.d("Facebook", output_json);
			loadUrl("javascript:whenwhere.facebook_callback('" + output_json
					+ "')");
		}
	}

	public void queryGraph(final String endpoint, final String param_json_str) {
		String output_json = "";
		try {
			Bundle param = new Bundle();
			JSONObject json = new JSONObject(param_json_str);
			@SuppressWarnings("unchecked")
			Iterator<String> json_iterator = json.keys();
			boolean has_next = json_iterator.hasNext();
			String key, value;
			while (has_next) {
				key = json_iterator.next();
				value = json.optString(key);
				has_next = json_iterator.hasNext();
				if (value == "") {
					continue;
				}
				param.putString(key, value);
			}
			Log.d("Facebook", "Getting \"" + endpoint + "\" using " + param);
			output_json = facebook.request(endpoint, param);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			output_json = "{'error':'" + e.toString() + "'}";
		} finally {
			Log.d("Facebook", output_json);
			loadUrl("javascript:whenwhere.facebook_callback('" + output_json
					+ "')");
		}
	}

	private void onFacebookLoggedIn() {
		loadUrl("javascript:whenwhere.facebook_login_callback()");
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

	public void showLoading() {
		if (progressDialog == null || !progressDialog.isShowing()) {
			progressDialog = ProgressDialog.show(mContext, "", "Loading...");
		}
	}

	public void hideLoading() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
	}
}