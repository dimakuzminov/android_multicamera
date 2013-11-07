package com.galilsoft.cam1;

import com.galilsoft.cam1.R;
import com.galilsoft.cam1.CameraPreview;
import com.galilsoft.cam1.Grey3DView;
//import android.widget.TextView;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.app.Activity;
import android.util.AndroidException;
import android.util.Log;
import android.view.Menu;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
//import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import android.content.Context;
import android.content.Intent;
//import android.util.Log;


public class MainActivity extends Activity  implements OnClickListener{
	public static final String TAG = "ivcam.uvc";
	private Button mCapture;
	private Camera mRgbCam;
	private Camera mDepthCam;
	public byte[] mRgbData = null;
	public byte[] mDepthData = null;
	private volatile int mPictureTakenFlag = 0;
	private PictureCallback mRgbPicture = null;
	private PictureCallback mDepthPicture = null;
	private PictureCallback mDummyPicture = null;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		mCapture = (Button) findViewById(R.id.button1);
		mCapture.setOnClickListener(this);
//		getCamerasInfo();
		SetupPreview(0);
/*
		mRgbPicture = new PictureCallback() {
		    @Override
		    public void onPictureTaken(byte[] data, Camera camera) {
				Log.i(TAG, "rgb raw picture callback");
//		    	mRgbData = data.clone();
		    }
		};
		
		mDepthPicture = new PictureCallback() {
		    @Override
		    public void onPictureTaken(byte[] data, Camera camera) {
				Log.i(TAG, "depth picture callback");
//		    	mDepthData = data.clone();
		    }
		};
*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()) {
		case R.id.button1:
			PictureCallback mDummyPicture = new PictureCallback() {
			    @Override
			    public void onPictureTaken(byte[] data, Camera camera) {
					Log.i(TAG, "dummy picture callback");
			    }
			};
			
			mRgbCam.takePicture(null, null, null, mDummyPicture);
			Intent grey3DView = new Intent(this, Grey3DView.class);
			startActivity(grey3DView);
			break;
		default:
			break;
		}
	}

	@Override
	public void onDestroy() {
        super.onDestroy();
	}
	
	private void getCamerasInfo() {
		int cameraCount = 0;
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//		TextView number = (TextView) this.findViewById(R.id.number);
//		TextView info = (TextView) this.findViewById(R.id.info);
		cameraCount = Camera.getNumberOfCameras();
//		number.setText("Number of cameras:" + cameraCount);
		Log.i(TAG, "Number of cameras:" + cameraCount);
		StringBuffer buffer = new StringBuffer();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
//			if(camIdx != 2){
			Camera mcam = Camera.open(camIdx);
			if(mcam == null)
				break;
			Camera.Parameters cameraParams = mcam.getParameters();
			String str = cameraParams.flatten();
			Log.i(TAG, "info " + str + "\n");
			mcam.release();
//			}
			Camera.getCameraInfo(camIdx, cameraInfo);
			String facing = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ? "front"
					: cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ? "back"
							: "no facing";
			buffer.append("Camera " + camIdx + " facing " + facing
					+ ", orientation " + cameraInfo.orientation + "\n");
			Log.i(TAG, "Camera " + camIdx + " facing " + facing
					+ ", orientation " + cameraInfo.orientation);
		}
//		info.setText(buffer.toString());
	}

	private void SetupPreview(int id) {
		mRgbCam = Camera.open(id);
//		mDepthCam = Camera.open(id+1);
	    CameraPreview preview_rgb = new CameraPreview((Context)this,mRgbCam);
//	    CameraPreview preview_depth = new CameraPreview((Context)this,mDepthCam);
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_main);
//		layout.addView(preview_depth);
		layout.addView(preview_rgb);
		layout.bringChildToFront(findViewById(R.id.button1));
	}
}
