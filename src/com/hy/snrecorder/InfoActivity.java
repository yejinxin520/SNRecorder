package com.hy.snrecorder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.hy.util.HttpHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class InfoActivity extends Activity {

	private Bundle bundle;
	private int selection;
	private String type;
	private ProgressDialog dialog;
	private String url;
	private JSONObject jsonObject;
	private TextView uidrep, customerrep, modelrep, costrep, issuerep,
			solverep, backrep, warrantyrep, commentrep;
	private TextView modelout, uidnumout, dateout, commentout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_repairinfo);
		uidrep = (TextView) findViewById(R.id.uidrep);
		customerrep = (TextView) findViewById(R.id.customerrep);
		modelrep = (TextView) findViewById(R.id.modelrep);
		costrep = (TextView) findViewById(R.id.costrep);
		issuerep = (TextView) findViewById(R.id.issuerep);
		solverep = (TextView) findViewById(R.id.solverep);
		backrep = (TextView) findViewById(R.id.backdaterep);
		warrantyrep = (TextView) findViewById(R.id.warrantyrep);
		commentrep = (TextView) findViewById(R.id.commentrep);

		Intent intent = getIntent();
		bundle = new Bundle();
		bundle = intent.getExtras();
		selection = bundle.getInt("selection");
		type = bundle.getString("type");
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在查询");
		dialog.show();
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if (result == "") {
					AlertDialog.Builder msgBox = new Builder(InfoActivity.this);
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
					String resultstr = result.toString();
					System.out.println(resultstr);
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
					jsonObject = new JSONObject();
					try {
						jsonObject = new JSONObject(resultstr);
						if (type.equals("out")) {
							final String model = jsonObject.getString("model");
							final int uidnum = jsonObject.getInt("UIDsNum");
							final String date = jsonObject.getString("date");
							final String comment = jsonObject
									.getString("comment");
							System.out.println(model + "," + uidnum + ","
									+ date + "," + comment);
							setContentView(R.layout.layout_outinfo);
							modelout = (TextView) findViewById(R.id.modelout);
							uidnumout = (TextView) findViewById(R.id.uidnum);
							dateout = (TextView) findViewById(R.id.dateout);
							commentout = (TextView) findViewById(R.id.commentout);
							modelout.setText(model);
							uidnumout.setText("" + uidnum);
							dateout.setText(date);
							if (comment.equals("")) {
								commentout.setText("无");
							} else {
								commentout.setText(comment);
							}

						} else if (type.equals("repair")) {
							String uid = jsonObject.getString("UID");
							String customer = bundle.getString("customer");
							String model = bundle.getString("model");
							int costs = jsonObject.getInt("costs");
							String issues = jsonObject.getString("issues");
							String issues_resolve = jsonObject
									.getString("issues_resolve");
							String sent_back = jsonObject
									.getString("sent_back");
							String warranty = jsonObject.getString("warranty");
							String comment = jsonObject.getString("comment");
							System.out.println(model + "," + uid + ","
									+ customer + "," + comment + "," + costs
									+ "," + issues + "," + issues_resolve + ","
									+ sent_back + "," + warranty);
							uidrep.setText(uid);
							customerrep.setText(customer);
							modelrep.setText(model);
							costrep.setText("" + costs);
							issuerep.setText(issues);
							solverep.setText(issues_resolve);
							backrep.setText(sent_back);
							warrantyrep.setText(warranty);
							if (comment.equals("")) {
								commentrep.setText("无");
							} else {
								commentrep.setText(comment);
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub
				if (type.equals("out")) {
					url = "http://192.168.0.201/mary/sellrec/api/query/"
							+ selection
							+ "/?format=json&username=tomsu&api_key=123456&id="
							+ selection + "&p=0";
				} else if (type.equals("repair")) {
					url = "http://192.168.0.201/mary/sellrec/api/queryrep/"
							+ selection
							+ "/?format=json&username=tomsu&api_key=123456&id="
							+ selection + "&p=0";
				}
				return new HttpGet(url);
			}
		}.execute();
	}

}
