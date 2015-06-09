package com.motorolasolutions.adc.decoder;

import com.hy.snrecorder.R;

import android.app.Activity;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.util.Log;

public class DecodeUtil extends Activity implements BarCodeReader.DecodeCallback{

	static final int STATE_IDLE = 0;
	static final int STATE_DECODE = 1;
	private ToneGenerator tg = null;
	// BarCodeReader specifics
	private BarCodeReader bcr = null;
	private int state = STATE_IDLE;
	private int decodes = 0;
	private String decodeDataString;
	private String decodeStatString;
	private String decodeData;
	private String decodeState;
	private int stateID;

	private boolean isLog = true;
	private static int decCount = 0;
	static {
		System.loadLibrary("IAL");
		System.loadLibrary("SDL");

		if (android.os.Build.VERSION.SDK_INT >= 19)
			System.loadLibrary("barcodereader44"); // Android 4.4
		else if (android.os.Build.VERSION.SDK_INT >= 18)
			System.loadLibrary("barcodereader43"); // Android 4.3
		else
			System.loadLibrary("barcodereader"); // Android 2.3 - Android 4.2
	}

	public void log(String str) {
		if (isLog) {
			Log.d("0127", str);
		}
	}
	private int trigMode = BarCodeReader.ParamVal.LEVEL;

	private int modechgEvents = 0;

	private int motionEvents = 0;
	

	public void decodeinit(final BarCodeReader r) {
		state = STATE_IDLE;

		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					
					
						bcr = r; // Android 2.3

					if (bcr == null) {
						System.out.println("open failed");
						return;
					}
					bcr.setDecodeCallback(DecodeUtil.this);
				} catch (Exception e) {
					System.out.println("open excp:" + e);
				}
			}
		});
		t.start();
		tg = new ToneGenerator(AudioManager.STREAM_MUSIC,
				ToneGenerator.MAX_VOLUME);
	}
	
	public void doDecode() {
		// doSetParam(905, 1);
		
		if (setIdle() != STATE_IDLE)
			return;
		state = STATE_DECODE;
		decCount = 0;
		decodeDataString = new String("");
		decodeStatString = new String("");
		setData("");
		setState(R.string.decoding);
		bcr.startDecode(); // start decode (callback gets results)
		
	}
	
	private int setIdle() {
		// TODO Auto-generated method stub
		int prevState = state;
		int ret = prevState; // for states taking time to chg/end

		state = STATE_IDLE;
		switch (prevState) {
		// fall thru
		case STATE_DECODE:
			setState("decode stopped");
			bcr.stopDecode();
			break;
		default:
			ret = STATE_IDLE;
		}
		return ret;
	}
	private boolean isHandsFree() {
		// TODO Auto-generated method stub
		return (trigMode == BarCodeReader.ParamVal.HANDSFREE);
	}

	private boolean isAutoAim() {
		// TODO Auto-generated method stub
		return (trigMode == BarCodeReader.ParamVal.AUTO_AIM);
	}
	@Override
	public void onDecodeComplete(int symbology, int length, byte[] data,
			BarCodeReader reader) {
		// TODO Auto-generated method stub
		log("into onDecodeComplete");
		if (state == STATE_DECODE)
			state = STATE_IDLE;

		// Get the decode count
		if (length == BarCodeReader.DECODE_STATUS_MULTI_DEC_COUNT)
			decCount = symbology;

		if (length > 0) {
			log("length > 0");
			if (isHandsFree() == false && isAutoAim() == false)
				bcr.stopDecode();

			++decodes;
			log("symbology:" + symbology);
			if (symbology == 0x69) // signature capture
			{
				log("into symbology");

				decodeStatString += new String("[" + decodes + "] type: "
						+ symbology + " len: " + length);
				decodeDataString += new String(data);

			} else {

				if (symbology == 0x99) // type 99?
				{
					symbology = data[0];
					int n = data[1];
					int s = 2;
					int d = 0;
					int len = 0;
					byte d99[] = new byte[data.length];
					for (int i = 0; i < n; ++i) {
						s += 2;
						len = data[s++];
						System.arraycopy(data, s, d99, d, len);
						s += len;
						d += len;
					}
					d99[d] = 0;
					data = d99;
				}
				decodeStatString += new String("[" + decodes + "] type: "
						+ symbology + " len: " + length);
				decodeDataString += new String(data);
				setState(decodeStatString);
				setData(decodeDataString);
				log("=======");
				System.out.println(decodeDataString);
				log("======= end");

				if (decCount > 1) // Add the next line only if multiple decode
				{
					decodeStatString += new String(" ; ");
					decodeDataString += new String(" ; ");
				} else {
					decodeDataString = new String("");
					decodeStatString = new String("");
				}
			}
			
			
			
			if (tg != null)
				tg.startTone(ToneGenerator.TONE_CDMA_NETWORK_CALLWAITING);
		} else // no-decode
		{
			setData("");
			switch (length) {
			case BarCodeReader.DECODE_STATUS_TIMEOUT:
				setState(R.string.timedout);
				break;

			case BarCodeReader.DECODE_STATUS_CANCELED:
				setState(R.string.cancel);
				break;

			case BarCodeReader.DECODE_STATUS_ERROR:
			default:
				setState(R.string.failed);
				break;
			}
		}
	}

	private void setState(int id) {
		// TODO Auto-generated method stub
		stateID = id;
	}

	private void setData(String s) {
		// TODO Auto-generated method stub
		decodeData = s;
	}
	public String getData() {
		return decodeData;
		
	}
	
	public int getStateID() {
		return stateID;
		
	}
	@Override
	public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {
		// TODO Auto-generated method stub
		switch (event) {
		case BarCodeReader.BCRDR_EVENT_SCAN_MODE_CHANGED:
			++modechgEvents;
			setState("Scan Mode Changed Event (#" + modechgEvents + ")");
			break;

		case BarCodeReader.BCRDR_EVENT_MOTION_DETECTED:
			++motionEvents;
			setState("Motion Detect Event (#" + motionEvents + ")");
			break;

		default:
			// process any other events here
			break;
		}
	}

	

	public String getDecodeState() {
		return decodeState;
	}

	public void setState(String decodeState) {
		this.decodeState = decodeState;
	}

}
