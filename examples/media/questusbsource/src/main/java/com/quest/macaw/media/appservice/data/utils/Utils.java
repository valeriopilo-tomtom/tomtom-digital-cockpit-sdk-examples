/*
 * Copyright (c) 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.quest.macaw.media.appservice.data.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

public class Utils {
    public static final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
    };
    private static final String TAG = "Utils";
    public static final String ERROR_RESOLUTION_ACTION_INTENT =
            "android.media.extras.ERROR_RESOLUTION_ACTION_INTENT";

    public static final String ERROR_RESOLUTION_ACTION_LABEL =
            "android.media.extras.ERROR_RESOLUTION_ACTION_LABEL";

    public static Uri getUriForResource(Context context, int id) {
        Resources res = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + "://" + res.getResourcePackageName(id)
                + "/" + res.getResourceTypeName(id)
                + "/" + res.getResourceEntryName(id));
    }

    public static boolean hasRequiredPermissions(Context context) {
        for (String permission : PERMISSIONS) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
    public static Bitmap getCoverArtLocal(Context context, Uri uri){
        Log.d(TAG, "getCoverArtImage: uri " + uri.toString());
        Bitmap bitmap;
        try (ParcelFileDescriptor parcelFileDescriptor =
                     context.getContentResolver().openFileDescriptor(uri, "r");
             MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            retriever.setDataSource(fileDescriptor);
            byte[] art = retriever.getEmbeddedPicture();
            Log.d(TAG, "getCoverArtImage: art " + art);
            if (art != null) {
                bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                Log.d(TAG, "getCoverArtImage: " + bitmap);
                return bitmap;
            } else {
                throw new IOException("Cover art image is null");
            }
        } catch (Exception e) {
            Log.e(TAG, "getCoverArtImage: " + e.getMessage());
            return null;
        }
    }

    public static Bitmap getUSBCoverArtImage(Context context, String musicUri) {
        try {
            Log.i(TAG, "getUSBCoverArtImage music uri: "+ musicUri);
            Uri musicFileUri = Uri.parse(musicUri);
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(musicFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e){
            Log.e(TAG, " getUSBCoverArtImage Exception"+e.getMessage());
            return null;
        }
    }
}
