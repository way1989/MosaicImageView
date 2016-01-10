package com.example.mosaicimageview;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
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
	/** 传递动作,为takephoto 表示拍照，否则传递过来的是图片的路径 */
	public static final String FILEPATH = "filepath";
	public static final String ACTION = "action";
	public static final String ACTION_INIT = "action_init";


	public static final String FROM = "from";

	/** 动作 */
	public static final String TAKEPHOTO = "takephoto";

	public static final String REDRAW = "ReDraw";

	/** 涂鸦控件的容器 **/
	public LinearLayout imageContent;
	/** 操纵图片的路径 **/
	private String filePath = "";
	/** 涂鸦控件 **/
	private MosaicImageView touchView;
	/** 完成按钮 **/
	public TextView overBt;
	/** 返回按钮（左上角）*/
	public ImageButton backIB = null;
	/** 完成按钮（右上角）*/
	public Button finishBtn = null;
	/** 撤销文字 **/
	public TextView cancelText;
	private GetImage handler;
	private ProgressDialog progressDialog = null;
	/** 是否为涂鸦 如果是涂鸦 不能删除之前的照片 **/
	public boolean isReDraw = false;
	Intent intent = null;
	public Context context;
	public BroadcastReceiver broadcastReceiver = null;

	/** 回调接口 */
	private SeekBar seekBar;
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.draw_photo);
		initView();
		context = this;
		// 获取传递过来的图片路径
		intent = getIntent();
		broadcastReceiver = new BroadcastReceiver(){
			public void onReceive(Context context, Intent intent) {
				if(progressDialog != null && progressDialog.isShowing()){
					progressDialog.dismiss();
				}
			};
		};
		
		registerReceiver(broadcastReceiver, new IntentFilter(ACTION_INIT));
		String action = intent.getExtras().getString(ACTION,"");
		if(!TextUtils.isEmpty(action)&&action.equals(TAKEPHOTO)){
			Intent takephoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File f = new File(GlobalData.CameraFile);
			if (!f.exists())
				f.mkdir();
			takephoto.putExtra(MediaStore.EXTRA_OUTPUT,
					Uri.fromFile(new File(GlobalData.CameraPhoto)));// 设置相机
			startActivityForResult(takephoto, 1);
		}else {
			System.out.println("645646564456456546");
			filePath = intent.getExtras().getString(FILEPATH);
			if (!TextUtils.isEmpty(filePath)) {
				ImageThread thread = new ImageThread();
				thread.start();
			}
		}
		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 0)// 按返回键 没获取照片
			finish();
		if (requestCode != 1)
			finish();
		if (resultCode == Activity.RESULT_OK && requestCode == 1) {
			// Bitmap bitmap = setImage(uri);
			try {
				BitmapFactory.Options newOpts = new BitmapFactory.Options();
				// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
				newOpts.inPreferredConfig = Bitmap.Config.RGB_565;// 表示16位位图
				newOpts.inInputShareable = true;
				newOpts.inPurgeable = true;// 设置图片可以被回收
				newOpts.inJustDecodeBounds = false;
				// 获取相机返回的数据，并转换为图片格式
				newOpts.inSampleSize = 2;
				Bitmap bitmap = BitmapFactory.decodeFile(
						GlobalData.CameraPhoto, newOpts);// 读取路径上的文件
				String ddr = GlobalData.tempImagePaht;
				File ddrfile = new File(ddr);
				if (!ddrfile.exists()) {
					ddrfile.mkdirs();
				}
				String fileName = GlobalData.tempImagePaht + "/"
						+ System.currentTimeMillis() + ".jpg";
				File file = new File(fileName);
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(file));
				// 判断图片是否旋转，处理图片旋转
				ExifInterface exif = new ExifInterface(GlobalData.CameraPhoto);
				int orientation = exif.getAttributeInt(
						ExifInterface.TAG_ORIENTATION, 0);
				// 旋转图片，使其显示方向正常
				bitmap = ImageUtil.getTotateBitmap(bitmap, orientation);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);// 将图片压缩到流中
				bos.flush();// 输出
				bos.close();// 关闭
				bitmap.recycle();// 回收数据
				bitmap = null;
				System.gc(); // 提醒系统及时回收
				filePath = fileName;
				intent.putExtra("action", filePath);
				if (filePath != null && !filePath.equals("")) {
					ImageThread thread = new ImageThread();
					thread.start();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void initView() {
		imageContent = (LinearLayout) findViewById(R.id.draw_photo_view);
		handler = new GetImage();
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
				if(progress > 0 && progress < 12.5){
					seekBar.setProgress(0);
				}else if(progress > 12.5 && progress < 25){
					seekBar.setProgress(25);
				}else if(progress > 25 && progress < 37.5){
					seekBar.setProgress(25);
				}else if(progress > 37.5 && progress < 50){
					seekBar.setProgress(50);
				}else if(progress > 50 && progress < 62.5){
					seekBar.setProgress(50);
				}else if(progress > 62.5 && progress < 75){
					seekBar.setProgress(75);
				}else if(progress > 75 && progress < 87.5){
					seekBar.setProgress(75);
				}else if(progress > 87.5 && progress < 100){
					seekBar.setProgress(100);
				}
				touchView.setStrokeMultiples(1 + (float)(progress / 100.0));
				touchView.removeStrokeView();
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				this.progress = progress;
//				System.out.println(progress);
				touchView.setStrokeMultiples(1 + (float)(progress / 100.0));
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
				File f = new File(GlobalData.tempImagePaht + "/"
						+ System.currentTimeMillis() + ".jpg");
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
					// 删除Temp中已经有的图片
					if (!TextUtils.isEmpty(filePath)
							&& filePath.contains(GlobalData.tempImagePaht)) {
						new File(filePath).delete();
					}
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
	@SuppressWarnings("deprecation")
	@SuppressLint("HandlerLeak")
	public void cancelDrawImage() {
			touchView.destroyDrawingCache();
			WindowManager manager = DrawPhotoActivity.this.getWindowManager();
			int ww = manager.getDefaultDisplay().getWidth();// 这里设置高度
			int hh = manager.getDefaultDisplay().getHeight();// 这里设置宽度为
			touchView.revocation(filePath, ww, hh);
			// OME--
			if(imageContent.getChildCount() == 0){
				imageContent.addView(touchView);
			}
	}

	@SuppressLint("HandlerLeak")
	private class GetImage extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case 0: {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
				progressDialog = ProgressDialog.show(DrawPhotoActivity.this,
						context.getString(R.string.drawPhoto_actionName),
						context.getString(R.string.drawPhoto_actioning));
			}
				break;
			case 1: {
				if (touchView != null) {
					imageContent.removeView(touchView);
				}
				touchView = (MosaicImageView) msg.obj;
				touchView.destroyDrawingCache();
				imageContent.addView(touchView);
			}
				break;
			case 2: {
				// 获取新的图片路径
				filePath = (String) msg.obj;
				// 开启图片和处理线程
				ImageThread thread = new ImageThread();
				thread.start();
			}
				break;
			case 3: {
				if (progressDialog != null)
					progressDialog.dismiss();
			}
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}

	}

	private class ImageThread extends Thread {
		@SuppressWarnings("deprecation")
		public void run() {
			// 打开进度条
			Message msg = new Message();
			msg.what = 0;
			handler.sendMessage(msg);
			// 获取屏幕大小
			WindowManager manager = DrawPhotoActivity.this.getWindowManager();
			int ww = manager.getDefaultDisplay().getWidth();// 这里设置高度
			int hh = manager.getDefaultDisplay().getHeight();// 这里设置宽度为
			// 生成画图视图
			touchView = new MosaicImageView(DrawPhotoActivity.this, null, filePath, ww, hh);
			Message msg1 = new Message();
			msg1.what = 1;
			msg1.obj = touchView;
			handler.sendMessage(msg1);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration config) {
		super.onConfigurationChanged(config);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(broadcastReceiver != null){
			unregisterReceiver(broadcastReceiver);
		}
	}
}

