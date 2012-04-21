package com.nuttyknot.whenwhere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RestClient {

	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public static void connect_async(final String url,
			final JSONObject jsonInput, final Handler handler,
			final String method) {
		// create a new thread
		Thread background = new Thread(new Runnable() {

			@Override
			public void run() {
				String str = "";
				if (method == "POST") {
					str = post(url, jsonInput);
				} else if (method == "GET") {
					str = get(url, jsonInput);
				}
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("data", str);
				msg.setData(bundle);
				handler.dispatchMessage(msg);
			}
		});

		background.start();
	}

	public static JSONObject connect_sync(final String url,
			final JSONObject jsonInput, final String method) {
		JSONObject json = new JSONObject();
		String str;
		if (method == "POST") {
			str = post(url, jsonInput);
		} else if (method == "GET") {
			str = get(url, jsonInput);
		} else {
			return json;
		}

		try {
			json = new JSONObject(str);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
	}

	/*
	 * This is a test function which will connects to a given rest service and
	 * prints it's response to Android Log with labels "Praeda".
	 */
	private static String post(String url, JSONObject jsonInput) {
		String ret = "";
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		HttpPost httppost = new HttpPost(url);
		try {
			StringEntity se = new StringEntity(jsonInput.toString());
			se.setContentType("application/json;charset=UTF-8");
			httppost.setEntity(se);
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// base64 mrstrountlyinhedgmadendi:mLRN14gNMLEoDqlaIvtU7VXq
		httppost.setHeader("Authorization",
				"Basic bXJzdHJvdW50bHlpbmhlZGdtYWRlbmRpOm1MUk4xNGdOTUxFb0RxbGFJdnRVN1ZYcQ==");
		// Log.i("POST", url); // prevent exposure of token

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
			// Examine the response status
			Log.i("POST", response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i("POST", result);

				// Closing the input stream will trigger connection release
				instream.close();

				ret = result;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	private static String get(String url, JSONObject jsonInput) {
		String ret = "";
		HttpClient httpclient = new DefaultHttpClient();

		// Prepare a request object
		Uri.Builder base = Uri.parse(url).buildUpon();
		@SuppressWarnings("unchecked")
		Iterator<String> iter = jsonInput.keys();
		while (iter.hasNext()) {
			String key = iter.next();
			try {
				String value = jsonInput.getString(key);
				base.appendQueryParameter(key, "\"" + value + "\"");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Uri uri = base.build();
		String uri_str = uri.toString();
		HttpGet httpget = new HttpGet(uri_str);
		// base64 mrstrountlyinhedgmadendi:mLRN14gNMLEoDqlaIvtU7VXq
		httpget.setHeader("Authorization",
				"Basic bXJzdHJvdW50bHlpbmhlZGdtYWRlbmRpOm1MUk4xNGdOTUxFb0RxbGFJdnRVN1ZYcQ==");
		// Log.i("GET", uri_str); // prevent exposure of token

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status
			Log.i("GET", response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i("GET", result);

				// Closing the input stream will trigger connection release
				instream.close();

				ret = result;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}
}