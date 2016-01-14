package com.example.mosaicimageview;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

/**
 * 对图片进行编辑的界面
 * 
 **/

public class DrawPhotoActivity extends Activity implements OnClickListener, OnSeekBarChangeListener {

	private LinearLayout mMosaicViewContent;
	private MosaicView mMosaicView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draw_photo);
		initView();
		// 生成画图视图
		mMosaicView = new MosaicView(DrawPhotoActivity.this);
		mMosaicView.setSourceBitmap(BitmapFactory.decodeResource(getResources(), R.raw.aaa));
		if (mMosaicView != null) {
			mMosaicViewContent.removeView(mMosaicView);
		}
		mMosaicView.destroyDrawingCache();
		mMosaicViewContent.addView(mMosaicView);

	}

	private void initView() {
		mMosaicViewContent = (LinearLayout) findViewById(R.id.draw_photo_view);
		findViewById(R.id.title_bar_left_btn).setOnClickListener(this);
		findViewById(R.id.draw_ok_text).setOnClickListener(this);
		findViewById(R.id.draw_photo_cancel).setOnClickListener(this);
		SeekBar mosaicSizeSeekBar = (SeekBar) findViewById(R.id.seekBar);
		mosaicSizeSeekBar.setOnSeekBarChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_bar_left_btn:
			mMosaicView.release();
			finish();
			break;
		case R.id.draw_photo_cancel:
			cancelDrawImage();
			break;
		case R.id.draw_ok_text:
			save2File();
			break;

		default:
			break;
		}

	}

	private void save2File() {
		// 新建一个文件保存照片
		File f = new File(GlobalData.tempImagePaht + "/" + System.currentTimeMillis() + ".jpg");
		if (!f.exists()) {
			try {
				if (!new File(GlobalData.tempImagePaht).exists()) {
					new File(GlobalData.tempImagePaht).mkdirs();
				}
				f.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			Bitmap saveBitmap = mMosaicView.getMosaicBitmap();
			ImageUtil.saveMyBitmap(f, saveBitmap);// 将图片重新存入SD卡
		} catch (IOException e) {
			e.printStackTrace();
		}
		mMosaicView.release();
		Toast.makeText(DrawPhotoActivity.this, "已保存至SD卡MosaicImageView/Temp目录下", Toast.LENGTH_LONG).show();
		finish();
	}

	/** 撤销方法 **/
	public void cancelDrawImage() {
		mMosaicView.destroyDrawingCache();
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.raw.aaa);
		mMosaicView.reset(bitmap);
		if (mMosaicViewContent.getChildCount() == 0) {
			mMosaicViewContent.addView(mMosaicView);
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		mMosaicView.setStrokeMultiples(1 + (float) (progress / 100.0));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		int progress = seekBar.getProgress();
		if (progress > 0 && progress < 12.5) {
			seekBar.setProgress(0);
		} else if (progress > 12.5 && progress < 25) {
			seekBar.setProgress(25);
		} else if (progress > 25 && progress < 37.5) {
			seekBar.setProgress(25);
		} else if (progress > 37.5 && progress < 50) {
			seekBar.setProgress(50);
		} else if (progress > 50 && progress < 62.5) {
			seekBar.setProgress(50);
		} else if (progress > 62.5 && progress < 75) {
			seekBar.setProgress(75);
		} else if (progress > 75 && progress < 87.5) {
			seekBar.setProgress(75);
		} else if (progress > 87.5 && progress < 100) {
			seekBar.setProgress(100);
		}
		mMosaicView.setStrokeMultiples(1 + (float) (progress / 50.0));
		mMosaicView.removeStrokeView();
	}
}
