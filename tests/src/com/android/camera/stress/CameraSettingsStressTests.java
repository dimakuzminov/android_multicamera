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

import com.android.camera.tests.CameraHelper;
import android.media.ExifInterface;
import com.android.camera.CameraActivity;
import com.android.camera.Storage;
import com.android.camera.CameraSettings;
import com.android.camera.CameraHolder;
import com.android.camera.ComboPreferences;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.content.Context;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.app.Instrumentation;
import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.util.Log;
import android.view.KeyEvent;
import android.content.Intent;
import android.view.View;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Properties;
import android.test.TouchUtils;
import com.android.camera.R;
import com.android.camera.tests.TestConfigurationManager;
import android.os.Environment;

/**
 * Junit / Instrumentation test case for camera test
 * 
 * Running the test suite:
 * 
 * adb shell am instrument \ -e class
 * com.android.camera.tests.stress.CameraSettingsStressTests \ -w
 * com.android.camera.tests/android.test.InstrumentationTestRunner
 * 
 */

public class CameraSettingsStressTests extends
        ActivityInstrumentationTestCase2<CameraActivity> {
    private static final long WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN = 8000;
    private static final long WAIT_FOR_FOCUS = 2000;
    private static final long WAIT_TO_SAVE_BURST_IMAGES = 20000;
    private static final long WAIT_FOR_PARAMETERS_TO_BE_SAVED = 2000;
    private static final long WAIT_FOR_PREFERENCE_CHANGE = 4000;
    private static final long WAIT_FOR_CAMERA_OPERATIONS = 3000;
    private static final long WAIT_FOR_RESTORE_PREFERENCES = 5000;
    private static final String TAG = "CameraSettingsTests";
    private static final String DEFAULT_PREF_VALUE = "default_value";
    private static final String NULL_PHOTO_SIZE = "0";
    private static final String CAMERA_DIRECTORY = Storage.DIRECTORY;
    private static final String DCIM_DIRECTORY = Storage.DCIM;
    private static final String CONFIG_DIRECTORY = Environment
            .getExternalStorageDirectory().getAbsolutePath();
    private static final String CAMERA_ID_BACK = "0";
    private static final String CAMERA_ID_FRONT = "1";
    private final static String EXTRAS_CAMERA_FACING = "android.intent.extras.CAMERA_FACING";
    private File cameraDirectory = new File(CAMERA_DIRECTORY);
    private String sNull = "ety";
    private long CameraAppLaunchTime = 0;
    private ComboPreferences comb;
    private TestConfigurationManager mTestConfigurationManager;
    private Instrumentation mInstrumentation;
    private Activity mActivity;
    public Context mActivityContext;

    public CameraSettingsStressTests() {
        super(CameraActivity.class);
        Log.v(TAG, "Camera Constructor");
    }

    @Override
    protected void setUp() throws Exception {
        Log.v(TAG, "camera : setUp() ");
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
            return launchBackCamera();
        }
        if (cameraID.equals(CAMERA_ID_FRONT)) {
            return launchFrontCamera();
        }
        return null;
    }

    private Activity launchBackCamera() throws InterruptedException {
        Log.v(TAG, "Launching back camera");
        Intent intent = new Intent();
        intent.setClass(mActivityContext, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRAS_CAMERA_FACING,
                android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
        Activity act = mInstrumentation.startActivitySync(intent);
        Log.v(TAG, "Back Camera sucessfully launched");
        return act;
    }

    private Activity launchFrontCamera() throws InterruptedException {
        Log.v(TAG, "Launching front camera");
        Intent intent = new Intent();
        intent.setClass(mActivityContext, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRAS_CAMERA_FACING,
                android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        Activity act = mInstrumentation.startActivitySync(intent);
        Log.v(TAG, "Front Camera sucessfully launched");
        return act;
    }

    private Activity launchPreferencesCamera() throws InterruptedException {
        Log.v(TAG, "launching preferences Camera");
        Intent intent = new Intent();
        intent.setClass(mActivityContext, CameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Activity act = mInstrumentation.startActivitySync(intent);
        Log.v(TAG, "Preferences Camera sucessfully launched");
        return act;
    }

    public void imageCapture(String PhotoSize, long BurstLength,
            int numberOfPicturesToTake, String CameraID) throws InterruptedException,
            IOException, FileNotFoundException {
        Log.v(TAG, "imageCapture");

        CameraHelper.deleteAllFilesInFolder(cameraDirectory);

        long initialCameraDirectorySize = CameraHelper.getFolderSize(cameraDirectory);
        Log.v(TAG, "imageCapture= initialCameraDirectorySize= "
                + initialCameraDirectorySize);
        long initialNumberOfPictures = CameraHelper.getFolderFilesNumber(cameraDirectory);
        Log.v(TAG, "imageCapture= initialNumberOfPictures= " + initialNumberOfPictures);

        // start Camera App
        mActivity = launchCamera(CameraID);
        Log.v(TAG, "Camera App was started");

        // read Camera ID
        if (PhotoSize != NULL_PHOTO_SIZE) {
            comb = new ComboPreferences(mActivityContext);
            Log.v(TAG,
                    "CameraSettings.KEY_CAMERA_ID== "
                            + comb.getString(CameraSettings.KEY_CAMERA_ID,
                                    DEFAULT_PREF_VALUE));
            assertEquals("CameraID read from file was not set", CameraID,
                    comb.getString(CameraSettings.KEY_CAMERA_ID, DEFAULT_PREF_VALUE));
        }
        for (int i = 0; i < numberOfPicturesToTake; i++) {

            // take picture
            KeyEvent focusEvent = new KeyEvent(KeyEvent.ACTION_DOWN,
                    KeyEvent.KEYCODE_FOCUS);
            Log.v(TAG, "take picture iteration = " + i);
            Thread.sleep(WAIT_FOR_FOCUS);
            mInstrumentation.sendKeySync(focusEvent);
            mInstrumentation.sendCharacterSync(KeyEvent.KEYCODE_CAMERA);
            Log.v(TAG, "sent KEYCODE_CAMERA to take picture");
            Thread.sleep(WAIT_FOR_IMAGE_CAPTURE_TO_BE_TAKEN);

            if (BurstLength > 1) {
                Log.v(TAG, "BurstLength= " + BurstLength);
                // wait to allow burst photos to be saved in Camera folder
                Thread.sleep(WAIT_TO_SAVE_BURST_IMAGES);
            }
        }
        // close Camera App
        mActivity.finish();

        long finalCameraDirectorySize = CameraHelper.getFolderSize(cameraDirectory);

        long finalNumberOfPictures = CameraHelper.getFolderFilesNumber(cameraDirectory);
        Log.v(TAG, "imageCapture= finalNumberOfPictures= " + finalNumberOfPictures);

        long NumberOfPictureTaken = finalNumberOfPictures - initialNumberOfPictures;
        assertTrue(
                "Camera Directory Size was not changed which means picture was not saved  = ",
                (finalCameraDirectorySize - initialCameraDirectorySize > 0));

        assertEquals("Number of picture taken is not equal with Burst Length ",
                (BurstLength * numberOfPicturesToTake), NumberOfPictureTaken);

        if (PhotoSize != "0") {
            checkPhotoSize(cameraDirectory, PhotoSize);
        }

    }

    public void checkPhotoSize(File dir, String PhotoSize) throws IOException,
            FileNotFoundException {
        String Size = "";
        int i = 1;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                try {
                    ExifInterface imageInfo = new ExifInterface(file.getAbsolutePath());
                    String width = imageInfo.getAttribute(imageInfo.TAG_IMAGE_WIDTH);
                    String length = imageInfo.getAttribute(imageInfo.TAG_IMAGE_LENGTH);
                    Log.v(TAG, "iteration== " + i + "\n Width= " + width + "\n Height= "
                            + length);
                    Size = width + "x" + length;
                    assertEquals("the size of the picture taken is not valid ",
                            PhotoSize, Size);
                    i = i + 1;
                } catch (IOException e) {
                    Log.v(TAG, "File format not supported");
                    assertEquals("the picture is not in jpeg format ", false);
                }
            }
        }
    }

    public void setParameters(ComboPreferences comb, String Key_Pref, String value) {
        Editor editor = comb.edit();
        editor.putString(Key_Pref, value);
        boolean status = editor.commit();
        assertTrue("Preferences Commit was not successful= ", status);
        Log.v(TAG, Key_Pref + "== " + comb.getString(Key_Pref, DEFAULT_PREF_VALUE));

    }

    private void changeCamera(ComboPreferences comb, String value)
            throws InterruptedException {
        comb.setLocalId(mActivityContext, Integer.parseInt(value));
        setParameters(comb, CameraSettings.KEY_CAMERA_ID, value);
        Thread.sleep(WAIT_FOR_PARAMETERS_TO_BE_SAVED);
        Thread.sleep(WAIT_FOR_PREFERENCE_CHANGE);
    }

    public void updateCameraSettings(Properties props) throws InterruptedException {

        String CameraID = props.getProperty("CameraID");
        String StoreLocation = props.getProperty("StoreLocation");
        String FlashMode = props.getProperty("FlashMode");
        String Exposure = props.getProperty("Exposure");
        String Psize = props.getProperty("Psize");
        String RedEye = props.getProperty("RedEye");
        String DVS = props.getProperty("DVS");
        String Bl = props.getProperty("Bl");
        String Bfps = props.getProperty("Bfps");
        String ColorCorection = props.getProperty("ColorCorection");
        String DIS = props.getProperty("DIS");
        String Pquality = props.getProperty("Pquality");
        String SceneMode = props.getProperty("SceneMode");
        String ColorEfect = props.getProperty("ColorEfect");
        String WiteBalance = props.getProperty("WiteBalance");
        String FocusMode = props.getProperty("FocusMode");

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
            changeCamera(comb, CameraID);
            mActivity.finish();
            defaultCam = true;
            // start Camera App
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
            act = launchPreferencesCamera();

            // read Camera ID
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
            comb = new ComboPreferences(mActivityContext);
            CameraID1 = comb.getString(CameraSettings.KEY_CAMERA_ID, DEFAULT_PREF_VALUE);
            Log.v(TAG, "CameraSettings.KEY_CAMERA_ID CameraID1 == " + CameraID1);
            Log.v(TAG, "CameraSettings.KEY_CAMERA_ID CameraID == " + CameraID);

        }

        // set camera
        if (Integer.parseInt(CameraID1) != Integer.parseInt(CameraID)) {
            Log.v(TAG, "Change Camera");
            changeCamera(comb, CameraID);
            Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);
        }

        // read Camera ID
        mActivityContext = mInstrumentation.getTargetContext();
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

        if (FlashMode.equals(sNull) == false) {
            Log.v(TAG, "FlashMode");
            setParameters(comb, CameraSettings.KEY_FLASH_MODE, FlashMode);
        }

        if (Exposure.equals(sNull) == false) {
            Log.v(TAG, "Exposure");
            setParameters(comb, CameraSettings.KEY_EXPOSURE, Exposure);
        }

        if (Psize.equals(sNull) == false) {
            Log.v(TAG, "Psize");
            setParameters(comb, CameraSettings.KEY_PICTURE_SIZE, Psize);
        }

        if (Pquality.equals(sNull) == false) {
            Log.v(TAG, "Pquality");
            setParameters(comb, CameraSettings.KEY_JPEG_QUALITY, Pquality);
        }

        if (SceneMode.equals(sNull) == false) {
            Log.v(TAG, "SceneMode");
            setParameters(comb, CameraSettings.KEY_SCENE_MODE, SceneMode);
        }

        if (SceneMode == "auto") {
            if (WiteBalance.equals(sNull) == false) {
                Log.v(TAG, "WiteBalance");
                setParameters(comb, CameraSettings.KEY_WHITE_BALANCE, WiteBalance);
            }

            if (FocusMode.equals(sNull) == false) {
                Log.v(TAG, "FocusMode");
                setParameters(comb, CameraSettings.KEY_FOCUS_MODE, FocusMode);
            }
        }
        // close activity to submit preferences
        Thread.sleep(WAIT_FOR_CAMERA_OPERATIONS);

        if (defaultCam == false) {
            mActivity.finish();
        } else {
            mActivity.finish();
        }
    }

    public void restorePreferences(int CameraId) throws InterruptedException {
        launchCamera(String.valueOf(CameraId));
        ComboPreferences comb = new ComboPreferences(mActivityContext);
        comb.setLocalId(mActivityContext, CameraId);
        Editor editor = comb.edit();
        editor.clear();
        editor.apply();
        Thread.sleep(WAIT_FOR_RESTORE_PREFERENCES);
        mActivity.finish();
    }

    public void restoreDefaultCameraSettings(Properties cProps)
            throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG,
                "This test will verify that restore Camera Settings to Default works as expected");

        // set preferences
        // CameraID,StoreLocation, FlashMode, Exposure, Psize, RedEye, DVS, Bl,
        // Bfps, ColorCorection, DIS, Pquality, SceneMode, ColorEfect,
        // WiteBalance,FocusMode
        updateCameraSettings(cProps);
        Log.v(TAG, "Updated Camera Settings");

        // restore default settings
        String cameraID = cProps.getProperty("CameraID");
        String storeLoc = cProps.getProperty("StoreLocation");
        String photoSize = cProps.getProperty("Psize");
        restorePreferences(Integer.parseInt(cameraID));
        Log.v(TAG, "Restored Camera Settings");

        mActivity = launchCamera(cProps.getProperty("CameraID"));
        mActivityContext = mInstrumentation.getTargetContext();
        ComboPreferences comb = new ComboPreferences(mActivityContext);
        comb.setLocalId(mActivityContext, Integer.parseInt(cameraID));
        // check preferences were changed
        assertFalse("preferences were not set to default ", storeLoc.equals(comb
                .getString(CameraSettings.KEY_RECORD_LOCATION, DEFAULT_PREF_VALUE)));
        assertFalse("preferences were not set to default ", photoSize.equals(comb
                .getString(CameraSettings.KEY_PICTURE_SIZE, DEFAULT_PREF_VALUE)));
        mActivity.finish();
        Log.v(TAG, "Instrumentation test stop");

    }

    public void cameraLaunchTime(Properties cProps) throws InterruptedException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG,
                "This test will determine Camera app Launch Time and will fail if it is > 2000ms");

        // set preferences
        // CameraID,StoreLocation, FlashMode, Exposure, Psize, RedEye, DVS, Bl,
        // Bfps, ColorCorection, DIS, Pquality, SceneMode, ColorEfect,
        // WiteBalance,FocusMode
        updateCameraSettings(cProps);

        // start Camera
        long beforeStart = System.currentTimeMillis();
        launchCamera(cProps.getProperty("CameraID"));
        long cameraStarted = System.currentTimeMillis();
        CameraAppLaunchTime = cameraStarted - beforeStart;

        Log.v(TAG, " Camera startup time == CameraAppLaunchTime:== "
                + CameraAppLaunchTime);
        assertFalse(
                " Camera startup time on Intel Table is >2000ms so this test fails = ",
                (CameraAppLaunchTime > 2000));

        Log.v(TAG, "Instrumentation test stop");
    }

    public void takePicture(Properties cProps) throws InterruptedException, IOException {
        Log.v(TAG, "Instrumentation test start");

        // set preferences
        // CameraID,StoreLocation, FlashMode, Exposure, Psize, RedEye, DVS, Bl,
        // Bfps, ColorCorection, DIS, Pquality, SceneMode, ColorEfect,
        // WiteBalance,FocusMode
        updateCameraSettings(cProps);

        // take picture
        // photosize, burstlength, NumberOFpictureTobeTaken, CameraID
        String photoSize = cProps.getProperty("Psize");
        String burstlength = cProps.getProperty("Bl");
        String numPics = cProps.getProperty("NumberOFpictureTobeTaken");
        String camID = cProps.getProperty("CameraID");
        if (burstlength.equals(sNull)) {
            imageCapture(photoSize, 1, Integer.parseInt(numPics), camID);
        } else {
            imageCapture(photoSize, Integer.parseInt(burstlength),
                    Integer.parseInt(numPics), camID);
        }
        Log.v(TAG, "Instrumentation test stop");
    }

    public void testTakePictureBAT() throws InterruptedException, IOException {
        Log.v(TAG, "Instrumentation test start");
        Log.v(TAG,
                "This test will check that it is possible to open Camera App and take 1 picture ");

        // picturesize, burstlength, NumberOFpictureTobeTaken, CameraID
        imageCapture("0", 1, 1, "0");
        Log.v(TAG, "Instrumentation test stop");

    }

    public void testTakePicture() throws IOException, InterruptedException {
        Properties cameraProps = mTestConfigurationManager
                .getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", cameraProps);
        takePicture(cameraProps);
    }

    public void testCameraLaunchTime() throws IOException, InterruptedException {
        Properties cameraProps = mTestConfigurationManager
                .getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", cameraProps);
        cameraLaunchTime(cameraProps);
    }

    public void testRestoreDefaultCameraSettings() throws IOException,
            InterruptedException {
        Properties cameraProps = mTestConfigurationManager
                .getProperties(CONFIG_DIRECTORY);
        assertNotNull(TAG + " could not load config properties", cameraProps);
        restoreDefaultCameraSettings(cameraProps);
    }
}
