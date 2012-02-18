package com.nuttyknot.whenwhere;

import android.app.Activity;
import android.content.Context;
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
	private int event_id;
	private Handler handler;


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		handler = new Handler();
		
		browser = (WebView) findViewById(R.id.webView);
		WebSettings webSettings = browser.getSettings();		
		webSettings.setJavaScriptEnabled(true);
		webSettings.setDomStorageEnabled(true); // localStorage
		webSettings.setDatabasePath("/data/data/com.nuttyknot.whenwhere/databases/");
		
		browser.addJavascriptInterface(new JavaScriptInterface(this, browser, handler), "backend");
		browser.loadUrl("file:///android_asset/main.htm");
		browser.setWebChromeClient(new WebChromeClient() {
			public boolean onConsoleMessage(ConsoleMessage cm) {
				Log.d("com.nuttyknot.whenwhere",
						cm.message() + " -- From line " + cm.lineNumber()
								+ " of " + cm.sourceId());
				return true;
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && browser.canGoBack()) {
	    	browser.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
}