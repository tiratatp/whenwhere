package com.nuttyknot.whenwhere;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class Model {
	/*
	private void callWebService(String q){  
		HttpClient httpclient = new DefaultHttpClient();  
        HttpGet request = new HttpGet(URL + q);  
        request.addHeader("deviceId", deviceId);  
        ResponseHandler<string> handler = new BasicResponseHandler();  
        try {  
            result = httpclient.execute(request, handler);  
        } catch (ClientProtocolException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        httpclient.getConnectionManager().shutdown();  
        Log.i(tag, result);  
    } // end callWebService()  */
}
