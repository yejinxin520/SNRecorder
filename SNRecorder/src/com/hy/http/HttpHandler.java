package com.hy.http;

import org.apache.http.client.methods.HttpUriRequest;

public abstract class HttpHandler {

	public abstract HttpUriRequest getRequestMethod();
	public abstract void onResponse(String result);
	public void execute() {
		new AsyncHttpTask(this).execute();
	}
}
