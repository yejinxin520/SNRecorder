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

import com.baoyz.swipemenulistview.SwipeMenuListView;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
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
	private String filename,barcodestr,modelstr,url,resultstr;
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
					barcode.setText("�����������߻����ϴ���");
				//}
				//else {
					//barcode.setText("�����������ߵ�����"+localhash.get(s));
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
      //������
        arr_adapter= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data_list);
        //������ʽ
        arr_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //����������
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
		if(barcodestr!=null){
			if(!scannedList.contains(barcodestr)){
				save(filename,barcodestr);	
			}else {
				barcode.setText("�������ϴ���");
			}
		}else {
			barcode.setText("����ûɨ�裡");
		}
	}
	public void doClear(View v) {
		barcode.setText("");		
	}
	public void doUpload(View v) {
		
		if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
			dialog = new ProgressDialog(this);
			dialog.setTitle("���Ե�");
			dialog.setMessage("�����ϴ�");
			dialog.show();
			for (int j = 0; j < templist.size(); j++) {
				if(!scannedList.contains(templist.get(j).toString())){					
					dopost(templist.get(j).toString());			
				}
				else {
					Toast.makeText(getApplicationContext(), "����"+templist.get(j).toString()+"�Ѽ�¼", 
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
		dialog.setTitle("���Ե�");
		dialog.setMessage("�������߱���");
		dialog.show();		
		try {
			offLineService.save(filename, barcodestr);
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				offLineService.saveToSDCard(filename, barcodestr);	
				locallist.add(barcodestr);
				localnum++;
				templist.add(barcodestr);
				localhash.put(barcodestr, modelstr);
				if(NetUtil.getNetworkState(this)!=NetUtil.NETWORN_NONE){
					netstate.setText("��⵽������wifi�Ƿ��ϴ���");
					uploadlocal.setVisibility(View.VISIBLE);
				}
				
				barcode.setText("");
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
				Toast.makeText(getApplicationContext(), "����ɹ�", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "sd�������ڻ�д�뱣��", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			dialog.dismiss();
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), "����ʧ��", Toast.LENGTH_SHORT).show();
		}
	}
	@SuppressLint("HandlerLeak")
	private Handler hander = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
            case 0:
            	localadapter.notifyDataSetChanged(); //������Ϣ֪ͨListView����
                localListV.setSelection(localnum);
                break;
            case 1:
            	netstate.setText("�ϴ����");
            	uploadlocal.setVisibility(View.GONE);
            	dialog.dismiss();
            	FileHandler.deleteFile(filename);
            case 2:
            	init = true;				
				scannedList.clear();
				httpQuery();
				
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
			System.out.println("�ļ�������");
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
					System.out.println("�������");
				} else {					
					resultstr = result.toString();
					System.out.println(result.toString());
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
						System.out.println("json��������");
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
					System.out.println("�����������ʧ��");
					
				} else {
					resultstr = result.toString();System.out.println(result.toString());
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
            netstate.setText("�޿������磡");
            uploadlocal.setVisibility(View.GONE);
        }else if(NetUtil.getNetworkState(this) == NetUtil.NETWORN_WIFI){
        	idQuery();
        	if(!locallist.isEmpty()){
        		netstate.setText("��⵽������wifi�Ƿ��ϴ���");
        		uploadlocal.setVisibility(View.VISIBLE);
        	}else {
        		netstate.setText("������wifi");
        		uploadlocal.setVisibility(View.GONE);
			}
        }
	}
}
