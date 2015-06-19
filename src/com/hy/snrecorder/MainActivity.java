package com.hy.snrecorder;

import java.io.FileInputStream;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EncodingUtils;

import com.hy.util.ConfigurationSet;
import com.hy.util.HttpHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private ProgressDialog dialog;
	String url = "http://192.168.0.201/mary/sellrec/api/task/"
			+ "?formfat=json?&username=tomsu&api_key=123456&finished=0&p=0";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		initSetting();
	}

	public void doClick(View v) {
		if (v.equals((LinearLayout) findViewById(R.id.task))) {
			dialog = new ProgressDialog(this);
			dialog.setTitle("请稍等");
			dialog.setMessage("正在获取任务");
			dialog.show();
			new HttpHandler() {

				@Override
				public void onResponse(String result) {
					// TODO Auto-generated method stub
					if (result == "") {
						AlertDialog.Builder msgBox = new Builder(
								MainActivity.this);
						msgBox.setTitle("提示");
						msgBox.setMessage("网络错误，连接失败");
						msgBox.setPositiveButton("确定", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
						msgBox.create().show();
					} else {
						System.out.println(result.toString());

						Thread t = new Thread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								try {
									Thread.sleep(500);
									dialog.dismiss();
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						});
						t.start();
						Intent task = new Intent(MainActivity.this,
								TaskActivity.class);
						Bundle b = new Bundle();
						b.putString("taskmessage", result);
						task.putExtras(b);
						startActivity(task);
					}
				}

				@Override
				public HttpUriRequest getRequestMethod() {
					// TODO Auto-generated method stub
					return new HttpGet(url);
				}
			}.execute();

		}
		if (v.equals((LinearLayout) findViewById(R.id.query))) {
			Intent query = new Intent(this, QueryActivity.class);
			startActivity(query);
		}
		if (v.equals((LinearLayout) findViewById(R.id.setting))) {
			Intent setting = new Intent(this, SettingActivity.class);
			startActivity(setting);
		}
	}

	private void initSetting() {
		String str = "";
		try {
			FileInputStream fis = openFileInput("conf");
			int length = fis.available();
			byte[] buffer = new byte[length];
			fis.read(buffer);
			str = EncodingUtils.getString(buffer, "utf-8");
			fis.close();
			String[] file = str.split("\n");
			ConfigurationSet.setAutoUpload(Boolean.parseBoolean(file[0]));
			ConfigurationSet.setSanTimes(Integer.parseInt(file[1]));
			ConfigurationSet.setBarcodeLimit1(Integer.parseInt(file[2]));
			ConfigurationSet.setBarcodeLimit2(Integer.parseInt(file[3]));
			ConfigurationSet.setBarcodeLimit3(Integer.parseInt(file[4]));
		} catch (Exception e) {
			ConfigurationSet.setAutoUpload(false);
			ConfigurationSet.setSanTimes(1);
			ConfigurationSet.setBarcodeLimit1(0);
			ConfigurationSet.setBarcodeLimit2(0);
			ConfigurationSet.setBarcodeLimit3(0);
			e.printStackTrace();
		}
	}
}
