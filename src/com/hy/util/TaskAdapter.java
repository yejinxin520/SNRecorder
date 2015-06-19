package com.hy.util;

import java.util.List;
import java.util.Map;

import com.hy.snrecorder.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TaskAdapter extends BaseAdapter {

	private List<Map<String, Object>> data;
	private Context context;

	public TaskAdapter(Context context, List<Map<String, Object>> data) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.data = data;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = LayoutInflater.from(this.context);
		View view = inflater.inflate(R.layout.list_item, null);
		ImageView imageView = (ImageView) view.findViewById(R.id.modelimg);
		TextView textView = (TextView) view.findViewById(R.id.taskcontent);
		imageView.setBackgroundResource((Integer) data.get(position).get(
				"image"));
		textView.setText((String) data.get(position).get("content"));
		return view;
	}

}
