package com.nuttyknot.whenwhere.webview;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.nuttyknot.whenwhere.R;

import android.app.Activity;
import android.app.PendingIntent;
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
	private String picked_place_id;
	private String picked_place_name;
	private int radius;
	private Handler handler;
	private JavaScriptInterface javaScriptInterface;

	protected JSONObject getPickedPlace() throws JSONException {
		JSONObject jsonInput = new JSONObject();
		jsonInput.put("place_id", picked_place_id);
		jsonInput.put("place_name", picked_place_name);
		return jsonInput;
	}

	protected JSONObject getStoredVariable() throws JSONException {
		JSONObject jsonInput = new JSONObject();
		jsonInput.put("latitude", current_latitude);
		jsonInput.put("longitude", current_longitude);
		jsonInput.put("position", current_latitude + ", " + current_longitude);
		jsonInput.put("radius", radius);
		return jsonInput;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		String event_id = "";
		Intent intent = getIntent();
		String action = intent.getAction();
		String url = "file:///android_asset/www/main.htm";
		if (Intent.ACTION_VIEW.equals(action)) {
			String fragment = intent.getData().getFragment();
			Matcher eventIdMatcher = Pattern.compile("event_id=([A-Za-z0-9]+)")
					.matcher(fragment);
			if (eventIdMatcher.find()) {
				event_id = eventIdMatcher.group(1);
				Log.d("Event_id", event_id);
				url = "file:///android_asset/www/search.htm";
			}
		}
		if (intent.hasExtra("event_id")
				|| (intent.hasExtra("latitude") && intent.hasExtra("longitude"))) {
			current_latitude = intent.getStringExtra("latitude");
			current_longitude = intent.getStringExtra("longitude");
			radius = intent.getIntExtra("radius", 0);
			url = "file:///android_asset/www/when.htm";
		} else if (intent.hasExtra("place_id") && intent.hasExtra("place_name")) {
			picked_place_id = intent.getStringExtra("place_id");
			picked_place_name = intent.getStringExtra("place_name");
			url = "file:///android_asset/www/decide_when.htm";
		}

		handler = new Handler();

		browser = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = browser.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true); // localStorage
		webSettings
				.setDatabasePath("/data/data/com.nuttyknot.whenwhere/databases/");

		javaScriptInterface = new JavaScriptInterface(this, browser, handler);

		browser.setWebViewClient(new ExtendedWebViewClient(this));
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

		// register C2DM
		Intent registrationIntent = new Intent(
				"com.google.android.c2dm.intent.REGISTER");

		registrationIntent.putExtra("app",
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));

		registrationIntent.putExtra("sender", "nutty.knot@gmail.com");

		this.startService(registrationIntent);

		if (event_id.compareTo("") != 0) {
			loadUrl("javascript:whenwhere.callback('" + event_id + "')");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// Check if the key event was the Back button and if there's history
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (browser.canGoBack()) {
				browser.goBack();
				return true;
			} else if (!browser.getUrl().equals(
					"file:///android_asset/www/main.htm")) {
				loadUrl("file:///android_asset/www/main.htm");
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
			String content = scanResult.getContents();
			Log.d("BarCode", content);
			Matcher eventIdMatcher = Pattern
					.compile("#event_id=([A-Za-z0-9]+)").matcher(content);
			if (eventIdMatcher.find()) {
				String event_id = eventIdMatcher.group(1);
				Log.d("Event_id", event_id);
				loadUrl("javascript:whenwhere.callback('" + event_id + "')");
			}
		}
	}
}