package com.androidcourse.toktik.player;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;

import com.androidcourse.toktik.util.ProxyServer;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoManager {

    private static boolean firstLoad = true;
    private IjkMediaPlayer mMediaPlayer = null;

    private Context context;


    private MediaSourceType mediaSourceType;

    private String videoPath = "";
    private Uri videoUri = null;
    private int videoResourceId = 0;

    private boolean cycle = true;

    public VideoManager(Context context) {
        if (firstLoad) {
            try {
                IjkMediaPlayer.loadLibrariesOnce(null);
                IjkMediaPlayer.native_profileBegin("libijkplayer.so");
                firstLoad = false;
            } catch (Exception e) {

            }
        }
        init(context);
    }

    ;


    private void init(Context context) {
        this.context = context;
    }

    public void setVideoPath(String videoPath) {
        this.mediaSourceType = MediaSourceType.LINK;
        Log.d("video",videoPath);
        if(videoPath.startsWith("/")){
            this.videoPath =  videoPath;
        }else{
            String path = ProxyServer.getProxy(context).getProxyUrl(videoPath);
            this.videoPath = path;
        }
    }

    public void setVideoUri(Uri videoUri) {
        this.mediaSourceType = MediaSourceType.URI;
        this.videoUri = videoUri;
    }

    public void setVideoResourceId(int videoResourceId) {
        this.mediaSourceType = MediaSourceType.NATIVE_RESOURCE;
        this.videoResourceId = videoResourceId;
    }

    public void prepareAsync() {
        mMediaPlayer.prepareAsync();
    }

    public void setDisplay(SurfaceHolder sh) {
        if (mMediaPlayer == null) {
            createPlayer();
        }
        mMediaPlayer.setDisplay(sh);
    }

    public void load() {
        createPlayer();
        mMediaPlayer.setLooping(cycle);
        try {
            switch (this.mediaSourceType) {
                case URI:
                    mMediaPlayer.setDataSource(context, videoUri);
                    break;
                case LINK:
                    mMediaPlayer.setDataSource(videoPath);
                    break;
                case NATIVE_RESOURCE:
                    AssetFileDescriptor fileDescriptor = context.getResources().openRawResourceFd(videoResourceId);
                    RawDataSourceProvider provider = new RawDataSourceProvider(fileDescriptor);
                    mMediaPlayer.setDataSource(provider);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * ??????????????????player
     */
    private void createPlayer() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.setDisplay(null);
            mMediaPlayer.release();
        }
        IjkMediaPlayer ijkMediaPlayer = new IjkMediaPlayer();
        //???????????????
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
        //????????????seek
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        mMediaPlayer = ijkMediaPlayer;
        mMediaPlayer.setSpeed(1f);
    }

    /**
     * ????????????
     */
    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    /**
     * ?????????????????????
     */
    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**
     * ????????????
     */
    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    /**
     * ????????????
     */
    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    /**
     * ????????????
     */
    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public long getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    /**
     * ??????????????????
     *
     * @return
     */
    public long getCurrentPosition() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    /**
     * ???????????????????????????
     *
     * @return
     */
    public boolean isPlaying() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    /**
     * ??????????????????
     *
     * @param l
     */
    public void seekTo(long l) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(l);
        }
    }

    public boolean isPlayable() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlayable();
        }
        return false;
    }

    public int getVideoWidth() {
        return mMediaPlayer.getVideoWidth();
    }

    public int getVideoHeight() {
        return mMediaPlayer.getVideoHeight();
    }

    //????????????
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener onCompletionListener) {
        mMediaPlayer.setOnCompletionListener(onCompletionListener);
    }

    //?????????
    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
        mMediaPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
    }

    public void setOnControlMessageListener(IjkMediaPlayer.OnControlMessageListener onControlMessageListener) {
        mMediaPlayer.setOnControlMessageListener(onControlMessageListener);
    }

    //????????????????????????
    public void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener) {
        mMediaPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    public void setOnMediaCodecSelectListener(IjkMediaPlayer.OnMediaCodecSelectListener onMediaCodecSelectListener) {
        mMediaPlayer.setOnMediaCodecSelectListener(onMediaCodecSelectListener);
    }

    public void setOnNativeInvokeListener(IjkMediaPlayer.OnNativeInvokeListener onNativeInvokeListener) {
        mMediaPlayer.setOnNativeInvokeListener(onNativeInvokeListener);
    }

    //??????????????????????????????
    public void setOnErrorListener(IMediaPlayer.OnErrorListener onErrorListener) {
        mMediaPlayer.setOnErrorListener(onErrorListener);
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
    }

    //????????????
    public void setOnInfoListener(IMediaPlayer.OnInfoListener onInfoListener) {
        mMediaPlayer.setOnInfoListener(onInfoListener);
    }

    /**
     * ??????????????????????????????????????????????????????
     *
     * @param onPreparedListener
     */
    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener onPreparedListener) {
        mMediaPlayer.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                VideoManager.this.start();
                onPreparedListener.onPrepared(iMediaPlayer);
            }
        });
    }


}
