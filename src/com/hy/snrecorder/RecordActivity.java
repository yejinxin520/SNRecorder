package com.hy.snrecorder;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.baoyz.swipemenulistview.SwipeMenuListView.OnMenuItemClickListener;
import com.hy.util.AsyncHttpTask;
import com.hy.util.ConfigurationSet;
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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class RecordActivity extends Activity {

	private int offset = 0;
	private String idMessage, model, tasknumber, url, resultstr;
	private Boolean autoUpload, init;
	private int scanTimes, total_count, uploadnum, visnum, scannedTimes = 0;
	private int[] barcodelimit;
	private TextView modeltv, total, scanned, barcode1, barcode2, barcode3,
			state;
	private ProgressDialog dialog;
	private LinearLayout llcontainer;
	private SlidingDrawer drawer;
	private ImageView imageView;
	private List<String> scannedList;
	private List<String> tmpList;
	private ArrayAdapter<String> adapter;
	private JSONObject jsonObject;
	DecodeUtil decodeMethod = new DecodeUtil();
	private BarCodeReader bcr = null;
	private Hashtable<String, String> hashtable;
	private SwipeMenuListView scannedListV;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String s = bundle.getString("barc");
			if (scannedTimes < scanTimes) {
				if (s.length() == barcodelimit[scannedTimes]
						|| barcodelimit[scannedTimes] == 0) {
					if (hashtable.containsKey(s) || tmpList.contains(s)) {
						state.setText("条码已记录或已扫描，请重新扫描！");
						state.setTextColor(Color.RED);
					} else {						
						if (scannedTimes == 0)
							barcode1.setText(s);
						else if (scannedTimes == 1)
							barcode2.setText(s);
						else
							barcode3.setText(s);
						tmpList.add(s);
						scannedTimes++;
						state.setText("条码" + scannedTimes + "扫描完成");
						state.setTextColor(Color.BLACK);
					}
				} else {
					state.setText("条码长度不匹配");
					state.setTextColor(Color.RED);
				}
			} else {
				state.setText("扫描已完成，请上传或者清除");
				state.setTextColor(Color.RED);
			}
			if (scannedTimes == scanTimes && autoUpload) {
				dialog = new ProgressDialog(RecordActivity.this);
				dialog.setTitle("请稍等");
				dialog.setMessage("正在上传");
				dialog.show();
				for (int j = 0; j < tmpList.size(); j++) {
					hashtable.put(tmpList.get(j).toString(), "");
					dopost(tmpList.get(j).toString());
				}
				scannedTimes = 0;
				tmpList.clear();
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_record);
		Intent intent = getIntent();
		idMessage = intent.getStringExtra("idmessage");
		model = intent.getStringExtra("modelmessage");
		tasknumber = intent.getStringExtra("numbermessage");
		url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
				+ "offset=" + offset + "&finished=0&task=" + idMessage + "&p=0";
		init = true;
		scannedListV = (SwipeMenuListView) findViewById(R.id.contentlist);
		hashtable = new Hashtable<String, String>();
		scannedList = new ArrayList<String>();
		tmpList = new ArrayList<String>();
		httpQuery();

		adapter = new ArrayAdapter<String>(RecordActivity.this,
				android.R.layout.simple_list_item_1, scannedList);

		scannedListV.setAdapter(adapter);

		autoUpload = ConfigurationSet.getAutoUpload();
		scanTimes = ConfigurationSet.getSanTimes();
		barcodelimit = new int[3];
		barcodelimit[0] = ConfigurationSet.getBarcodeLimit1();
		barcodelimit[1] = ConfigurationSet.getBarcodeLimit2();
		barcodelimit[2] = ConfigurationSet.getBarcodeLimit3();
		modeltv = (TextView) findViewById(R.id.modeltv);
		modeltv.setText(model);
		total = (TextView) findViewById(R.id.total);
		total.setText(tasknumber);
		scanned = (TextView) findViewById(R.id.scanned);
		barcode1 = (TextView) findViewById(R.id.barstr);
		barcode2 = (TextView) findViewById(R.id.barstr1);
		barcode3 = (TextView) findViewById(R.id.barstr2);
		state = (TextView) findViewById(R.id.statetv);
		llcontainer = (LinearLayout) findViewById(R.id.container1);
		if (scanTimes == 2) {
			llcontainer.removeViews(4, 2);
		}
		if (scanTimes == 1) {
			llcontainer.removeViews(2, 4);
		}
		drawer = (SlidingDrawer) findViewById(R.id.sduidlist);
		imageView = (ImageView) findViewById(R.id.open);
		drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {

			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				imageView.setImageResource(R.drawable.ic_slidclose);
			}
		});
		drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {

			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				imageView.setImageResource(R.drawable.ic_slidopen);
			}
		});

		new ToneGenerator(AudioManager.STREAM_MUSIC, ToneGenerator.MAX_VOLUME);
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// TODO Auto-generated method stub

				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getApplicationContext());
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				deleteItem.setWidth(dp2px(90));
				deleteItem.setIcon(R.drawable.ic_delete);
				menu.addMenuItem(deleteItem);
			}
		};
		scannedListV.setMenuCreator(creator);
		scannedListV.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public void onMenuItemClick(final int position, SwipeMenu menu,
					int index) {
				// TODO Auto-generated method stub
				switch (index) {
				case 0:
					final String key = scannedList.get(position);
					final String id = hashtable.get(key);
					System.out.println(id);
					Builder msgBox = new Builder(RecordActivity.this);
					msgBox.setTitle("提示");
					msgBox.setMessage("您确定要删除这条记录吗");
					msgBox.setPositiveButton("确定", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							httpDelete(id);
							hashtable.remove(key);
							scannedList.remove(position);
							adapter.notifyDataSetChanged();
						}
					});
					msgBox.setNegativeButton("取消", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub

						}
					});
					msgBox.create().show();
					break;

				default:
					break;
				}
			}
		});
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println(event.getKeyCode());
		if (((keyCode == 135) || (keyCode == 136) )
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			doScan(findViewById(R.id.scanbtn));
			return true;
		}
		if(keyCode == 137&& event.getAction() == KeyEvent.ACTION_DOWN){
			doUpload(findViewById(R.id.ulbtn));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void httpDelete(final String id) {
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在删除");
		dialog.show();
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
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
				uploadnum--;
				scanned.setText("" + uploadnum);
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub				
				String urlp = "http://192.168.0.201/mary/sellrec/api/collect/"
						+ id
						+ "/?format=json&username=tomsu&api_key=123456&id="
						+ id;
				System.out.println(urlp);
				return new HttpDelete(urlp);
			}
		}.execute();
	}

	private void idQuery() {
		offset = 0;
		url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
				+ "offset="
				+ offset
				+ "&finished=0&task="
				+ idMessage + "&p=0";
		init = true;
		hashtable.clear();
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if (result == "") {
					System.out.println("网络错误");
				} else {					
					resultstr = result.toString();
					System.out.println(result);
					Thread t = new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							try {
								Thread.sleep(500);
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
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (init) {
						try {
							total_count = jsonObject.getJSONObject("meta")
									.getInt("total_count");
							init = false;
							visnum = total_count;
							uploadnum = total_count;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						visnum -= 20;
					}
					if (visnum > 20) {
						inithash();
						offset += 20;
						url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
								+ "offset="
								+ offset
								+ "&finished=0&task="
								+ idMessage + "&p=0";
						new AsyncHttpTask(this).execute();
					} else {
						inithash();
					}
				}
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub				
				return new HttpGet(url);
			}
		}.execute();
	}
	
	private void httpQuery() {
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在查询");
		dialog.show();
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if (result == "") {
					AlertDialog.Builder msgBox = new Builder(
							RecordActivity.this);
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
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (init) {
						try {
							total_count = jsonObject.getJSONObject("meta")
									.getInt("total_count");
							init = false;
							visnum = total_count;
							uploadnum = total_count;
						} catch (JSONException e) {
							e.printStackTrace();
						}
					} else {
						visnum -= 20;
					}
					if (visnum > 20) {
						initList();
						offset += 20;
						url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
								+ "offset="
								+ offset
								+ "&finished=0&task="
								+ idMessage + "&p=0";
						new AsyncHttpTask(this).execute();
					} else {
						initList();
					}
					scanned.setText("" + total_count);

				}
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub
				return new HttpGet(url);
			}
		}.execute();
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
				RecordActivity.this.handler.sendMessage(msg);

			}
		});
		t.start();

	}

	public void doUpload(View v) {
		if (scannedTimes == scanTimes) {
			dialog = new ProgressDialog(this);
			dialog.setTitle("请稍等");
			dialog.setMessage("正在上传");
			dialog.show();
			for (int j = 0; j < tmpList.size(); j++) {
				hashtable.put(tmpList.get(j).toString(), "");
				dopost(tmpList.get(j).toString());
			}						
			scannedTimes = 0;
			tmpList.clear();			
		} else {
			state.setText("扫描未完成！");
			state.setTextColor(Color.RED);
		}
	}

	private void dopost(final String uid) {
		final String urlpath = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456";		
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				System.out.println("post:" + result.toString());
				uploadnum ++;
				scanned.setText("" + uploadnum);
				scannedList.add(uid);
				
				
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(500);
							dialog.dismiss();idQuery();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				t.start();
				adapter.notifyDataSetChanged();
				scannedListV.setSelection(uploadnum - 1);
				if (scannedTimes == 1)
					barcode1.setText("");
				else if (scannedTimes == 2){
					barcode1.setText("");
					barcode2.setText("");
				}
				else{
					barcode1.setText("");
					barcode2.setText("");
					barcode3.setText("");
				}
				state.setText("上传成功！请继续扫描！");
				state.setTextColor(Color.BLACK);
				drawer.open();
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub
				HttpPost httpPost = new HttpPost(urlpath);
				try {
					String json = "";										
					JSONObject jsonObject = new JSONObject();					
                    jsonObject.accumulate("task", "/mary/sellrec/api/task/"
							+ idMessage + "/");
					jsonObject.accumulate("UID", uid);
					json = jsonObject.toString();
					StringEntity se = new StringEntity(json);
					httpPost.setEntity(se);
					httpPost.setHeader("Accept", "application/json");
					httpPost.setHeader("Content-type", "application/json");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.err.println("upload err");
				}
				return httpPost;
			}
		}.execute();
	}

	public void doClear(View v) {
		if (scanTimes == 3) {
			barcode1.setText("");
			barcode2.setText("");
			barcode3.setText("");
		}
		if (scanTimes == 2) {
			barcode1.setText("");
			barcode2.setText("");
		}
		if (scanTimes == 1) {
			barcode1.setText("");
		}

		scannedTimes = 0;
		tmpList.clear();
		state.setText("请重新扫描条码！");
		state.setTextColor(Color.BLACK);
	}
	
	private void inithash() {
		int tempnum = total_count > 20 ? 20 : total_count;
		for (int i = 0; i < tempnum; i++) {
			String uid = "";
			try {
				uid = jsonObject.getJSONArray("objects").getJSONObject(i)
						.getString("UID");
				int id = jsonObject.getJSONArray("objects").getJSONObject(i)
						.getInt("id");
				hashtable.put(uid, "" + id);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void initList() {
		int tempnum = total_count > 20 ? 20 : total_count;
		for (int i = 0; i < tempnum; i++) {
			String uid = "";
			try {
				uid = jsonObject.getJSONArray("objects").getJSONObject(i)
						.getString("UID");
				int id = jsonObject.getJSONArray("objects").getJSONObject(i)
						.getInt("id");
				hashtable.put(uid, "" + id);
				scannedList.add(uid);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	
	public void doSet(View v) {
		Intent intent = new Intent();
		intent.setClass(this, SettingActivity.class);
		startActivity(intent);
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
						bcr = BarCodeReader.open(1); // Android 2.3

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
