package com.herudi.exovideo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.google.android.exoplayer2.util.Util;
import android.view.View;

import android.net.Uri;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.MimeTypes;
import com.facebook.react.bridge.Callback;

/**
 * Created by herudi-sahimar on 24/04/2017.
	http://blogs.quovantis.com/exoplayer-events-and-ui-customizations/
	http://blogs.quovantis.com/getting-started-with-exoplayer/

	https://github.com/google/ExoPlayer/issues/3472
	https://medium.com/@takusemba/understands-callbacks-of-exoplayer-c05ac3c322c2
	https://stackoverflow.com/a/42996915/3076704
	http://steveliles.github.io/returning_a_result_from_an_android_activity.html
	https://medium.com/google-exoplayer/exoplayer-2-x-improved-demo-app-d97171aaaaa1 <-- A good example for all the type of play
	https://medium.com/fungjai/playing-video-by-exoplayer-b97903be0b33 <-- One of the best explanation
	https://github.com/sindresorhus/file-type/tree/master/fixture  <-- Many sample files present here for tesing the player
 */

public class PlayerActivity extends Activity {
    private static final String TAG = "PlayerActivity";
    private static final String KEY_PLAY_WHEN_READY = "play_when_ready";
    private static final String KEY_WINDOW = "window";
    private static final String KEY_POSITION = "position";
    private String TAG_NAME = "rnexoplayer";
    private Uri videoUri;
    private Uri subtitleUri=null;
    private BandwidthMeter bandwidthMeter;
    private ExtractorsFactory extractorsFactory;
    private TrackSelection.Factory trackSelectionFactory;
    private DataSource.Factory dataSourceFactory;
    private TrackSelector trackSelector;
    private LoadControl loadControl;
    private SimpleExoPlayer player;
    private VidPlayerView playerView;
		private boolean playWhenReady;
    private int currentWindow;
    private long playbackPosition;
		private RenderersFactory renderersFactory;
		private RegisterCallBack rgstrCallBack = new RegisterCallBack();
		private String vidUrl;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
				
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.loadControl = new DefaultLoadControl();
        this.extractorsFactory = new DefaultExtractorsFactory();
        this.trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        this.dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this.getApplicationContext(), TAG_NAME), (TransferListener<? super DataSource>) bandwidthMeter);
        this.trackSelector = new DefaultTrackSelector(trackSelectionFactory);
				this.renderersFactory = new DefaultRenderersFactory(this);
        this.player = ExoPlayerFactory.newSimpleInstance(renderersFactory, this.trackSelector, this.loadControl);
        this.playerView = new VidPlayerView(this,this.player);
				this.playWhenReady = true;
				this.currentWindow = 0;
				this.playbackPosition = 0;
				
				this.vidUrl = getIntent().getStringExtra("url");
        setVidParams(
                this.vidUrl,
                getIntent().getStringExtra("title"),
                getIntent().getStringExtra("subtitle")
        );
				
				if (savedInstanceState != null) {
						setStartPosition(savedInstanceState);
        }
				
        setContentView(getPlayerView());
        getWindow().getDecorView().getRootView().setKeepScreenOn(true); //Added by Chandrajyoti, To keep the screen ON, when Exo Player is in Focus.
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
		
		// onCreate() only at creation only
		// onCreate() -> onStart() -> onResume() When activity starts.
		// So as onResume() was the last call while starting the activity, onPause() callback always follows onResume().
		// onPause() called when activity loses focus and enters a Paused state(example, the user taps the Back or Recents button)
		// Once onPause() finishes executing, the next callback is either onStop() or onResume()(via onStart()), depending on what happens after the activity enters the Paused state.
		// onPause() -> onStop() : The system calls onStop() when the activity is no longer visible to the user. 
		// onPause() -> onStart() -> onResume() : This happens when activity comes back from background to foreground
		// onStop() -> onRestart(). onRestart() always called followed by onStop().
		// onDestroy() called before an activity is destroyed.
		
		// For back button press behavior is onPause()-> onStop() -> onDestroy()
		// For home button press or jumping to other app using recent app list behavior is onPause() -> onStop()

    @Override
    protected void onStart() {  //Called when player intent started
        super.onStart();
        Log.v(TAG,"ClenetExo onStart()...");
				if (Util.SDK_INT > 23) {
					initializePlayer();
				}
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG,"ClenetExo onResume()...");
				if ((Util.SDK_INT <= 23 || getPlayer() == null)) {
					initializePlayer();
				}
				else if(getPlayer() != null){
					startPlayer();
				}
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG,"ClenetExo onPause()...");
				pausePlayer();
    }
		
    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG,"ClenetExo onStop()...");
				pausePlayer();
    }
		
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"ClenetExo onDestroy()...");
        releasePlayer();
    }
		
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }
		
		
    public VidPlayerView getPlayerView() {
        return playerView;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }
		
		public void initializePlayer() {
				// this.rgstrCallBack.invokeCallBack("Hi Margaya Hi");
				this.player = ExoPlayerFactory.newSimpleInstance(this.renderersFactory, this.trackSelector, this.loadControl);
				this.playerView.setPlayer(this.player);
				this.playerView.rqustFocus();
				
				boolean previousPosition = this.currentWindow != C.INDEX_UNSET;
				
				if (previousPosition) {
						this.player.seekTo(this.currentWindow, this.playbackPosition);
				}
				
				if (this.getSubtitleUri()!=null){
						this.player.prepare(this.getMergingMediaSource(),!previousPosition,false);
				}
				else{
						this.player.prepare(this.getMediaSource(),!previousPosition,false);
				}
				
        this.player.addListener(new PlayerEventListener());
				this.player.setPlayWhenReady(this.playWhenReady);
		}

    private void setVidParams(String uri,String title, String sub) {
        if (title!=null){
            this.playerView.getTextView().setText(title);
        }
        this.setVideoUri(Uri.parse(uri));
				
        if (sub!=null){
            this.setSubtitleUri(Uri.parse(sub));
        }
    }
		
		private void startPlayer(){
			if(this.player!=null){
				this.player.setPlayWhenReady(this.playWhenReady);
			}
		}
		
		private void pausePlayer(){
			if(this.player!=null){
				saveStartPosition();
				this.player.setPlayWhenReady(false);
			}
		}
		
		private void releasePlayer(){
			 if(this.player!=null){
				 saveStartPosition();
				 player.release();
				 player = null;
			 }
		}
		
    private Uri getVideoUri() {
        return videoUri;
    }

    private void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    private Uri getSubtitleUri() {
        return subtitleUri;
    }

    private void setSubtitleUri(Uri subtitleUri) {
        this.subtitleUri = subtitleUri;
    }

    private ExtractorsFactory getExtractorsFactory() {
        return extractorsFactory;
    }

    private DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    private MediaSource getMediaSource(){
        return new ExtractorMediaSource.Factory(this.getDataSourceFactory()).createMediaSource(this.getVideoUri());
    }

    private MediaSource getMediaSourceSubtitle(){
        return new SingleSampleMediaSource.Factory(this.getDataSourceFactory()).createMediaSource(this.getSubtitleUri(), this.getFormat(), C.TIME_UNSET);
    }

    private Format getFormat(){
        return Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,1, "en");
    }

    private MergingMediaSource getMergingMediaSource(){
        return new MergingMediaSource(this.getMediaSource(), this.getMediaSourceSubtitle());
    }
	
		private void saveInstanceState(Bundle outState) {
				outState.putBoolean(KEY_PLAY_WHEN_READY, this.playWhenReady);
        outState.putInt(KEY_WINDOW, this.currentWindow);
        outState.putLong(KEY_POSITION, this.playbackPosition);
    }
		
		private void saveStartPosition() {
        this.playbackPosition = this.player.isCurrentWindowSeekable() ? Math.max(0, this.player.getCurrentPosition()): C.TIME_UNSET;
        this.currentWindow = this.player.getCurrentWindowIndex();
        this.playWhenReady = this.player.getPlayWhenReady();
    }
		
    private void setStartPosition(Bundle savedInstanceState) {
        this.playbackPosition = savedInstanceState.getLong(KEY_POSITION);
        this.currentWindow = savedInstanceState.getInt(KEY_WINDOW);
        this.playWhenReady = savedInstanceState.getBoolean(KEY_PLAY_WHEN_READY);
    }
		
		private class PlayerEventListener extends Player.DefaultEventListener{
			@Override
			public void onLoadingChanged(boolean isLoading) {

			}
			@Override
			public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

			}
			@Override
			public void onPlayerError(ExoPlaybackException error) {
        switch (error.type) {
            case ExoPlaybackException.TYPE_SOURCE:
                // Log.e(TAG, "TYPE_SOURCE: " + error.getSourceException().getMessage());
								rgstrCallBack.invokeCallBack("TYPE_SOURCE: " + error.getSourceException().getMessage(),vidUrl);
								finish();
                break;

            case ExoPlaybackException.TYPE_RENDERER:
                // Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
								rgstrCallBack.invokeCallBack("TYPE_RENDERER: " + error.getRendererException().getMessage(),vidUrl);
								finish();
                break;

            case ExoPlaybackException.TYPE_UNEXPECTED:
                // Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
								rgstrCallBack.invokeCallBack("TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage(),vidUrl);
								finish();
                break;
				}
			}
			@Override
			public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
					switch (playbackState){
						case Player.STATE_IDLE:       // The player does not have any media to play yet.
								playerView.getProgressBar().setVisibility(View.VISIBLE);
								break;
						case Player.STATE_BUFFERING:  // The player is buffering (loading the content)
								playerView.getProgressBar().setVisibility(View.VISIBLE);
								break;
						case Player.STATE_READY:      // The player is able to immediately play
								playerView.getProgressBar().setVisibility(View.GONE);
								break;
						case Player.STATE_ENDED:      // The player has finished playing the media
								playerView.getProgressBar().setVisibility(View.GONE);
								break;
					}
			}
			@Override
			public void onPositionDiscontinuity(int reason) {

			}
			@Override
			public void onRepeatModeChanged(int repeatMode) {

			}
			@Override
			public void onSeekProcessed(){
				
			}
			@Override
			public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled){
				
			}
			@Override
			public void onTimelineChanged(Timeline timeline, Object manifest,int reason) {

			}
			@Override
			public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

			}
	  }
}


