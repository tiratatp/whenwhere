package com.nuttyknot.whenwhere;

import android.app.Activity;
import android.content.Intent;
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
	
	public void createWhere() {
		Intent intent = new Intent(this, WhereActivity.class);
    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(intent);
	}

	public void loadUrl(String in) {	    
		browser.loadUrl("file:///android_asset/" + in);
	}
}