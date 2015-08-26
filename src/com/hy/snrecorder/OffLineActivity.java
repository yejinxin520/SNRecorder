package com.hy.snrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

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
import com.hy.util.FileHandler;
import com.hy.util.HttpHandler;
import com.hy.util.NetBroadcastReceiver;
import com.hy.util.NetBroadcastReceiver.netEventHandler;
import com.hy.util.NetUtil;
import com.motorolasolutions.adc.decoder.BarCodeReader;
import com.motorolasolutions.adc.decoder.DecodeUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OffLineActivity extends Activity implements netEventHandler{

	private Spinner spinner;
	private TextView barcode,netstate;
	private Button uploadlocal;
	private ProgressDialog dialog;
	private String filename,barcodestr="",modelstr,url,resultstr;
	private int localnum=0,offset = 0,total_count,visnum;
	private Boolean init;
	private JSONObject jsonObject;
	private SwipeMenuListView localListV;
	private Hashtable<String, String> localhash,idhash;
	private List<String> data_list,locallist,scannedList,templist;
	private ArrayAdapter<String> arr_adapter,localadapter;
	private FileHandler offLineService;
	DecodeUtil decodeMethod = new DecodeUtil();
	private BarCodeReader bcr = null;
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Bundle bundle = msg.getData();
			String s = bundle.getString("barc");			
			if(localhash.containsKey(s)||scannedList.contains(s)){
				//if(localhash.get(s).equals(modelstr)){
					barcode.setText("该条码已离线或已上传！");
				//}
				//else {
					//barcode.setText("该条码已离线到机型"+localhash.get(s));
				//}
			}else {
				barcode.setText(s);
				barcodestr = s;
			}			
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_offline);
		idhash = new Hashtable<String, String>();
		data_list = new ArrayList<String>();
		scannedList = new ArrayList<String>();
		//idQuery();
		NetBroadcastReceiver.mListeners.add(this);
		offLineService = new FileHandler(this);
		spinner = (Spinner)findViewById(R.id.modeltype);
		barcode = (TextView)findViewById(R.id.barcodeoffline);
		netstate = (TextView)findViewById(R.id.netstatetv);
		uploadlocal = (Button)findViewById(R.id.uploadlocal);
		localListV = (SwipeMenuListView)findViewById(R.id.locallist);
		
        data_list.add("For TEST");
        data_list.add("V68");
        data_list.add("P1230");
        data_list.add("PE900S");
        data_list.add("P1213");
        data_list.add("P1522");
        data_list.add("PD805");
      //适配器
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //设置样式
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //加载适配器
        spinner.setAdapter(arr_adapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long id) {
				// TODO Auto-generated method stub
				filename = data_list.get(position)+".txt";
				modelstr = data_list.get(position);
				read(filename);
				localadapter.notifyDataSetChanged();
				onNetChange();
				//hander.sendEmptyMessage(2);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        
        locallist = new ArrayList<String>();
        templist = new ArrayList<String>();
        localhash = new Hashtable<String, String>();
        localadapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,locallist);
        localListV.setAdapter(localadapter); 
        initSwipeListView();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println(event.getKeyCode());
		if (((keyCode == 135) || (keyCode == 136) )
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			doScan(findViewById(R.id.qscanbtn));
			return true;
		}
		if(keyCode == 137&& event.getAction() == KeyEvent.ACTION_DOWN){
			doSave(findViewById(R.id.savebtn));
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
				OffLineActivity.this.handler.sendMessage(msg);

			}
		});
		t.start();
	}
	public void doSave(View v) {
		if(barcodestr!=""){
			if(!scannedList.contains(barcodestr)){
				save(filename,barcodestr);	
			}else {
				barcode.setText("条码已上传！");
			}
		}else {
			barcode.setText("条码没扫描！");
		}
	}
	public void doClear(View v) {
		barcode.setText("");		
	}
	public void doUpload(View v) {
		
		if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
			dialog = new ProgressDialog(this);
			dialog.setTitle("请稍等");
			dialog.setMessage("正在上传");
			dialog.show();
			for (int j = 0; j < templist.size(); j++) {
				if(!scannedList.contains(templist.get(j).toString())){					
					dopost(templist.get(j).toString());			
				}
				else {
					Toast.makeText(getApplicationContext(), "条码"+templist.get(j).toString()+"已记录", 
							Toast.LENGTH_SHORT).show();
					locallist.remove(templist.get(j).toString());
					//dialog.dismiss();
					if(locallist.isEmpty()){
						hander.sendEmptyMessage(1);
						templist.clear();
					}
				}
			}
		}
		
	}
	private void dopost(final String uid) {
		
		final String urlpath = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&"
				+ "username=tomsu&api_key=123456";		
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				scannedList.add(uid);
				locallist.remove(uid);
				localnum--;
				localhash.remove(uid);
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(500);
							hander.sendEmptyMessage(0);
							
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				t.start();
				if(locallist.isEmpty()){
					netstate.setText("上传完成");
					hander.sendEmptyMessage(1);
					templist.clear();
				}
			}

			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub
				HttpPost httpPost = new HttpPost(urlpath);
				try {
					String json = "";										
					JSONObject jsonObject = new JSONObject();					
                    jsonObject.accumulate("task", "/mary/sellrec/api/task/"
							+ idhash.get(modelstr) + "/");
					jsonObject.accumulate("UID", uid);
					json = jsonObject.toString();
					StringEntity se = new StringEntity(json);
					httpPost.setEntity(se);
					httpPost.setHeader("Accept", "application/json");
					httpPost.setHeader("Content-type", "application/json");
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return httpPost;
			}
		}.execute();
	}
	private void save(String filename,String barcodestr){
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在离线保存");
		dialog.show();		
		try {
			offLineService.save(filename, barcodestr);
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				offLineService.saveToSDCard(filename, barcodestr,true);	
				locallist.add(barcodestr);
				localnum++;
				templist.add(barcodestr);
				localhash.put(barcodestr, modelstr);
				if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
					netstate.setText("检测到已连接wifi是否上传？");
					uploadlocal.setVisibility(View.VISIBLE);
				}
				
				barcode.setText("");
				this.barcodestr="";
				Thread t = new Thread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(500);
							hander.sendEmptyMessage(0);
							dialog.dismiss();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
				t.start();
				Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "sd卡不存在或被写入保护", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			dialog.dismiss();
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "保存失败", Toast.LENGTH_SHORT).show();
		}
	}
	@SuppressLint("HandlerLeak")
	private Handler hander = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
            case 0:
            	localadapter.notifyDataSetChanged(); //发送消息通知ListView更新
                localListV.setSelection(localnum);
                break;
            case 1:
            	
            	uploadlocal.setVisibility(View.GONE);
            	dialog.dismiss();
            	FileHandler.deleteFile(filename);
            	break;
            case 2:
            	init = true;				
				scannedList.clear();
				httpQuery();
				break;
			case 3:
				reWritefile();
				break;
            default:
                break;
            }
        }
    };
	private void read(String filename){
		String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
		File f = new File(SDPATH +filename);
		locallist.clear();
		templist.clear();
		if(f.exists()){
			try {
				String temp = offLineService.readFile(filename);
				String [] filestr = temp.split(",");
				localnum = filestr.length-1;
				for(int i=0;i<filestr.length;i++){
					locallist.add(filestr[i]);System.out.println(filestr[i]);
					templist.add(filestr[i]);
					localhash.put(filestr[i], modelstr);
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			System.out.println("文件不存在");
		}
	}
	private void idQuery() {		
		url = "http://192.168.0.201/mary/sellrec/api/task/"
				+ "?formfat=json?&username=tomsu&api_key=123456&finished=0&p=0";
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if (result == "") {
					System.out.println("网络错误");
				} else {					
					resultstr = result.toString();					
					jsonObject = new JSONObject();
					try {
						jsonObject = new JSONObject(resultstr);
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						int count = jsonObject.getJSONObject("meta").getInt("total_count");
						if (count > 0) {
							for (int i = 0; i < count; i++) {
								String id =jsonObject.getJSONArray("objects").getJSONObject(i).getString("id");
								String model = jsonObject.getJSONArray("objects")
										.getJSONObject(i).getString("model");
								idhash.put(model,id);								
							}
							hander.sendEmptyMessage(2);
						}
					} catch (Exception e) {
						System.out.println("json解析错误");
						e.printStackTrace();
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
		offset = 0;
		url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
				+ "offset=" + offset + "&finished=0&task=" + idhash.get(modelstr) + "&p=0";
		new HttpHandler() {

			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if (result == "") {					
					System.out.println("网络错误，连接失败");
					
				} else {
					resultstr = result.toString();
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
								+ idhash.get(modelstr) + "&p=0";
						new AsyncHttpTask(this).execute();
					} else {
						initList();
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
	private void initList() {
		
		int tempnum = total_count > 20 ? 20 : total_count;
		for (int i = 0; i < tempnum; i++) {
			String uid = "";
			try {
				uid = jsonObject.getJSONArray("objects").getJSONObject(i)
						.getString("UID");
				scannedList.add(uid);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initSwipeListView(){
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
		localListV.setMenuCreator(creator);
		localListV.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@Override
			public void onMenuItemClick(final int position, SwipeMenu menu,
					int index) {
				// TODO Auto-generated method stub
				switch (index) {
				case 0:
					Builder msgBox = new Builder(OffLineActivity.this);
					msgBox.setTitle("提示");
					msgBox.setMessage("您确定要删除这条记录吗");
					msgBox.setPositiveButton("确定", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// TODO Auto-generated method stub
							localhash.remove(locallist.get(position));
							locallist.remove(position);
							templist.remove(position);
							localadapter.notifyDataSetChanged();
							//FileHandler.deleteFile(filename);
							hander.sendEmptyMessage(3);
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
	private int dp2px(int dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getResources().getDisplayMetrics());
	}
	private void reWritefile(){
		String tempstr="";
		//int i=0;
		if(!locallist.isEmpty()){
			for(int i=0;i<locallist.size()-1;i++){
				
				tempstr += locallist.get(i)+",";
			}
			tempstr += locallist.get(locallist.size()-1);
			rsave(tempstr);
		}else {
			//FileHandler.deleteFile(filename);
			netstate.setText("本地无内容");
			hander.sendEmptyMessage(1);
		}
	}
	private void rsave(String tempstr){
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在删除");
		dialog.show();
		try {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				offLineService.saveToSDCard(filename, tempstr,false);					
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
				Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "sd卡不存在或被写入保护", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			dialog.dismiss();
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
		}
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
	@Override
	public void onNetChange() {
		// TODO Auto-generated method stub
		if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
            netstate.setText("无可用网络！");
            uploadlocal.setVisibility(View.GONE);
        }else if(NetUtil.getNetworkState(this) == NetUtil.NETWORN_WIFI){
        	idQuery();
        	if(!locallist.isEmpty()){
        		netstate.setText("检测到已连接wifi是否上传？");
        		uploadlocal.setVisibility(View.VISIBLE);
        	}else {
        		netstate.setText("已连接wifi");
        		uploadlocal.setVisibility(View.GONE);
			}
        }
	}
}
