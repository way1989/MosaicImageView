package com.example.mosaicimageview;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.WindowManager;

public class DensityUtil {
	public static int dip2px(Context context, float dip) {
		//return (int) (0.5F + dip * context.getResources().getDisplayMetrics().density);
		Resources resources = context.getResources();
		int px = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, resources.getDisplayMetrics()));
		return px;
	}

	public static int px2dip(Context context, float px) {
		return (int) (0.5F + px / context.getResources().getDisplayMetrics().density);
	}

	/**
	 *
	 * @return
	 */
	public static int getDisplayHeight(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		return dm.heightPixels;
	}

	/**
	 *
	 * @return
	 */
	public static int getDisplayWidth(Context context) {
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		windowManager.getDefaultDisplay().getMetrics(dm);
		return dm.widthPixels;
	}

}