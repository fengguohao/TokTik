package com.androidcourse.toktik.player;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.androidcourse.toktik.R;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class VideoPlayerIJK extends FrameLayout {

    private VideoManager videoManager;
    Runnable flushBar;
    private boolean hasCreateSurfaceView = false;

    private SurfaceView surfaceView;

    private ImageButton playorpauseButton;
    private LinearLayout controlBar;
    private TextView currTime;
    private TextView fullTime;
    private SeekBar videoSeekBar;
    private LinearLayout timeInfo;

    private boolean mDragging = false;
    private int newPosition = 0;

    private boolean firstLoad = true;
    private IMediaPlayer.OnPreparedListener onPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {

        }
    };
    private IMediaPlayer.OnCompletionListener onCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {

        }
    };


    private Handler seekBarHandler;
    private PlayerSurfaceCallback callback;

    public void setPlayorpauseButton(ImageButton playorpauseButton) {
        this.playorpauseButton = playorpauseButton;
    }

    public void setControlBar(LinearLayout controlBar) {
        this.controlBar = controlBar;
    }

    public void setCurrTime(TextView currTime) {
        this.currTime = currTime;
    }

    public void setFullTime(TextView fullTime) {
        this.fullTime = fullTime;
    }

    public void setVideoSeekBar(SeekBar videoSeekBar) {
        this.videoSeekBar = videoSeekBar;
    }

    public void setTimeInfo(LinearLayout timeInfo) {
        this.timeInfo = timeInfo;
    }

    private VideoTouchListener videoTouchListener;

    public VideoPlayerIJK(@NonNull Context context) {
        super(context);
    }

    public VideoPlayerIJK(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoPlayerIJK(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public VideoPlayerIJK(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void preInit(){
        videoManager = new VideoManager(getContext());
    }

    /**
     * ???????????????????????????????????????????????????????????????/??????????????????????????????????????????
     */
    public void init(){

        this.videoTouchListener = new VideoTouchListener() {
            @Override
            public void onSingleTouch() { }
            @Override
            public void onDoubleTouch() { }
        };

        playorpauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoManager.isPlaying()){
                    playorpauseButton.setImageDrawable(getContext().getDrawable(R.drawable.pause));
                    videoManager.pause();
                }else{
                    playorpauseButton.setImageDrawable(getContext().getDrawable(R.drawable.play));
                    videoManager.start();
                }
            }
        });

        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    newPosition = progress;
                    seekBarHandler.removeCallbacks(VideoPlayerIJK.this.flushBar);
                    currTime.setText(longToTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mDragging=true;
                showTimeInfo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mDragging=false;
                seekBarHandler.postDelayed(flushBar,1000);
                videoManager.seekTo((newPosition));
                hideTimeInfo();
            }
        });

        // ???????????????????????????
        seekBarHandler = new Handler();
        flushBar = new Runnable() {
            @Override
            public void run() {
                videoSeekBar.setMax((int) (videoManager.getDuration()));
                videoSeekBar.setProgress((int)(videoManager.getCurrentPosition()));
                seekBarHandler.postDelayed(this,1000);
                currTime.setText(longToTime(videoManager.getCurrentPosition()));
                fullTime.setText(longToTime(videoManager.getDuration()));
            }
        };

        //???????????????????????????
        seekBarHandler.removeCallbacks(flushBar);
        seekBarHandler.postDelayed(flushBar,1000);
    }

    /**
     * ?????????????????????
     * ?????????????????????????????????????????????????????????
     *
     * @param path the path of the video.
     */
    public void setVideoPath(String path) {
        videoManager.setVideoPath(path);
    }

    public void setVideoResource(int resourceId) {
        videoManager.setVideoResourceId(resourceId);
    }

    public void setVideoUri(Uri uri) {
        videoManager.setVideoUri(uri);
    }

    /**
     * ????????????????????????????????? ??????
     */
    public void startPlay(){
        Log.d("video-debug","load resource");
        //???????????????
        videoManager.load();
        //?????????????????????
        Log.d("video-debug","create surface");
        createSurfaceView();

        // ?????????????????????????????????????????????????????????
        videoManager.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                int width = VideoPlayerIJK.this.getWidth();
                int height = VideoPlayerIJK.this.getHeight();
                int vWidth = videoManager.getVideoWidth();
                int vHeight = videoManager.getVideoHeight();
                if(height==0||vHeight==0){
                    return;
                }
                int screenRate = width/height;
                int videoRate = vWidth/vHeight;
                ViewGroup.LayoutParams layoutParams = VideoPlayerIJK.this.getLayoutParams();
                if(screenRate<videoRate){
                    VideoPlayerIJK.this.setSize(width,(width*vHeight)/vWidth);
                }else{
                    VideoPlayerIJK.this.setSize((height*vWidth)/vHeight,height);
                }
                // ??????custom listener
                onPreparedListener.onPrepared(iMediaPlayer);
            }
        });
    }

    public void setVideoTouchListener(VideoTouchListener videoTouchListener){
        if(videoTouchListener!=null){
            this.videoTouchListener = videoTouchListener;
        }
    }

    /**
     * ????????????surfaceview
     */
    private void createSurfaceView() {
        //??????????????????surface view
        surfaceView = new SurfaceView(getContext());
        callback = new PlayerSurfaceCallback();
        surfaceView.getHolder().addCallback(callback);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        surfaceView.setLayoutParams(layoutParams);
        this.addView(surfaceView);
        hasCreateSurfaceView = true;
    }

    /**
     * surfaceView????????????
     */
    private class PlayerSurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            //surfaceview??????????????????????????????
            //???mediaPlayer????????????
            videoManager.setDisplay(holder);
            videoManager.prepareAsync();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            videoManager.setDisplay(holder);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            videoManager.stop();
            videoManager.release();
        }
    }

    private void setSize(int width, int height){
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        ViewGroup.LayoutParams thislayout = this.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        thislayout.width = width;
        thislayout.height = height;
        surfaceView.setLayoutParams(layoutParams);
        this.setLayoutParams(thislayout);
    }

    private void showTimeInfo(){
        timeInfo.setAlpha(1f);
    }

    private void hideTimeInfo(){
        timeInfo.setAlpha(0f);
    }

    /**
     * ???????????????????????????????????????????????????????????????????????????
     */
    public void start() {
        if(videoManager.isPlayable()){
            Log.d("video-debug","video can play, normal play");
            videoManager.start();
        }else{
            Log.d("video-debug","video cannot play");
            this.startPlay();
        }

    }

    /**
     * ?????????????????????
     */
    public void release() {
        if(surfaceView!=null&&surfaceView.getHolder()!=null){
            surfaceView.getHolder().removeCallback(callback);
            callback=null;
        }
        videoManager.release();
    }

    /**
     * ????????????
     */
    public void pause() {
        videoManager.pause();
    }

    /**
     * ????????????
     */
    public void stop() {
        videoManager.stop();
    }

    /**
     * ????????????
     */
    public void reset() {
        videoManager.reset();
    }

    /**
     * ??????????????????
     * @return
     */
    public long getDuration() {
        return videoManager.getDuration();
    }

    /**
     * ??????????????????
     * @return
     */
    public long getCurrentPosition() {
        return videoManager.getCurrentPosition();
    }

    /**
     * ???????????????????????????
     * @return
     */
    public boolean isPlaying() {
        return videoManager.isPlaying();
    }

    /**
     * ??????????????????
     * @param l
     */
    public void seekTo(long l) {
        videoManager.seekTo(l);
    }

    private String longToTime(long t){
        t = t/1000;
        String min = (int)(t/60)+"";
        String sec = (int)(t%60)+"";
        if(min.length()<2){
            min = "0"+min;
        }
        if(sec.length()<2){
            sec = "0"+sec;
        }
        return min+":"+sec;
    }

    //????????????
    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener onCompletionListener){
        videoManager.setOnCompletionListener(onCompletionListener);
    }

    //?????????
    public void setOnBufferingUpdateListener(IMediaPlayer.OnBufferingUpdateListener onBufferingUpdateListener){
        videoManager.setOnBufferingUpdateListener(onBufferingUpdateListener);
    }

    public void setOnControlMessageListener(IjkMediaPlayer.OnControlMessageListener onControlMessageListener){
        videoManager.setOnControlMessageListener(onControlMessageListener);
    }

    //????????????????????????
    public void setOnVideoSizeChangedListener(IMediaPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener){
        videoManager.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
    }

    public void setOnMediaCodecSelectListener(IjkMediaPlayer.OnMediaCodecSelectListener onMediaCodecSelectListener){
        videoManager.setOnMediaCodecSelectListener(onMediaCodecSelectListener);
    }

    public void setOnNativeInvokeListener(IjkMediaPlayer.OnNativeInvokeListener onNativeInvokeListener){
        videoManager.setOnNativeInvokeListener(onNativeInvokeListener);
    }

    //??????????????????????????????
    public void setOnErrorListener(IMediaPlayer.OnErrorListener onErrorListener){
        videoManager.setOnErrorListener(onErrorListener);
    }

    public void setOnSeekCompleteListener(IMediaPlayer.OnSeekCompleteListener onSeekCompleteListener){
        videoManager.setOnSeekCompleteListener(onSeekCompleteListener);
    }

    //????????????
    public void setOnInfoListener(IMediaPlayer.OnInfoListener onInfoListener){
        videoManager.setOnInfoListener(onInfoListener);
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener onPreparedListener){
        this.onPreparedListener = onPreparedListener;
    }

}
