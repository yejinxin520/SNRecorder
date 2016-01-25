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
import com.hy.util.ConfigurationSet;
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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OffLineActivity extends Activity implements netEventHandler{

	public static final String ACTION_BARCODE_SERVICE_BROADCAST = "action_barcode_broadcast";
	public static final String KEY_BARCODE_STR = "key_barcode_string";
	private BarcodeReceiver barcodeReceiver;
	public static final String KEY_ACTION = "KEY_ACTION";
	public static final String TONE = "TONE=100";
	private Intent intentService = new Intent(
			"com.hyipc.core.service.barcode.BarcodeService2D");
	private Spinner spinner;
	private RelativeLayout ulrl;
	private TextView barcode,netstate,offbartv,barcode1,saved;
	private ProgressDialog dialog;
	private String filename,barcodestr="",modelstr,url,resultstr;
	private int localnum=0,offset = 0,total_count,visnum,total,scanTimes,scannedTimes=0;
	private int[] barcodelimit;
	private Boolean init;
	private JSONObject jsonObject;
	private SwipeMenuListView localListV;
	private Hashtable<String, String> localhash,idhash,tasknumhash,savehash,uploadedhash;
	private List<String> data_list,locallist,scannedList,templist,scantemp;
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
			if(scannedTimes < scanTimes){
				if (s.length() == barcodelimit[scannedTimes]
						|| barcodelimit[scannedTimes] == 0) {
					if(savehash.containsKey(s)||scantemp.contains(s)
							||uploadedhash.containsKey(s)){
						netstate.setText("该条码已上传或已离线！");						
					}else {
						if (scannedTimes == 0){
							barcode.setText(s);
							barcodestr = s;
							netstate.setText("条码1扫描完成");
						}
						else if (scannedTimes == 1){
							barcode1.setText(s);
							barcodestr = barcodestr+"_"+s;
							netstate.setText("条码2扫描完成");
						}						
						scannedTimes++;
						scantemp.add(s);
						}
				}else {
					netstate.setText("该条码长度不匹配！");					
				}
			}else {
				netstate.setText("扫描已完成，请上传或者清除！");
			}
			if(scannedTimes == scanTimes && autoupload){
				save(filename,barcodestr);	
				scannedTimes = 0;
				scantemp.clear();
			}
		};
	};
	private Boolean autoupload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_offline);
		idhash = new Hashtable<String, String>();
		tasknumhash = new Hashtable<String, String>();
		savehash = new Hashtable<String, String>();
		uploadedhash = new Hashtable<String, String>();
		data_list = new ArrayList<String>();
		scannedList = new ArrayList<String>();
		scantemp = new ArrayList<String>();
		//idQuery();
		NetBroadcastReceiver.mListeners.add(this);
		offLineService = new FileHandler(this);
		spinner = (Spinner)findViewById(R.id.modeltype);
		barcode = (TextView)findViewById(R.id.barcodeoffline);
		netstate = (TextView)findViewById(R.id.netstatetv);
		ulrl = (RelativeLayout)findViewById(R.id.ulrl);
		localListV = (SwipeMenuListView)findViewById(R.id.locallist);
		saved = (TextView)findViewById(R.id.savedstr);
		saved.setText("0");
		offbartv = (TextView)findViewById(R.id.barcodeofflinetv1);
		barcode1 = (TextView)findViewById(R.id.barcodeoffline1);
		autoupload = ConfigurationSet.getAutoUpload();
		scanTimes = ConfigurationSet.getSanTimes();
		barcodelimit = new int[3];
		barcodelimit[0] = ConfigurationSet.getBarcodeLimit1();
		barcodelimit[1] = ConfigurationSet.getBarcodeLimit2();
		if (scanTimes > 1) {
			offbartv.setVisibility(View.VISIBLE);
			scanTimes =2;
		}
        data_list.add("For TEST");
        data_list.add("V68");        
        data_list.add("PE900S");
        data_list.add("RD1000");
        data_list.add("RD2000");
        data_list.add("RD3000");
        data_list.add("RD5000");
        data_list.add("P1230");
        data_list.add("P1213");
        data_list.add("P1202");
        data_list.add("P1512");
        data_list.add("P1522");
        data_list.add("P1500");
        data_list.add("PD805");
        data_list.add("PD800");
        data_list.add("PD801");
        data_list.add("AP7200");
        data_list.add("P1302");
        data_list.add("P1312");
        data_list.add("P1322");
        
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
				saved.setText(localnum+"");
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
        intentService.putExtra(KEY_ACTION, TONE);
		this.startService(intentService);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println(event.getKeyCode());
		if (((keyCode == 135) || (keyCode == 136) || (keyCode == 134) || (keyCode == 137))
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			doScan(findViewById(R.id.qscanbtn));

			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
			doSave(findViewById(R.id.savebtn));
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	public void doScan(View v) {
		if(bcr!=null){
			decodeMethod.doDecode();
		}else {
			intentService.putExtra(KEY_ACTION, "UP");
			this.startService(intentService);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			intentService.putExtra(KEY_ACTION, "DOWN");
			this.startService(intentService);
		}
		
	}
	public void doSave(View v) {
		if (scannedTimes == scanTimes) {
			if(barcodestr!=""){
				save(filename,barcodestr);	
				scannedTimes = 0;
				scantemp.clear();
			}else {
				netstate.setText("未扫描条码！");
			}
		}else {
			netstate.setText("扫描未完成！");
		}		
	}
	public void doClear(View v) {
		barcode.setText("");
		barcode1.setText("");
		barcodestr = "";
		netstate.setText("请重新扫描！");
		scannedTimes = 0;
		scantemp.clear();
	}
	public void doUpload(View v) {
		
		if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
			dialog = new ProgressDialog(this);
			dialog.setTitle("请稍等");
			dialog.setMessage("正在上传");
			dialog.show();
			int totaltemp = total;
			if(templist.size()+totaltemp <= Integer.parseInt(tasknumhash.get(modelstr))){
				for (int j = 0; j < templist.size(); j++) {
					if(!scannedList.contains(templist.get(j).toString())){						
							dopost(templist.get(j).toString());	
					}
					else {
						Toast.makeText(getApplicationContext(), "条码"+templist.get(j).toString()+"重复记录，将删除！", 
								Toast.LENGTH_SHORT).show();
						locallist.remove(templist.get(j).toString());
						if(locallist.isEmpty()){
							hander.sendEmptyMessage(1);
							templist.clear();
						}
					}
				}
			}else {
				int temp = templist.size()+total-Integer.parseInt(tasknumhash.get(modelstr));
				Builder msgBox = new Builder(OffLineActivity.this);
				msgBox.setTitle("提示");
				if(total==Integer.parseInt(tasknumhash.get(modelstr))){
					msgBox.setMessage("任务已完成");
				}else {
					msgBox.setMessage("本地保存超过任务数，请删除"+temp+"条记录");
				}
				
				msgBox.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				});				
				msgBox.create().show();
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
				total++;
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
					if(total == Integer.parseInt(tasknumhash.get(modelstr))){
						netstate.setText("上传完成");
					}else if(total < Integer.parseInt(tasknumhash.get(modelstr))){
						netstate.setText("还差"+(Integer.parseInt(tasknumhash.get(modelstr))-total)+"个条码");
					}
					
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
				String[] tmpstr=barcodestr.split("_");							
				for(int j=0;j<tmpstr.length;j++)
				{
					savehash.put(tmpstr[j],modelstr);
				}
				if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
					hander.sendEmptyMessage(4);
				}
				saved.setText("本地已保存");
				barcode.setText("");
				barcode1.setText("");
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
            	saved.setText(localnum+"");
                localListV.setSelection(localnum-1);
                break;
            case 1:
            	ulrl.setVisibility(View.GONE);
            	dialog.dismiss();
            	FileHandler.deleteFile(filename);
            	break;
            case 2:
            	init = true;				
				scannedList.clear();
				uploadedhash.clear();
				httpQuery();
				break;
			case 3:
				reWritefile();
				break;
			case 4:
				if(idhash.containsKey(modelstr)){
        			netstate.setText("已连接wifi,可上传");
            		ulrl.setVisibility(View.VISIBLE);
        		}else {
            		netstate.setText("已连接wifi,但无此任务");
            		ulrl.setVisibility(View.GONE);
    			}
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
				localnum = filestr.length;
				for(int i=0;i<filestr.length;i++){
					locallist.add(filestr[i]);System.out.println(filestr[i]);
					templist.add(filestr[i]);
					localhash.put(filestr[i], modelstr);
					String[] tmpstr=filestr[i].split("_");							
					for(int j=0;j<tmpstr.length;j++)
					{
						savehash.put(tmpstr[j],modelstr);
					}
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			localnum = 0;
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
								String taskNum = jsonObject.getJSONArray("objects")
								.getJSONObject(i).getString("number");
								idhash.put(model,id);	
								tasknumhash.put(model, taskNum);
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
		if(idhash.containsKey(modelstr)){
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
								total = total_count;
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
		}else {
			
		}
		
	}
	private void initList() {
		
		int tempnum = total_count > 20 ? 20 : total_count;
		for (int i = 0; i < tempnum; i++) {
			String uid = "";
			try {
				uid = jsonObject.getJSONArray("objects").getJSONObject(i)
						.getString("UID");
				scannedList.add(uid);
				String[] tmpstr=uid.split("_");							
				for(int j=0;j<tmpstr.length;j++)
				{
					uploadedhash.put(tmpstr[j], "");
				}
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
							String[] tmpstr=locallist.get(position).split("_");							
							for(int j=0;j<tmpstr.length;j++)
							{
								savehash.remove(tmpstr[j]);
							}
							locallist.remove(position);
							localnum--;
							templist.remove(position);
							localadapter.notifyDataSetChanged();
							saved.setText(localnum+"");
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
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在删除");
		dialog.show();
		String tempstr="";		
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
		unregisterReceiver(barcodeReceiver);
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
		intentService.putExtra(KEY_ACTION, "INIT");
		this.startService(intentService);
		barcodeReceiver = new BarcodeReceiver(this);
        barcodeReceiver.registerAction(ACTION_BARCODE_SERVICE_BROADCAST);
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

					decodeMethod.decodeinit(bcr,getApplicationContext());
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
            ulrl.setVisibility(View.VISIBLE);
        }else if(NetUtil.getNetworkState(this) == NetUtil.NETWORN_WIFI){
        	idQuery();
        	if(!locallist.isEmpty()){        		
        		Thread t = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						try {
							Thread.sleep(500);
							hander.sendEmptyMessage(4);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
        		t.start();
        		
        	}else {
        		netstate.setText("已连接wifi,本地无内容");
        		ulrl.setVisibility(View.GONE);
			}
        }
	}
	class BarcodeReceiver extends BroadcastReceiver {

		public static final String ACTION_BARCODE_SERVICE_BROADCAST = "action_barcode_broadcast";
		public static final String KEY_BARCODE_STR = "key_barcode_string";
		Context ct=null;
	    BroadcastReceiver receiver;
	    String barcodeString="";
		public BarcodeReceiver(Context context) {
			// TODO Auto-generated constructor stub
			ct = context;
			receiver=this;
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_BARCODE_SERVICE_BROADCAST)){
				barcodeString = intent.getExtras().getString(KEY_BARCODE_STR);				
			}
			if(barcodeString.length()>0){
				Message msg = new Message();
				Bundle bundle = new Bundle();
				bundle.putString("barc", barcodeString);
				msg.setData(bundle);
				OffLineActivity.this.handler.sendMessage(msg);
			}
		}

		//注册
	    public void registerAction(String action){
	        IntentFilter filter=new IntentFilter();
	        filter.addAction(action);
	        ct.registerReceiver(receiver, filter);
	    }
	    
	}
}
