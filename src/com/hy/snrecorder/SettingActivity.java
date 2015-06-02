package com.hy.snrecorder;

import java.io.FileOutputStream;

import net.simonvt.numberpicker.NumberPicker;

import com.hy.util.ConfigurationSet;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class SettingActivity extends Activity {
	
	private ToggleButton toggleButton;
	private RadioButton radioButton1;
	private RadioButton radioButton2;
	private RadioButton radioButton3;
	private boolean autoupload;
	private int scanTimes;
	private int barcode1Limit;
	private int barcode2Limit;
	private int barcode3Limit;
	RelativeLayout rl;
	ViewGroup vg ;
	int hasadd = 0,hasadd1 = 0;
	NumberPicker np1,np2,np3;
	String writestr = "";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_setting);
		
		radioButton1 = (RadioButton)findViewById(R.id.rb1);
		radioButton2 = (RadioButton)findViewById(R.id.rb2);
		radioButton3 = (RadioButton)findViewById(R.id.rb3);
		autoupload = ConfigurationSet.getAutoUpload();
		scanTimes = ConfigurationSet.getSanTimes();
		barcode1Limit = ConfigurationSet.getBarcodeLimit1();
		barcode2Limit = ConfigurationSet.getBarcodeLimit2();
		barcode3Limit = ConfigurationSet.getBarcodeLimit3();
		toggleButton = (ToggleButton)findViewById(R.id.isupload);
		toggleButton.setChecked(autoupload);
		np1 = (NumberPicker) findViewById(R.id.numberPicker);
		np1.setMaxValue(20);
		np1.setMinValue(0);
		np1.setFocusable(true);
		np1.setFocusableInTouchMode(true);
		vg = (ViewGroup)findViewById(R.id.container);
		toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton view, boolean ischecked) {
				// TODO Auto-generated method stub
				autoupload = ischecked;
			}
		});
		switch (scanTimes) {
		case 1:
			radioButton1.setChecked(true);
			np1.setValue(barcode1Limit);
			break;
		case 2:
			radioButton2.setChecked(true);
			inflateview(0);
			hasadd = 1;
			np2 = (NumberPicker)this.findViewById(0);
			np2.setValue(barcode2Limit);
			break;
		case 3:
			radioButton3.setChecked(true);
			np1.setValue(barcode1Limit);
			inflateview(0);
			hasadd = 1;
			np2 = (NumberPicker)this.findViewById(0);
			np2.setValue(barcode2Limit);
			inflateview(1);
			hasadd1 = 1;
			np3 = (NumberPicker)this.findViewById(1);
			np3.setValue(barcode3Limit);
			break;

		default:
			break;
		}
		
		
	}
	public void doRadioClick(View view) {
		switch (view.getId()) {
		case R.id.rb1:
			scanTimes = 1;
			
			if(hasadd1 == 1){
				vg.removeViewAt(5);
				hasadd1 = 0;
			}
			if(hasadd ==1){
				vg.removeViewAt(4);
				hasadd = 0;
			}
			break;
        case R.id.rb2:
			scanTimes = 2;
			if(hasadd == 0){
				inflateview(0);
				hasadd = 1;
			}
			if(hasadd1 == 1){
				vg.removeViewAt(5);
				hasadd1 = 0;
			}
			break;
        case R.id.rb3:
			scanTimes = 3;
			if(hasadd1 == 0){
				if(hasadd == 0){
					inflateview(0);
					hasadd = 1;
				}
				inflateview(1);
				hasadd1 = 1;
			}
			break;

		default:
			break;
		}
	}
	public void doConfirm(View v) {
		barcode1Limit = np1.getValue();
		if(hasadd == 1){
			np2 = (NumberPicker)this.findViewById(0);
			barcode2Limit = np2.getValue();
		}
		if(hasadd1 == 1){
			np3 = (NumberPicker)this.findViewById(1);
			barcode3Limit = np3.getValue();
		}	
		ConfigurationSet.setAutoUpload(autoupload);
		ConfigurationSet.setSanTimes(scanTimes);
		ConfigurationSet.setBarcodeLimit1(barcode1Limit);
		ConfigurationSet.setBarcodeLimit2(barcode2Limit);
		ConfigurationSet.setBarcodeLimit3(barcode3Limit);
		writestr +=autoupload+"\n"+scanTimes+"\n"+barcode1Limit+"\n"+barcode2Limit+"\n"+barcode3Limit;
		
		try{
			FileOutputStream fos = openFileOutput("conf", MODE_PRIVATE);
			byte [] bytes = writestr.getBytes();
			fos.write(bytes);
			fos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		this.finish();
	}
	private void inflateview(int index){
		rl = new RelativeLayout(this);
		rl.setPadding(20, 0, 20, 0);
		
		rl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));
		vg.addView(rl);
		NumberPicker np = new NumberPicker(this);
		np.setMaxValue(20);
		np.setMinValue(0);
		np.setFocusable(true);
		np.setFocusableInTouchMode(true);np.setId(index);
		RelativeLayout.LayoutParams margin = new RelativeLayout.LayoutParams(50, 60);
		margin.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		margin.setMargins(5, 0, 0, 0);
		//np.setLayoutParams(new LayoutParams(40, 50));
		TextView tv = new TextView(this);
		if(index == 0)
		tv.setText(R.string.length_limit2);
		if(index == 1)
			tv.setText(R.string.length_limit3);
		tv.setTextSize(15);
		RelativeLayout.LayoutParams margin1 = 
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		margin1.addRule(RelativeLayout.LEFT_OF, index);	
		margin1.addRule(RelativeLayout.CENTER_VERTICAL);
		rl.addView(np, margin);
		rl.addView(tv,margin1);
	}
}
