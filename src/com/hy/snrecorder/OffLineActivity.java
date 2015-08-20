package com.hy.snrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import com.baoyz.swipemenulistview.SwipeMenuListView;
import com.hy.util.FileHandler;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OffLineActivity extends Activity {

	private Spinner spinner;
	private TextView barcode;
	private ProgressDialog dialog;
	private String filename,barcodestr;
	private int localnum=0;
	private SwipeMenuListView localListV;
	private Hashtable<String, String> localhash;
	private List<String> data_list,locallist;
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
			if(localhash.containsKey(s)){
				barcode.setText("该条码已离线到本地！");
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
		offLineService = new FileHandler(this);
		spinner = (Spinner)findViewById(R.id.modeltype);
		barcode = (TextView)findViewById(R.id.barcodeoffline);
		localListV = (SwipeMenuListView)findViewById(R.id.locallist);
		
		data_list = new ArrayList<String>();
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
				read(filename);
				localadapter.notifyDataSetChanged();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
        locallist = new ArrayList<String>();
        localhash = new Hashtable<String, String>();
        //filename = spinner.getSelectedItem().toString()+".txt";
        //read(filename);
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
			//doSave(findViewById(R.id.savebtn));
			read(filename);
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
		filename = spinner.getSelectedItem().toString()+".txt";
		if(!barcodestr.equals(""))
			save(filename,barcodestr);		
	}
	public void doClear(View v) {
		barcode.setText("");
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
				offLineService.saveToSDCard(filename, barcodestr);	
				locallist.add(barcodestr);
				localnum++;
				localhash.put(barcodestr, ""+localnum);
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
            default:
                break;
            }
        }
    };
	private void read(String filename){
		String SDPATH = Environment.getExternalStorageDirectory().getPath() + "/";
		File f = new File(SDPATH +filename);
		locallist.clear();
		if(f.exists()){
			try {
				String temp = offLineService.readFile(filename);
				String [] filestr = temp.split(",");
				localnum = filestr.length-1;
				for(int i=0;i<filestr.length;i++){
					locallist.add(filestr[i]);System.out.println(filestr[i]);
					localhash.put(filestr[i], ""+i);
				}
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else {
			System.out.println("文件不存在");
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
}
