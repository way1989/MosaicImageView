package com.example.mosaicimageview;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import cn.ffmpeg.NativeFFmpeg;

public class MainActivity extends Activity {

	protected String srcPath = "src.png";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ImageUtil.write(this, "aaa.png", srcPath);
		findViewById(R.id.button1).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				running();
			}
		});
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				int result = new NativeFFmpeg().FFmpegConvertGMp4ToGif("/mnt/sdcard/Movies/Telecine/Telecine_2016-01-10-15-11-05.mp4", "1500" , "15" , "3" , "10", "/mnt/sdcard/Movies/out.gif");
				Log.i("way", "result = " + result);
			}
		}).start();
	}

	private void running() {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), DrawPhotoActivity.class);
		String path = GlobalData.CameraFile + "/" + srcPath;
		intent.putExtra(DrawPhotoActivity.FILEPATH, path);
		// …Ë÷√ªÿµ˜
		startActivity(intent);
	}

}
