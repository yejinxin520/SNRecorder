package com.hy.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncHttpTask extends AsyncTask<String, Void, String> {

	private HttpHandler handler;
	public AsyncHttpTask(HttpHandler handler) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
	}
	@Override
	protected String doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		String result = "";
		
		InputStream inputStream =null;
		try{
			HttpClient httpClient = new DefaultHttpClient();					
			
			HttpResponse httpResponse = httpClient.execute(handler.getRequestMethod());
		    
			inputStream = httpResponse.getEntity().getContent();

	        if(inputStream != null)
	            result = convertInputStreamToString(inputStream);
	        
		}catch(Exception e){
			Log.d("InputStream", e.getLocalizedMessage());
		}
		
		return result;
	}	
	@Override
	protected void onPostExecute(String result) {
		// TODO Auto-generated method stub
		//super.onPostExecute(result);
		handler.onResponse(result);
	}
	private static String convertInputStreamToString(InputStream is) {
		 
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
 
		String line;
		try {
 
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
 
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
 
		return sb.toString();
 
	}
}
