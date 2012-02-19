package com.nuttyknot.whenwhere;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.nuttyknot.whenwhere.WebviewActivity;

public class JavaScriptInterface {
    private Context mContext;
    private WebView browser;
    private Handler handler;

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
	
	public void submit(String name, String email, String action, int start, int end, String event_id) {		
		Log.d("com.nuttyknot.whenwhere", "submit");
        JSONObject jsonInput = new JSONObject();
		try {
			jsonInput = ((WebviewActivity)mContext).getStoredVariable();
			jsonInput.put("type", "rsvp");
	        jsonInput.put("name", name);
	        jsonInput.put("email", email);
	        jsonInput.put("action", action);
	        jsonInput.put("event_id", event_id);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        String url = "https://mrstrountlyinhedgmadendi:mLRN14gNMLEoDqlaIvtU7VXq@nuttyknot.cloudant.com/whenwhere";
        Log.d("com.nuttyknot.whenwhere", jsonInput.toString());
        JSONObject json = RestClient.connect(url, jsonInput);        
        try {
			loadUrl("javascript:callback('"+json.get("id")+"')");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void shareLink(String img_url) {		
		String message = "Join me at " + img_url;
		Log.d("com.nuttyknot.whenwhere", message);
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("text/plain");
		share.putExtra(Intent.EXTRA_TEXT, message);

		mContext.startActivity(Intent.createChooser(share, "Share this!"));
	}
}