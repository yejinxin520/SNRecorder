package com.hy.util;

public class ConfigurationSet {

	private static Boolean autoUpload = false;
	private static int sanTimes = 1;
	private static int barcodeLimit1 = 0;
	private static int barcodeLimit2 = 0;
	private static int barcodeLimit3 = 0;
	public static Boolean getAutoUpload() {
		return autoUpload;
	}
	public static void setAutoUpload(Boolean autoUpload) {
		ConfigurationSet.autoUpload = autoUpload;
	}
	public static int getSanTimes() {
		return sanTimes;
	}
	public static void setSanTimes(int sanTimes) {
		ConfigurationSet.sanTimes = sanTimes;
	}
	public static int getBarcodeLimit1() {
		return barcodeLimit1;
	}
	public static void setBarcodeLimit1(int barcodeLimit1) {
		ConfigurationSet.barcodeLimit1 = barcodeLimit1;
	}
	public static int getBarcodeLimit2() {
		return barcodeLimit2;
	}
	public static void setBarcodeLimit2(int barcodeLimit2) {
		ConfigurationSet.barcodeLimit2 = barcodeLimit2;
	}
	public static int getBarcodeLimit3() {
		return barcodeLimit3;
	}
	public static void setBarcodeLimit3(int barcodeLimit3) {
		ConfigurationSet.barcodeLimit3 = barcodeLimit3;
	}
}
