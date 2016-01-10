package com.example.mosaicimageview;

import java.io.File;

import android.os.Environment;


/**
 * 存放全局缓存类
 * 
 */
public class GlobalData {
	/** SD卡的路径 **/
	public static String SDcardPaht = getSDcardPath();

	public static final String CameraFile = SDcardPaht + "/MosaicImageView";
	public static final String CameraPhoto = SDcardPaht + "/MosaicImageView/temp.jpg";
	public static final String tempImagePaht = SDcardPaht + "/MosaicImageView/Temp";

	/** 获取SD卡路径 **/
	public static String getSDcardPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
		}
		if(sdDir ==null){
			return "/mnt/sdcard";
		}
		return sdDir.toString();
	}
	
	
}