package com.platypii.baseline.ui;

import java.io.File;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;


class AjaxPostJump {

	
	public static String postJump(String filename) {
		
		HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost("http://platypiiindustries.com:12000/upload");
	    // HttpPost httppost = new HttpPost("http://www.platypiiindustries.com/upload");

	    try {

	    	File file = new File(filename);
	    	FileEntity reqEntity = new FileEntity(file, "binary/octet-stream");
	        reqEntity.setChunked(true); // Send in multiple parts if needed
	        httppost.setEntity(reqEntity);
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);

	        // TODO: Do something with response... check status

	        return response.toString();
	        
	    } catch (ClientProtocolException e) {
	    	Log.e("WebPostJump", e.toString());
	    } catch (IOException e) {
	    	Log.e("WebPostJump", e.toString());
	    }
	    
	    return null;
	}
	
}
