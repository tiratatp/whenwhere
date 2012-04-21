package com.nuttyknot.whenwhere.webview;

import com.nuttyknot.whenwhere.where.WhereActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ExtendedWebViewClient extends WebViewClient {

	private Context context;

	public ExtendedWebViewClient(Context c) {
		this.context = c;
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Uri uri = Uri.parse(url);
		String page = uri.getLastPathSegment();
		if (page.equals("where.htm")) {
			Intent intent = new Intent(context, WhereActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
			return true;
		} else if (page.equals("decide_where.htm")) {
			String event_id = uri.getFragment();
			Intent intent = new Intent(context, WhereActivity.class);
			intent.putExtra("role", "decide_where");
			intent.putExtra("event_id", event_id);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
			return true;
		}

		return false;
	}

}
