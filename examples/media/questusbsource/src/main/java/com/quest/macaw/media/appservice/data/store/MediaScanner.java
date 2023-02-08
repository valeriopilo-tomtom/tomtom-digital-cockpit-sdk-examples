package com.quest.macaw.media.appservice.data.store;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

import com.quest.macaw.media.common.SongInfo;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class MediaScanner {

    private static final String TAG = "MediaScanner";
    private static final String UNKNOWN = "Unknown";
    private static OnMediaScannerComplete mOnMediaScannerComplete;
    private static int mMediaId = 100000;

    private MediaScanner() {
        //Private constructor.
    }

    public static void scanFile(Context context, String path, OnMediaScannerComplete onMediaScannerComplete) {
        mOnMediaScannerComplete = onMediaScannerComplete;
        Thread scanThread = new Thread(() -> {
            List<SongInfo> songInfoList = new ArrayList<>();
            File filepath = new File(path);
            if (!filepath.isFile()) {
                try {
                    Log.i(TAG, "scanFile: " + path);
                    recursiveScan(context, songInfoList, filepath);
                } catch (IOException e) {
                    Log.e(TAG, "scanFile: " + e.getMessage());
                }
            } else {
                SongInfo songInfo = getMetaData(context, filepath);
                if (songInfo != null) {
                    songInfoList.add(songInfo);
                }
            }
            if (onMediaScannerComplete != null) {
                onMediaScannerComplete.onScanComplete(songInfoList);
            }
        });
        scanThread.start();
    }

    private static void recursiveScan(Context context, List<SongInfo> songInfoList, File filepath) throws IOException {
        Files.walkFileTree(Paths.get(filepath.getAbsolutePath()), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) {
                Log.i(TAG, "visitFile: " + path.toString());
                if (path.toString().endsWith(".mp3") && path.toFile().canRead()) {
                    SongInfo songInfo = getMetaData(context, path.toFile());
                    if (songInfo != null) {
                        songInfoList.add(songInfo);
                        if (songInfoList.size() == 25 && mOnMediaScannerComplete != null) {
                            Log.i(TAG, "visitFile: size is 50");
                            mOnMediaScannerComplete.onScan(songInfoList);
                        }
                    }
                    Log.d(TAG, "recursiveScan: " + path.toString());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                Log.e(TAG, "Failed reading " + file.toString() + " -- skipping");
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static SongInfo getMetaData(Context context, File file) {
        try (ParcelFileDescriptor parcelFileDescriptor =
                     context.getContentResolver().openFileDescriptor(Uri.fromFile(file), "r");
             MediaMetadataRetriever retriever = new MediaMetadataRetriever()) {
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            retriever.setDataSource(fileDescriptor);

            SongInfo songInfo = new SongInfo();
            String mediaId = Integer.toString(mMediaId);
            songInfo.setMediaId(mediaId);
            String title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String albumName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
            String artistName = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            songInfo.setTitle(TextUtils.isEmpty(title) ? UNKNOWN : title);
            songInfo.setAlbumName(TextUtils.isEmpty(albumName) ? UNKNOWN : albumName);
            songInfo.setArtistName(TextUtils.isEmpty(artistName) ? UNKNOWN : artistName);
            songInfo.setDuration(Long.valueOf(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
            songInfo.setCoverArtUrl(Uri.fromFile(file).toString());
            songInfo.setSongUri(Uri.fromFile(file));
            songInfo.setIsLocalPath(true);
            mMediaId++;

            Log.i(TAG, "getMetaData: " + mediaId);
            return songInfo;
        } catch (Exception e) {
            Log.e(TAG, "getMetaData: " + e.getMessage());
        }

        return null;
    }

    public interface OnMediaScannerComplete {

        void onScan(List<SongInfo> songInfoList);

        void onScanComplete(List<SongInfo> songInfoList);
    }
}
