package com.hy.snrecorder;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.hy.util.AsyncHttpTask;
import com.hy.util.ConfigurationSet;
import com.hy.util.HttpHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerCloseListener;
import android.widget.SlidingDrawer.OnDrawerOpenListener;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class DetailActivity extends Activity {

	private int offset = 0;
	private String idMessage,model,tasknumber,url,resultstr;
	private Boolean autoUpload,init;
	private int scanTimes,total_count,visnum;
	private int [] barcodelimit;
	private TextView modeltv,total,scanned,barcode1;
	private ProgressDialog dialog;
	private LinearLayout llcontainer;
	private SlidingDrawer drawer;
	private ImageView imageView;
	private ListView scannedListV;
	private List<String> scannedList;
	private ArrayAdapter<String> adapter;
	private JSONObject jsonObject;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_details);
		Intent intent = getIntent();
		idMessage = intent.getStringExtra("idmessage");
		model = intent.getStringExtra("modelmessage");
		tasknumber = intent.getStringExtra("numbermessage");
		url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
		+ "offset="+offset+"&finished=0&task="+idMessage+"&p=0";
		init = true;
		httpQuery();
		
		autoUpload = ConfigurationSet.getAutoUpload();
		scanTimes = ConfigurationSet.getSanTimes();
		barcodelimit = new int [3];
		barcodelimit[0] = ConfigurationSet.getBarcodeLimit1();
		barcodelimit[1] = ConfigurationSet.getBarcodeLimit2();
		barcodelimit[2] = ConfigurationSet.getBarcodeLimit3();
		modeltv = (TextView)findViewById(R.id.modeltv);
		modeltv.setText(model);
		total = (TextView)findViewById(R.id.total);
		total.setText(tasknumber);
		scanned = (TextView)findViewById(R.id.scanned);
		barcode1 = (TextView)findViewById(R.id.barstr);
		llcontainer = (LinearLayout)findViewById(R.id.container1);
		if(scanTimes == 2){
			llcontainer.removeViews(4, 2);
		}
		if(scanTimes == 1){
			llcontainer.removeViews(2, 4);
		}
		drawer = (SlidingDrawer)findViewById(R.id.sduidlist);
		imageView = (ImageView)findViewById(R.id.open);
		drawer.setOnDrawerOpenListener(new OnDrawerOpenListener() {
			
			@Override
			public void onDrawerOpened() {
				// TODO Auto-generated method stub
				imageView.setImageResource(R.drawable.ic_launcher);
			}
		});
		drawer.setOnDrawerCloseListener(new OnDrawerCloseListener() {
			
			@Override
			public void onDrawerClosed() {
				// TODO Auto-generated method stub
				imageView.setImageResource(R.drawable.ic_arrow);
			}
		});
		scannedListV = (ListView)findViewById(R.id.contentlist);
		scannedList = new ArrayList<String>();

	}
	private void httpQuery() {
		dialog = new ProgressDialog(this);
		dialog.setTitle("请稍等");
		dialog.setMessage("正在获取任务");
		dialog.show();
		new HttpHandler() {
			
			@Override
			public void onResponse(String result) {
				// TODO Auto-generated method stub
				if(result==""){
					AlertDialog.Builder msgBox = new Builder(DetailActivity.this);
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
				}
				else{
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
					try{
						jsonObject = new JSONObject(resultstr);
					}catch(Exception e){
						e.printStackTrace();
					}
					if(init){
						try{
							total_count = jsonObject.getJSONObject("meta").getInt("total_count");
							init = false;
							visnum = total_count;
						}catch(JSONException e){
							e.printStackTrace();
						}
					}
					else {
						visnum -= 20;
					}
					if(visnum > 20){
						initList();
						offset += 20;
						url = "http://192.168.0.201/mary/sellrec/api/collect/?format=json&username=tomsu&api_key=123456&"
								+ "offset="+offset+"&finished=0&task="+idMessage+"&p=0";
						new AsyncHttpTask(this).execute();
					}else{
						initList();
					}
					scanned.setText(""+total_count);
					adapter = new ArrayAdapter<String>(DetailActivity.this, android.R.layout.simple_list_item_1,scannedList);
					scannedListV.setAdapter(adapter);
				}
			}
			
			@Override
			public HttpUriRequest getRequestMethod() {
				// TODO Auto-generated method stub
				return new HttpGet(url);
			}
		}.execute();
	}
	public void doScan(View v){
		barcode1.setText("123456789012");
	}
	private void initList() {
		int tempnum = total_count>20?20:total_count;
		for(int i=0;i<tempnum;i++){
			try{
				String uid = jsonObject.getJSONArray("objects").getJSONObject(i).getString("UID");
				scannedList.add(uid);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

}
