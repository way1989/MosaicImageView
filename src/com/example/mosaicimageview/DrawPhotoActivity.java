package com.example.mosaicimageview;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 对图片进行编辑的界面
 * 
 **/

public class DrawPhotoActivity extends Activity {

	/** 涂鸦控件的容器 **/
	public LinearLayout imageContent;
	/** 涂鸦控件 **/
	private MosaicImageView touchView;
	/** 完成按钮 **/
	public TextView overBt;
	/** 返回按钮（左上角） */
	public ImageButton backIB = null;
	/** 完成按钮（右上角） */
	public Button finishBtn = null;
	/** 撤销文字 **/
	public TextView cancelText;
	/** 是否为涂鸦 如果是涂鸦 不能删除之前的照片 **/
	public boolean isReDraw = false;
	Intent intent = null;
	public Context context;

	/** 回调接口 */
	private SeekBar seekBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draw_photo);
		initView();
		context = this;

		WindowManager manager = DrawPhotoActivity.this.getWindowManager();
		int ww = manager.getDefaultDisplay().getWidth();// 这里设置高度
		int hh = manager.getDefaultDisplay().getHeight();// 这里设置宽度为
		// 生成画图视图
		touchView = new MosaicImageView(DrawPhotoActivity.this);
		touchView.setSourceBitmap(BitmapFactory.decodeResource(getResources(), R.raw.aaa));
		if (touchView != null) {
			imageContent.removeView(touchView);
		}
		touchView.destroyDrawingCache();
		imageContent.addView(touchView);

	}

	private void initView() {
		imageContent = (LinearLayout) findViewById(R.id.draw_photo_view);
		backIB = (ImageButton) findViewById(R.id.title_bar_left_btn);
		backIB.setVisibility(View.VISIBLE);
		finishBtn = (Button) findViewById(R.id.title_bar_right_btn);
		finishBtn.setBackgroundResource(R.drawable.selector_round_green_btn);
		finishBtn.setVisibility(View.GONE);
		overBt = (TextView) findViewById(R.id.draw_ok_text);
		cancelText = (TextView) findViewById(R.id.draw_photo_cancel);
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int progress = 0;

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
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
				touchView.setStrokeMultiples(1 + (float) (progress / 50.0));
				touchView.removeStrokeView();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				this.progress = progress;
				// System.out.println(progress);
				touchView.setStrokeMultiples(1 + (float) (progress / 100.0));
			}
		});
		backIB.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				try {
					touchView.sourceBitmap.recycle();
					touchView.sourceBitmapCopy.recycle();
					touchView.destroyDrawingCache();
				} catch (Exception e) {
					e.printStackTrace();
				}
				finish();
			}
		});
		finishBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				touchView.sourceBitmap.recycle();
				touchView.sourceBitmapCopy.recycle();
				touchView.destroyDrawingCache();
				finish();
			}
		});
		overBt.setOnClickListener(new View.OnClickListener() {// 完成编辑按钮
			@Override
			public void onClick(View v) {
				overBt.setEnabled(false);
				// 新建一个文件保存照片
				File f = new File(GlobalData.tempImagePaht + "/" + System.currentTimeMillis() + ".jpg");
				if (!f.exists()) {
					try {
						if (!new File(GlobalData.tempImagePaht).exists()) {
							new File(GlobalData.tempImagePaht).mkdirs();
						}
						f.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Bitmap saveBitmap = touchView.combineBitmap(touchView.sourceBitmapCopy, touchView.sourceBitmap);
					ImageUtil.saveMyBitmap(f, saveBitmap);// 将图片重新存入SD卡
					if (touchView.sourceBitmapCopy != null) {
						touchView.sourceBitmapCopy.recycle();
					}
					touchView.sourceBitmap.recycle();
					saveBitmap.recycle();
					touchView.destroyDrawingCache();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Toast.makeText(DrawPhotoActivity.this, "已保存至SD卡MosaicImageView/Temp目录下", Toast.LENGTH_LONG).show();
				finish();
			}
		});

		cancelText.setOnClickListener(new View.OnClickListener() {// 撤销按钮
			@Override
			public void onClick(View v) {
				cancelDrawImage();
			}
		});
	}

	/** 撤销方法 **/
	public void cancelDrawImage() {
		touchView.destroyDrawingCache();
		touchView.setSourceBitmap(BitmapFactory.decodeResource(getResources(), R.raw.aaa));
		// OME--
		if (imageContent.getChildCount() == 0) {
			imageContent.addView(touchView);
		}
	}

}
