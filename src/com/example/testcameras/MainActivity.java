package com.example.testcameras;

import com.example.testcameras.R;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

public class MainActivity extends Activity {
	public static final String TAG = "TestCameras";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addButtonListener();
		getCamerasInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void addButtonListener() {
		Button previewButton = (Button) findViewById(R.id.preview_button); 
		previewButton.setOnClickListener(new OnClickListener() { 
		@Override
			public void onClick(View arg0) {
				getCamerasPreview(); 
			}
		}); 
	}
	
	private void getCamerasInfo() {
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        TextView number = (TextView)this.findViewById(R.id.number);
        TextView info = (TextView)this.findViewById(R.id.info);
        cameraCount = Camera.getNumberOfCameras();
        number.setText("Number of cameras:" + cameraCount);
        Log.i(TAG, "Number of cameras:" + cameraCount);
        StringBuffer buffer = new StringBuffer();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {            
            Camera.getCameraInfo( camIdx, cameraInfo );
            String facing = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT ?
            				"front" : cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ?
            				"back" : "no facing";
            buffer.append("Camera " + camIdx + " facing " + facing + ", orientation " + cameraInfo.orientation + "\n");
            Log.i(TAG, "Camera " + camIdx + " facing " + facing + ", orientation " + cameraInfo.orientation);
        }
        info.setText(buffer.toString());
    }   	
	
	private void getCamerasPreview() {
		 PackageManager pm = getPackageManager();
	     if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
	    	 Log.i(TAG, "Phone has a frontal camera.");
	     }	                
        int cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {        	
        //for ( int camIdx = 1; camIdx >= 0; camIdx-- ) {	
            Camera camera = getCameraInstance(camIdx);
            if (camera == null) {
            	continue;
            }
            CameraPreview preview = new CameraPreview(this, camera, camIdx);
            FrameLayout layout = null;
            if (camIdx == 0) {
            	layout = (FrameLayout) findViewById(R.id.camera_preview_0);
            	layout.addView(preview);
            }
            else if (camIdx == 1) {
            	layout = (FrameLayout) findViewById(R.id.camera_preview_1);
            	layout.addView(preview);
            }                    
        }
    }   
	
	public static Camera getCameraInstance( int id ){
	    Camera camera = null;
	    try {
	    	Log.i(TAG, "Try to open camera id " + id);
	        camera = Camera.open( id );
	    }
	    catch (Exception e){
	    	Log.e(TAG, "Cannot get camera " + id + "," + e.toString());
	    }
	    return camera;
	}

}
