/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.camera.tests.stress;

import com.android.camera.tests.TestConfigurationManager;
import com.android.camera.tests.CameraHelper;
import com.android.camera.ui.CameraSwitcher;
import com.android.camera.ComboPreferences;
import com.android.camera.CameraActivity;
import com.android.camera.CameraSettings;
import com.android.camera.CameraHolder;
import com.android.camera.Util;
import com.android.camera.Storage;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.ActivityInstrumentationTestCase2;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.CameraInfo;
import android.content.SharedPreferences;
import android.media.CamcorderProfile;
import android.hardware.Camera.Size;
import android.app.Instrumentation;
import android.provider.MediaStore;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.view.KeyEvent;
import android.app.Activity;
import android.util.Log;
import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Junit / Instrumentation test case for camera test
 * 
 * Running the test suite:
 * 
 * adb shell am instrument -e class \
 * com.android.camera.tests.stress.VideoCamSettingsStressTests -w \
 * com.android.camera.tests/android.test.InstrumentationTestRunner
 * 
 */

public class VideoCamSettingsStressTests extends
        ActivityInstrumentationTestCase2<CameraActivity> {

    private static final long WAIT_FOR_CAMERA_OPERATIONS = 3000;
    private static final long WAIT_FOR_RESTORE_PREFERENCES = 5000;
    private static final long MAX_CAMERA_CHANGETIME = 1000;
    private static final String DEFAULT_PREF_VALUE = "default_value";
    private static final String TAG = "VideoCamSettingsStressTests";
    private static final String CAMERA_DIRECTORY = Storage.DIRECTORY;
    private static final String DCIM_DIRECTORY = Storage.DCIM;
    private static final String CONFIG_DIRECTORY = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    private static final String CAMERA_ID_BACK = "0";
    private static final String CAMERA_ID_FRONT = "1";
    private final static String EXTRAS_CAMERA_FACING = "android.intent.extras.CAMERA_FACING";
    private CameraSwitcher mSwitcher;
    private File cameraDirectory = new File(CAMERA_DIRECTORY);
    private String sNull = "ety";
    private long cameraAppLaunchTime = 0;
    private TestConfigurationManager mTestConfigurationManager;

    private ComboPreferences comb;
    public Context mActivityContext;
    private Activity mActivity;
    private Instrumentation mInstrumentation;

    public VideoCamSettingsStressTests() {
        super(CameraActivity.class);
        Log.v(TAG, "CameraActivity startup time: Constructor");
    }

    @Override
    protected void setUp() throws Exception {
        Log.v(TAG, "CameraActivity setUp() ");
        super.setUp();
        mTestConfigurationManager = new TestConfigurationManager(getInstrumentation()
                .getContext());
        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        mActivityContext = mInstrumentation.getTargetContext();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private Activity launchCamera(String cameraID) throws InterruptedException {
        if (cameraID.equals(CAMERA_ID_BACK)) {
            Log.v(TAG, "Launching back camera");
            return launchBackCamera();
        }
        if (cameraID.equals(CAMERA_ID_FRONT)) {
            Log.v(TAG, "Launching front camera");
            return launchFrontCamera();
        }
        return null;
    }

    private Activity launchBackCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        intent.setClass(mActivityContext, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRAS_CAMERA_FACING,
                android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
        return mInstrumentation.startActivitySync(intent);
    }

    private Activity launchFrontCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        intent.setClass(mActivityContext, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRAS_CAMERA_FACING,
                android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        return mInstrumentation.startActivitySync(intent);
    }

    private Activity launchPreferencesCamera() {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        intent.setClass(mActivityContext, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return mInstrumentation.startActivitySync(intent);
    }

    public void restorePreferences(int CameraID) throws InterruptedException {
        launchCamera(String.valueOf(CameraID));
        ComboPreferences comb = new ComboPreferences(mActivityContext);
        comb.setLocalId(mActivityContext, CameraID);
        Editor editor = comb.edit();
        editor.clear();
        editor.apply();
        Thread.sleep(WAIT_FOR_RESTORE_PREFERENCES);
        mActivity.finish();
    }

    public void videoCapture(int NumberOfvideoCapture, long VideoRecordingTime)
            throws InterruptedException, IOException {
        Log.v(TAG, "videoCapture");

        CameraHelper.deleteAllFilesInFolder(cameraDirectory);

        long CameraDirectorySize1 = CameraHelper.getFolderSize(cameraDirectory);
        long nr1 = CameraHelper.getFolderFilesNumber(cameraDirectory);
        int i = 0;

        for (i = 1; i <= NumberOfvideoCapture; i++) {
            Log.v(TAG, "take Video iteration i==" + i);
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
            mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
            Log.v(TAG, "sent KEYCODE to start recording");
            Thread.sleep(VideoRecordingTime);
            mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
        }

        long CameraDirectorySize2 = CameraHelper.getFolderSize(cameraDirectory);
        long nr2 = CameraHelper.getFolderFilesNumber(cameraDirectory);
        long NumberOfVideoTaken = nr2 - nr1;

        Log.v(TAG, "Size of the Camera Direcory== "
                + (CameraDirectorySize2 - CameraDirectorySize1));
        assertTrue(
                "Camera Directory Size was not changed which means Video was not saved  = ",
                (CameraDirectorySize2 - CameraDirectorySize1 > 0));

        Log.v(TAG, "Nuber of files in Camera Direcory== " + NumberOfVideoTaken
                + " Number of Video Taken== " + NumberOfvideoCapture);
        assertEquals("Number of Video taken is not equal with NumberOfvideoCapture ",
                NumberOfvideoCapture, NumberOfVideoTaken);

    }

    public void setParameters(ComboPreferences comb, String Key_Pref, String value) {
        Editor editor = comb.edit();
        editor.putString(Key_Pref, value);
        boolean status = editor.commit();
        assertTrue("Preferences Commit was not successful= ", status);
        Log.v(TAG, Key_Pref + "== " + comb.getString(Key_Pref, DEFAULT_PREF_VALUE));

    }

    private void ChangeCamera(ComboPreferences comb, String value)
            throws InterruptedException {
        comb.setLocalId(mActivityContext, Integer.parseInt(value));
        setParameters(comb, CameraSettings.KEY_CAMERA_ID, value);
        Thread.sleep(MAX_CAMERA_CHANGETIME);
    }

    public void updateCameraSettings(Properties props) throws InterruptedException {

        String CameraID = props.getProperty("CameraID");
        String StoreLocation = props.getProperty("StoreLocation");
        String VideoQuality = props.getProperty("VideoQuality");
        String TimeLapseInterval = props.getProperty("TimeLapseInterval");
        String VideoFlash = props.getProperty("VideoFlash");
        String ColorEfect = props.getProperty("ColorEfect");
        String WhiteBalance = props.getProperty("WhiteBalance");

        // start Camera app
        mActivity = launchCamera(CameraID);
        Log.v(TAG, "Activity was started");

        // read Camera ID
        comb = new ComboPreferences(mActivityContext);
        String CameraID1 = comb.getString(CameraSettings.KEY_CAMERA_ID,
                DEFAULT_PREF_VALUE);
        Log.v(TAG, "CameraSettings.KEY_CAMERA_ID CameraID1 == " + CameraID1);
        Log.v(TAG, "CameraSettings.KEY_CAMERA_ID CameraID == " + CameraID);
        boolean defaultCam = false;
        Activity act = null;

        if (CameraID1.equals(DEFAULT_PREF_VALUE) == true) {
            ChangeCamera(comb, CameraID);
            mActivity.finish();
            defaultCam = true;

            // start Camera App
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
            act = launchPreferencesCamera();

            // read Camera ID
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
            mActivityContext = mInstrumentation.getTargetContext();
            comb = new ComboPreferences(mActivityContext);
            CameraID1 = comb.getString(CameraSettings.KEY_CAMERA_ID, DEFAULT_PREF_VALUE);
            Log.v(TAG, "CameraSettings.KEY_CAMERA_ID CameraID1 == " + CameraID1);
            Log.v(TAG, "CameraSettings.KEY_CAMERA_ID CameraID == " + CameraID);

        }

        // set camera
        if (Integer.parseInt(CameraID1) != Integer.parseInt(CameraID)) {
            Log.v(TAG, "Change Camera");
            ChangeCamera(comb, CameraID);
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
        }

        // read Camera ID
        comb = new ComboPreferences(mActivityContext);
        comb.setLocalId(mActivityContext, Integer.parseInt(CameraID));
        Log.v(TAG,
                "CameraSettings.KEY_CAMERA_ID== "
                        + comb.getString(CameraSettings.KEY_CAMERA_ID, DEFAULT_PREF_VALUE));
        assertEquals("CameraID read from file was not set", CameraID,
                comb.getString(CameraSettings.KEY_CAMERA_ID, DEFAULT_PREF_VALUE));

        if (StoreLocation.equals(sNull) == false) {
            Log.v(TAG, "StoreLocation");
            setParameters(comb, CameraSettings.KEY_RECORD_LOCATION, StoreLocation);
        }

        if (VideoQuality.equals(sNull) == false) {
            Log.v(TAG, "VideoQuality");
            setParameters(comb, CameraSettings.KEY_VIDEO_QUALITY, VideoQuality);
        }

        if (TimeLapseInterval.equals(sNull) == false) {
            Log.v(TAG, "TimeLapseInterval");
            setParameters(comb, CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL,
                    TimeLapseInterval);
        }

        if (VideoFlash.equals(sNull) == false) {
            Log.v(TAG, "VideoFlash");
            setParameters(comb, CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE, VideoFlash);
        }

        if (WhiteBalance.equals(sNull) == false) {
            Log.v(TAG, "WhiteBalance");
            setParameters(comb, CameraSettings.KEY_WHITE_BALANCE, WhiteBalance);
        }

        // close activity to submit preferences
        Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);

        if (defaultCam == false) {
            mActivity.finish();
        } else {
            act.finish();
        }

    }

    public void recordVideo(Properties cProps) throws InterruptedException, IOException {
        Log.v(TAG, "Instrumentation test start");

        // read parameters from Properties
        // NumberOFvideoTaken, VideoRecordingTime, CameraID, StoreLocation,
        // VideoQuality, TimeLapseInterval, VideoFlash, ColorEfect,
        // WhiteBalance;
        updateCameraSettings(cProps);
        launchCamera(cProps.getProperty("CameraID"));

        String NumberOFvideoTaken = cProps.getProperty("NumberOFvideoTaken");
        String VideoRecordingTime = cProps.getProperty("VideoRecordingTime");
        // videoCapture(NumberOfvideoCapture, VideoRecordingTime)
        videoCapture(Integer.parseInt(NumberOFvideoTaken),
                Long.parseLong(VideoRecordingTime));

        Log.v(TAG, "Instrumentation test stop");

    }

    public void cameraLaunchTime(Properties cProps) throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG,
                "This test will determine Camera app Launch Time and will fail if it is > 2000ms");

        // read parameters from Properties
        // NumberOFvideoTaken, VideoRecordingTime, CameraID, StoreLocation,
        // VideoQuality, TimeLapseInterval, VideoFlash, ColorEfect,
        // WhiteBalance;

        updateCameraSettings(cProps);
        launchCamera(cProps.getProperty("CameraID"));

        // start Camera
        long beforeStart = System.currentTimeMillis();
        launchCamera(cProps.getProperty("CameraID"));
        long cameraStarted = System.currentTimeMillis();
        cameraAppLaunchTime = cameraStarted - beforeStart;

        Log.v(TAG, " CameraActivity startup time == cameraAppLaunchTime:== "
                + cameraAppLaunchTime);
        assertFalse(" CameraActivity startup time is > 2000ms so this test fails = ",
                (cameraAppLaunchTime > 2000));

        Log.v(TAG, "Instrumentation test stop");
    }

    public void restoreDefaultCameraActivitySettings(Properties cProps)
            throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG,
                "This test will verify that restore Camera Settings to Default works as expected");
        // read parameters from Properties
        updateCameraSettings(cProps);
        String CameraID = cProps.getProperty("CameraID");

        restorePreferences(Integer.parseInt(CameraID));// restore default
                                                       // settings
        mActivity = launchCamera(cProps.getProperty("CameraID"));

        mActivityContext = mInstrumentation.getTargetContext();
        ComboPreferences comb = new ComboPreferences(mActivityContext);
        comb.setLocalId(mActivityContext, Integer.parseInt(CameraID));

        // check if preferences were changed
        assertFalse(
                "preferences were not set to default ",
                cProps.getProperty("VideoQuality").equals(
                        comb.getString(CameraSettings.KEY_VIDEO_QUALITY,
                                DEFAULT_PREF_VALUE)));
        assertFalse(
                "preferences were not set to default ",
                cProps.getProperty("TimeLapseInterval").equals(
                        comb.getString(
                                CameraSettings.KEY_VIDEO_TIME_LAPSE_FRAME_INTERVAL,
                                DEFAULT_PREF_VALUE)));
        assertFalse(
                "preferences were not set to default ",
                cProps.getProperty("VideoFlash").equals(
                        comb.getString(CameraSettings.KEY_VIDEOCAMERA_FLASH_MODE,
                                DEFAULT_PREF_VALUE)));
        mActivity.finish();
        Log.v(TAG, "Instrumentation test stop");

    }

    public void testCameraRecordAVideoBAT() throws InterruptedException, IOException,
            FileNotFoundException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG,
                "This test will check if it is possible to open Camera App and record 1 Video ");
        // videoCapture(Number of videos to capture, Video recording time)
        videoCapture(1, 10000);
        Log.v(TAG, "Instrumentation test stop");

    }

    public void testCameraLaunchTime() throws InterruptedException, IOException {
        Properties cameraProps = mTestConfigurationManager
                .getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", cameraProps);
        cameraLaunchTime(cameraProps);
    }

    public void testRestoreDefaultBackVideoCameraSettings() throws InterruptedException,
            IOException {
        Properties cameraProps = mTestConfigurationManager
                .getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", cameraProps);
        restoreDefaultCameraActivitySettings(cameraProps);
    }

    public void testRecordVideo() throws InterruptedException, IOException {
        // pass a properties file to recordVideo method
        Properties cameraProps = mTestConfigurationManager
                .getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", cameraProps);
        recordVideo(cameraProps);
    }
}
