package com.quest.macaw.media.appservice.data.alexa;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MediaSourceEventListener;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

/**
 * Factory class to get the media source based on the URI received from Alexa service
 */
public class AlexaMediaSourceFactory {
    private static final String TAG = AlexaMediaSourceFactory.class.getSimpleName();
    private static final String USER_AGENT = "ShowcaseMediaUA";
    private static final int CONNECTION_TIMEOUT_MS = 8000;
    private static final int READ_TIMEOUT_MS = 20000;
    private final Context mContext;
    private final Handler mMainHandler = new Handler();
    private final PlaylistParser mPlaylistParser = new PlaylistParser();
    private final MediaSourceListener mMediaSourceListener = new MediaSourceListener();
    private DataSource.Factory mOkHttpDataSourceFactory;

    public AlexaMediaSourceFactory(Context context) {
        mContext = context;
        mOkHttpDataSourceFactory = buildOkHttpDataSourceFactory(mContext);
    }

    private HttpDataSource.Factory buildOkHttpDataSourceFactory(Context context) {
        // Some streams may see a long response time to begin data transfer from server after
        // connection. Use default 8 second connection timeout and increased 20 second read timeout
        // to catch this case and avoid reattempts to connect that will continue to time out.
        // May perceive long "dead time" in cases where data read takes a long time
        return new DefaultHttpDataSource.Factory()
                .setUserAgent(USER_AGENT)
                .setConnectTimeoutMs(CONNECTION_TIMEOUT_MS)
                .setReadTimeoutMs(READ_TIMEOUT_MS)
                .setAllowCrossProtocolRedirects(true);
    }

    public MediaSource createHttpMediaSource(final Uri uri) throws Exception {
        return createMediaSource(uri, mOkHttpDataSourceFactory, mMediaSourceListener, mMainHandler, mPlaylistParser);
    }

    private MediaSource createMediaSource(final Uri uri, final DataSource.Factory dataSourceFactory,
            final MediaSourceEventListener mediaSourceListener, final Handler handler,
            final PlaylistParser playlistParser) throws Exception {
        MediaType type = MediaType.inferContentType(uri.getLastPathSegment());
        Log.i(TAG, " MediaType " + type);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        switch (type) {
            case DASH:
                Log.d(TAG, "dash");
                DashMediaSource dashMediaSource = new DashMediaSource
                        .Factory(new DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                        .createMediaSource(mediaItem);
                dashMediaSource.addEventListener(handler, mediaSourceListener);
                return dashMediaSource;
            case SMOOTH_STREAMING:
                Log.d(TAG, "smooth streaming");
                SsMediaSource ssMediaSource = new SsMediaSource
                        .Factory(new DefaultSsChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                        .createMediaSource(mediaItem);
                ssMediaSource.addEventListener(handler, mediaSourceListener);
                return ssMediaSource;
            case HLS:
                Log.d(TAG, "hls");
                HlsMediaSource hlsMediaSource = new HlsMediaSource
                        .Factory(dataSourceFactory)
                        .createMediaSource(mediaItem);
                hlsMediaSource.addEventListener(handler, mediaSourceListener);
                return hlsMediaSource;
            case M3U:
            case PLS:
                Log.d(TAG, "pls m3");
                Uri parsedUri = playlistParser.parseUri(uri);
                return createMediaSource(parsedUri, dataSourceFactory, mediaSourceListener, handler, playlistParser);
            case OTHER:
                Log.d(TAG, "other");
                ProgressiveMediaSource progressiveMediaSource = new ProgressiveMediaSource
                        .Factory(dataSourceFactory)
                        .createMediaSource(mediaItem);
                progressiveMediaSource.addEventListener(handler, mediaSourceListener);
                return progressiveMediaSource;
            default:
                throw new IllegalStateException("Unsupported type");
        }
    }

    /**
     * Media types for creating an ExoPlayer MediaSource
     **/
    enum MediaType {
        DASH(C.TYPE_DASH),
        SMOOTH_STREAMING(C.TYPE_SS),
        HLS(C.TYPE_HLS),
        OTHER(C.TYPE_OTHER),
        M3U(4),
        PLS(5);

        private final int mType;

        MediaType(int type) {
            mType = type;
        }

        public int getType() {
            return mType;
        }

        public static MediaType inferContentType(@Nullable final String fileExtension) {
            if (fileExtension == null) {
                return OTHER;
            } else if (fileExtension.endsWith(".ashx") || fileExtension.endsWith(".m3u")) {
                return M3U;
            } else if (fileExtension.endsWith(".pls")) {
                return PLS;
            } else {
                int type = Util.inferContentType(fileExtension);
                for (MediaType mediaType : MediaType.values()) {
                    if (mediaType.getType() == type)
                        return mediaType;
                }
                return OTHER;
            }
        }
    }

    /**
     * Media Source event listener
     **/
    private static class MediaSourceListener implements MediaSourceEventListener {
        // TODO: Listen for errors and let the AACS know of those errors.
    }
}