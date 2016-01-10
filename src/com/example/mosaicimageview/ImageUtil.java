package com.example.mosaicimageview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

/**
 * 
 * @author Administrator
 * 
 */
public class ImageUtil {

	/**
	 * 旋转图片，使其显示正常
	 * 
	 * @param bitmap
	 *            原始图片
	 * @param orientation
	 *            图片旋转属性
	 * @return 旋转正常后的图片
	 */
	public static Bitmap getTotateBitmap(Bitmap bitmap, int orientation) {
		// 处理图片旋转的问题
		Matrix matrix = new Matrix();

		switch (orientation) {
		case ExifInterface.ORIENTATION_ROTATE_90: {// 旋转了90度
			matrix.setRotate(90);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
		}
			break;
		case ExifInterface.ORIENTATION_ROTATE_180: {// 旋转了180度
			matrix.setRotate(180);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
		}
			break;
		case ExifInterface.ORIENTATION_ROTATE_270: {// 旋转了270度
			matrix.setRotate(270);
			bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
					bitmap.getHeight(), matrix, true);
		}
			break;
		default:
			break;
		}
		return bitmap;
	}

	/** 保存图片方法 **/
	public static boolean saveMyBitmap(File f, Bitmap mBitmap)
			throws IOException {
		boolean saveComplete = true;
		try {
			f.createNewFile();
			FileOutputStream fOut = null;
			fOut = new FileOutputStream(f);
			int width = mBitmap.getWidth();
			int height = mBitmap.getHeight();
			// 计算缩放的比例
			int finalWidth = 800;
			int finalHeight = (int) (finalWidth * 1.0 * (height * 1.0 / width * 1.0));
			double x = width * finalHeight;
			double y = height * finalWidth;

			if (x > y) {
				finalHeight = (int) (y / (double) width);
			} else if (x < y) {
				finalWidth = (int) (x / (double) height);
			}

			if (finalWidth > width && finalHeight > height) {
				finalWidth = width;
				finalHeight = height;
			}
			Matrix matrix = new Matrix();
			matrix.reset();
			// 计算宽高缩放率
			float scaleWidth = ((float) finalWidth) / (float) width;
			float scaleHeight = ((float) finalHeight) / (float) height;
			// 缩放图片动作
			matrix.postScale(scaleWidth, scaleHeight);
			mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, (int) width,
					(int) height, matrix, true);
			mBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
			fOut.flush();
			fOut.close();
			// 回收内存空间
			mBitmap.recycle();
			System.gc();
		} catch (FileNotFoundException e) {
			saveComplete = false;
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			saveComplete = false;
		}
		return saveComplete;
	}

	/**
	 * 加载本地图片
	 * 
	 * @param path
	 *            本地图片路径
	 * @return Bitmap 本地图片不存在时返回null
	 */
	public static Bitmap getLoacalBitmap(Context context, String file) {
		String buff = file.replace("file://", "");
		// 进一步判断文件是否存在
		File check = new File(buff);
		// 本地图片路径不存在，返回null
		if (!check.exists()) {
			return null;
		}
		// 读取图片
		try {
			BitmapFactory.Options newOpts = new BitmapFactory.Options();
			// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
			newOpts.inJustDecodeBounds = false;
			// 表示16位位图,565代表对应三原色占的位数
			newOpts.inPreferredConfig = Bitmap.Config.RGB_565;
			newOpts.inInputShareable = true;
			newOpts.inPurgeable = true;// 设置图片可以被回收
			return BitmapFactory.decodeFile(buff, newOpts);
		} catch (Exception e) {
			e.printStackTrace();
			// 读取图片出错时返回null
			return null;
		}
	}

	public static void write(Context context, String srcPathName, String  newPathName) {
		InputStream inputStream;
		try {
			inputStream = context.getResources().getAssets().open(srcPathName);
			File file = null;
			file = new File(GlobalData.CameraFile);
			if (!file.exists()) {
				file.mkdirs();
			}
			file = new File(GlobalData.CameraFile + "/" + newPathName);
			if (file.exists()) {
				file.delete();
			}
			FileOutputStream fileOutputStream = new FileOutputStream(GlobalData.CameraFile + "/" + newPathName);
			byte[] buffer = new byte[512];
			int count = 0;
			while ((count = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, count);
			}
			fileOutputStream.flush();
			fileOutputStream.close();
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
