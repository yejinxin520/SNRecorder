package com.hy.snrecorder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.hy.util.TaskAdapter;

import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ListView;

public class TaskActivity extends ListActivity {

	private List<Map<String, Object>> data;
	private JSONObject jsonObject;
	String taskstr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_task);
		Intent intent = getIntent();
		taskstr = intent.getStringExtra("taskmessage");
		// taskList = (ListView)findViewById(R.id.tasklist);
		jsonObject = new JSONObject();
		try {
			jsonObject = new JSONObject(taskstr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {

			int count = jsonObject.getJSONObject("meta").getInt("total_count");
			if (count > 0) {
				data = new ArrayList<Map<String, Object>>();
				for (int i = 0; i < count; i++) {
					String model = jsonObject.getJSONArray("objects")
							.getJSONObject(i).getString("model");
					String snum = jsonObject.getJSONArray("objects")
							.getJSONObject(i).getString("number");
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("image", R.drawable.ic_device);
					map.put("content", snum + "个" + model + "机器码");
					data.add(map);
				}
				setListAdapter(new TaskAdapter(this, data));

			} else {
				Builder b = new Builder(this);
				b.setTitle("提示");
				b.setMessage("没有未完成的任务");
				b.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								finish();
							}
						});
				b.create().show();
			}
		} catch (JSONException e) {
			System.out.println("json解析错误");
			e.printStackTrace();
			Builder b = new Builder(this);
			b.setTitle("提示");
			b.setMessage("错误的数据,检查URL或联系管理员");
			b.setPositiveButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			b.create().show();
		}
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		try {

			Intent detail = new Intent(TaskActivity.this, RecordActivity.class);
			detail.putExtra("idmessage", jsonObject.getJSONArray("objects")
					.getJSONObject(position).getString("id"));
			detail.putExtra("modelmessage", jsonObject.getJSONArray("objects")
					.getJSONObject(position).getString("model"));
			detail.putExtra("numbermessage", jsonObject.getJSONArray("objects")
					.getJSONObject(position).getString("number"));
			startActivity(detail);
		} catch (Exception e) {
			System.out.println("json解析错误");
			e.printStackTrace();
		}

	}
}
