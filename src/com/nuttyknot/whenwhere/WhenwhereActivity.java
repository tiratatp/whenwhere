package com.nuttyknot.whenwhere;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class WhenwhereActivity extends Activity {
	private WebView browser;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		browser = (WebView) findViewById(R.id.webView);
		browser.getSettings().setJavaScriptEnabled(true);
		browser.addJavascriptInterface(this, "backend");
		browser.loadUrl("file:///android_asset/main.htm");
	}

	public void create(String in) {
		final String url = "file:///android_asset/www/" + in;
	}
}