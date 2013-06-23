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

package com.android.camera;

import java.io.*;
import android.util.Log;
import com.android.camera.Storage;

/**
 * class contains helper functions for test clases in the camera package
 */

public class CameraHelper {
    private final static String TAG = "CameraHelper";
    private final static String TMP_DIR = Storage.DCIM + "/tmp_tests";

    public static long getFolderSize(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            if (file.isFile()) {
                Log.v(TAG, "Picture taken file == " + file.getName() + " file size= "
                        + file.length());
                size += file.length();
            } else
                size += getFolderSize(file);
        }
        return size;
    }

    public static long getFolderFilesNumber(File dir) {
        long size = 0;
        for (File file : dir.listFiles()) {
            size += 1;
        }
        return size;
    }

    public static void deleteAllFilesInFolder(File dir) {
        for (File file : dir.listFiles()) {
            removeFiles(file.getAbsolutePath());
        }
    }

    protected static void removeFiles(String fileName) {
        File fileToDelete = new File(fileName);

        if (fileToDelete == null || fileToDelete.exists() == false) {
            Log.v(TAG, "Output file was not created \n");
        } else {
            try {
                Log.v(TAG, "fileToDelete.getName() = " + fileToDelete.getName());

                boolean exists = (new File(TMP_DIR)).exists();
                if (exists == false) {
                    // File or directory exists
                    boolean success = (new File(TMP_DIR)).mkdirs();
                    Log.v(TAG, "success = " + success);
                }

                fileToDelete.renameTo(new File(TMP_DIR + fileToDelete.getName()));

            } catch (Exception ex) {
                Log.v(TAG, "Output file is present but could not be deleted");
            }
        }
    }
}