package com.herudi.exovideo;

import android.content.Context;
import android.net.Uri;
import android.view.View;
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
import com.google.android.exoplayer2.util.Util;
import android.os.Bundle;
import com.facebook.react.bridge.Callback;
/**
 * Created by herudi-sahimar on 26/04/2017.
 */
public class PlayerController {
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

    public PlayerController(Context context) {
        this.bandwidthMeter = new DefaultBandwidthMeter();
        this.loadControl = new DefaultLoadControl();
        this.extractorsFactory = new DefaultExtractorsFactory();
        this.trackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        this.dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context.getApplicationContext(), TAG_NAME), (TransferListener<? super DataSource>) bandwidthMeter);
        this.trackSelector = new DefaultTrackSelector(trackSelectionFactory);
				this.renderersFactory = new DefaultRenderersFactory(context);
        this.player = ExoPlayerFactory.newSimpleInstance(renderersFactory, this.trackSelector, this.loadControl);
        this.playerView = new VidPlayerView(context,this.player);
				this.playWhenReady = true;
				this.currentWindow = 0;
				this.playbackPosition = 0;
    }

    public VidPlayerView getPlayerView() {
        return playerView;
    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }
		
		public void initializePlayer() {
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

    public void setVidParams(String uri,String title, String sub) {
        if (title!=null){
            this.playerView.getTextView().setText(title);
        }
        this.setVideoUri(Uri.parse(uri));
				
        if (sub!=null){
            this.setSubtitleUri(Uri.parse(sub));
        }
    }
		
		public void startPlayer(){
			if(this.player!=null){
				this.player.setPlayWhenReady(this.playWhenReady);
			}
		}
		
		public void pausePlayer(){
			if(this.player!=null){
				saveStartPosition();
				this.player.setPlayWhenReady(false);
			}
		}
		
		public void releasePlayer(){
			 if(this.player!=null){
				 saveStartPosition();
				 player.release();
				 player = null;
			 }
		}
		
    public Uri getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    public Uri getSubtitleUri() {
        return subtitleUri;
    }

    public void setSubtitleUri(Uri subtitleUri) {
        this.subtitleUri = subtitleUri;
    }

    public ExtractorsFactory getExtractorsFactory() {
        return extractorsFactory;
    }

    public DataSource.Factory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public MediaSource getMediaSource(){
        return new ExtractorMediaSource.Factory(this.getDataSourceFactory()).createMediaSource(this.getVideoUri());
    }

    public MediaSource getMediaSourceSubtitle(){
        return new SingleSampleMediaSource.Factory(this.getDataSourceFactory()).createMediaSource(this.getSubtitleUri(), this.getFormat(), C.TIME_UNSET);
    }

    public Format getFormat(){
        return Format.createTextSampleFormat(null, MimeTypes.APPLICATION_SUBRIP,1, "en");
    }

    public MergingMediaSource getMergingMediaSource(){
        return new MergingMediaSource(this.getMediaSource(), this.getMediaSourceSubtitle());
    }
	
		public void saveInstanceState(Bundle outState) {
				outState.putBoolean(KEY_PLAY_WHEN_READY, this.playWhenReady);
        outState.putInt(KEY_WINDOW, this.currentWindow);
        outState.putLong(KEY_POSITION, this.playbackPosition);
    }
		
		public void saveStartPosition() {
        this.playbackPosition = this.player.isCurrentWindowSeekable() ? Math.max(0, this.player.getCurrentPosition()): C.TIME_UNSET;
        this.currentWindow = this.player.getCurrentWindowIndex();
        this.playWhenReady = this.player.getPlayWhenReady();
    }
		
    public void setStartPosition(Bundle savedInstanceState) {
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
				rgstrCallBack.invokeCallBack("TYPE_SOURCE: " , error.getSourceException().getMessage());
                break;

            case ExoPlaybackException.TYPE_RENDERER:
                // Log.e(TAG, "TYPE_RENDERER: " + error.getRendererException().getMessage());
				rgstrCallBack.invokeCallBack("TYPE_RENDERER: " , error.getRendererException().getMessage());
                break;

            case ExoPlaybackException.TYPE_UNEXPECTED:
                // Log.e(TAG, "TYPE_UNEXPECTED: " + error.getUnexpectedException().getMessage());
				rgstrCallBack.invokeCallBack("TYPE_UNEXPECTED: " , error.getUnexpectedException().getMessage());
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
