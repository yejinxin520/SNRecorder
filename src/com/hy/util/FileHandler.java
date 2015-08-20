package com.hy.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

public class FileHandler {

	private Context context;
	public FileHandler(Context context) {
		this.context = context;
	}
	public void save(String filename,String content,int mode)throws Exception {
		FileOutputStream fos = context.openFileOutput(filename, mode);
		fos.write(content.getBytes());
		fos.close();
	}
	public void save(String filename,String content) throws Exception{
		save(filename, content, Context.MODE_PRIVATE);
	}
	@SuppressLint("SdCardPath")
	public void saveToSDCard(String filename,String content) throws Exception{
		File filepath = new File(Environment.getExternalStorageDirectory().getPath());
		File file = new File(filepath, filename);
		FileOutputStream fops = new FileOutputStream(file, true);
		fops.write(content.getBytes());
		fops.write(",".getBytes());
		fops.close();
	}
	@SuppressLint("SdCardPath")
	public String readFile(String filename) throws Throwable {
		/*FileInputStream fis = context.openFileInput(filename);
		byte [] bytes = new byte[1024];
		int len = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while ((len = fis.read(bytes))!=-1) {
			baos.write(bytes,0,len);
		}
		byte [] data = baos.toByteArray();
		baos.close();
		fis.close();"/mnt/sdcard/Download"
		return new String(data);*/		            
		File filepath = new File(Environment.getExternalStorageDirectory().getPath());
		File file = new File(filepath, filename);
				@SuppressWarnings("resource")
				FileInputStream inputStream = new FileInputStream(file);
                byte[] b = new byte[inputStream.available()];
                inputStream.read(b);                
                return new String(b);

	}
}
