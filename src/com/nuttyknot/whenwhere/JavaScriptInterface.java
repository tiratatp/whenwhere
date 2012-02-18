package com.nuttyknot.whenwhere;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.webkit.WebView;
import android.widget.Toast;

public class JavaScriptInterface {
    private Context mContext;
    private WebView browser;
    private Handler handler;
    private int event_id;

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
    
    public void setEventId(int event_id) {
    	this.event_id = event_id;	
	}
    
    public int getEventId() {
		return event_id;	
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
}