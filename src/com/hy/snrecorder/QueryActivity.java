package com.hy.snrecorder;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONObject;

import com.hy.util.HttpHandler;
import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.motorolasolutions.adc.decoder.DecodeUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class QueryActivity extends Activity {
	private String url;
	private int total, count;
	private List<String> queryList;
	private ArrayAdapter<String> adapter, hisadapter;
	private ListView queryListView;
	private AutoCompleteTextView uidactv;
	private ImageButton clearimg;
	private TextView resulstate;
	DecodeUtil decodeMethod = new DecodeUtil();
	private BarCodeReader bcr = null;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String s = bundle.getString("barc");
			uidactv.setText(s);
		};
	};
	private ProgressDialog dialog;
	private Intent infoIntent;
	private String resultstr;
	private JSONObject jsonObject;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_query);
		queryList = new ArrayList<String>();
		queryListView = (ListView) findViewById(R.id.querylist);
		resulstate = (TextView) findViewById(R.id.resulttv);
		clearimg = (ImageButton) findViewById(R.id.clearimg);
		uidactv = (AutoCompleteTextView) findViewById(R.id.uidactv);
		initHistory();
		TextWatcher textWatcher = new TextWatcher() {
			CharSequence temp;

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				temp = arg0;
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (temp.length() == 0) {
					clearimg.setVisibility(View.GONE);
				} else {
					clearimg.setVisibility(View.VISIBLE);
				}
			}
		};
		uidactv.addTextChangedListener(textWatcher);
		infoIntent = new Intent(this, InfoActivity.class);
		queryListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Bundle b = new Bundle();
				try {
					if (position + 1 > total) {
						b.putString("type", "repair");
						String customer = jsonObject.getJSONArray("repair")
								.getJSONObject(position - total)
								.getString("customer");
						String model = jsonObject.getJSONArray("repair")
								.getJSONObject(position - total)
								.getString("model");
						b.putString("customer", customer);
						b.putString("model", model);
						int pk = jsonObject.getJSONArray("repair")
								.getJSONObject(position - total).getInt("pk");
						b.putInt("selection", pk);
					} else {
						int index = jsonObject.getJSONArray("objects")
								.getJSONObject(position).getInt("index");
						b.putInt("selection", index);
						b.putString("type", "out");
					}
					infoIntent.putExtras(b);
					startActivity(infoIntent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println(event.getKeyCode());
		if (((keyCode == 135) || (keyCode == 136) )
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			doScan(findViewById(R.id.qscanbtn));
			return true;
		}
		if(keyCode == 137&& event.getAction() == KeyEvent.ACTION_DOWN){
			doQuery(findViewById(R.id.querybtn));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void doScan(View v) {
		decodeMethod.doDecode();
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					while (decodeMethod.getData().length() == 0) {
						Thread.sleep(500);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String s = decodeMethod.getData().trim();
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("barc", s);
				msg.setData(bundle);
				QueryActivity.this.handler.sendMessage(msg);

			}
		});
		t.start();
	}

	public void doQuery(View v) {
		queryList.clear();
		url = "http://192.168.0.201/mary/sellrec/query/?&username=tomsu&api_key=123456&"
				+ "uid=" + uidactv.getText();
		System.out.println(url);
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在查询");
		dialog.show();
		new HttpHandler() {
			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if (result == "") {
					AlertDialog.Builder msgBox = new Builder(QueryActivity.this);
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
					resultstr = result.toString();
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
						total = jsonObject.getInt("total");
						count = jsonObject.getInt("count");
						if (total == 0 && count == 0) {
							resulstate.setText("没有找到结果！");
							resulstate.setTextColor(Color.RED);
						} else {
							for (int i = 0; i < total; i++) {
								String customer = jsonObject
										.getJSONArray("objects")
										.getJSONObject(i).getString("customer");
								String date = jsonObject
										.getJSONArray("objects")
										.getJSONObject(i).getString("date");
								queryList.add(customer + "在" + date + "的出货记录");
							}
							for (int j = 0; j < count; j++) {
								String uid = jsonObject.getJSONArray("repair")
										.getJSONObject(j).getString("uid");
								String time = jsonObject.getJSONArray("repair")
										.getJSONObject(j).getString("time");
								queryList.add(uid + "在" + time + "的维修记录");
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					adapter = new ArrayAdapter<String>(QueryActivity.this,
							android.R.layout.simple_list_item_1, queryList);
					queryListView.setAdapter(adapter);
					saveHistory();
					uidactv.setText("");
				}
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub
				return new HttpGet(url);
			}
		}.execute();

	}

	public void doClear(View v) {
		uidactv.setText("");
	}

	private void initHistory() {
		SharedPreferences sp = getSharedPreferences("search_history", 0);
		String history = sp.getString("history", "nothing");
		String[] historyarr = history.split(",");
		String[] newArrays = new String[historyarr.length - 1];
		if (historyarr.length > 1) {
			System.arraycopy(historyarr, 0, newArrays, 0, historyarr.length - 1);
		}
		hisadapter = new ArrayAdapter<>(this,
				android.R.layout.simple_list_item_1, newArrays);
		uidactv.setAdapter(hisadapter);
		uidactv.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				AutoCompleteTextView view = (AutoCompleteTextView) v;
				if (hasFocus) {
					view.showDropDown();
				}
			}
		});
	}

	private void saveHistory() {
		String newhis = uidactv.getText().toString();
		SharedPreferences mysp = getSharedPreferences("search_history", 0);
		String oldhis = mysp.getString("history", "nothing");
		StringBuilder sb = new StringBuilder(oldhis);
		if (!newhis.equals("")) {
			sb.insert(0, newhis + ",");
			if (!oldhis.contains(newhis + ",")) {
				SharedPreferences.Editor editor = mysp.edit();
				editor.putString("history", sb.toString());
				editor.commit();
				super.onDestroy();
			}
		}
		System.out.println(oldhis);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (bcr != null) {
			bcr.release();
			bcr = null;
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {

					if (android.os.Build.VERSION.SDK_INT >= 18)
						bcr = BarCodeReader.open(getApplicationContext()); // Android
																			// 4.3
																			// and
																			// above
					else
						bcr = BarCodeReader.open(); // Android 2.3

					decodeMethod.decodeinit(bcr);
					if (bcr == null) {
						Log.d("tag", "open failed");
						return;
					}

				} catch (Exception e) {
					Log.d("tag", "open excp:" + e);
					System.out.println("open excp:" + e);
				}
			}
		});
		t.start();

	}
}
