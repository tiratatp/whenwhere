package com.nuttyknot.whenwhere;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebviewActivity extends Activity {
	private WebView browser;
	private String current_latitude;
	private String current_longitude;
	private int radius;
	private Handler handler;
	private JavaScriptInterface javaScriptInterface;

	protected JSONObject getStoredVariable() throws JSONException {
		JSONObject jsonInput = new JSONObject();
		jsonInput.put("latitude", current_latitude);
		jsonInput.put("longitude", current_longitude);
		jsonInput.put("position", current_latitude+", "+current_longitude);
		jsonInput.put("radius", radius);
		return jsonInput;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		String url = "file:///android_asset/main.htm";
		if (intent.hasExtra("event_id") || (intent.hasExtra("latitude") && intent.hasExtra("longitude"))) {
			current_latitude = intent.getStringExtra("latitude");
			current_longitude = intent.getStringExtra("longitude");
			radius = intent.getIntExtra("radius", 0);
			url = "file:///android_asset/when.htm";
		} else {
			url = "file:///android_asset/main.htm";
		}

		handler = new Handler();

		browser = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = browser.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true); // localStorage
		webSettings
				.setDatabasePath("/data/data/com.nuttyknot.whenwhere/databases/");

		javaScriptInterface = new JavaScriptInterface(this, browser, handler);

		browser.addJavascriptInterface(javaScriptInterface, "backend");
		browser.loadUrl(url);
		browser.setWebChromeClient(new WebChromeClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d("console.log",
						cm.message() + " -- From line " + cm.lineNumber()
								+ " of " + cm.sourceId());
				return true;
			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d("com.nuttyknot.whenwhere", "onKeyDown:" + browser.getUrl());
			if (browser.canGoBack()) {
				browser.goBack();
				return true;
			} else if (browser.getUrl() != "file:///android_asset/main.htm") {
				loadUrl("file:///android_asset/main.htm");
				return true;
			}
		}
		// If it wasn't the Back key or there's no web page history, bubble up
		// to the default
		// system behavior (probably exit the activity)
		return super.onKeyDown(keyCode, event);
	}

	public void loadUrl(final String in) {
		handler.post(new Runnable() {
			public void run() {
				browser.loadUrl(in);
			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {

		Log.d("misc", "onActivityResult");
		// facebook sso
		javaScriptInterface.authorizeCallback(requestCode, resultCode, intent);

		// qr
		IntentResult scanResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);
		if (scanResult != null) {
			loadUrl("javascript:callback('" + scanResult.getContents() + "')");
		}

	}
}