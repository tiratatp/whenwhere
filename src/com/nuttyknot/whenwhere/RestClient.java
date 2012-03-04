package com.nuttyknot.whenwhere;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

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

	/*
	 * This is a test function which will connects to a given rest service and
	 * prints it's response to Android Log with labels "Praeda".
	 */
	public static JSONObject connect(String url, JSONObject jsonInput) {
		JSONObject ret = new JSONObject();
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
		// Log.i("REST",url); // prevent exposure of token

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httppost);
			// Examine the response status
			Log.i("REST", response.getStatusLine().toString());

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i("REST", result);

				// A Simple JSONObject Creation
				JSONObject json = new JSONObject(result);
				Log.i("REST", "<jsonobject>\n" + json.toString()
						+ "\n</jsonobject>");

				// Closing the input stream will trigger connection release
				instream.close();

				ret = json;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

}