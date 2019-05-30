package com.herudi.exovideo;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;

/**
 * Created by herudi-sahimar on 26/04/2017.
 */
public class VidPlayerView extends RelativeLayout {

    private PlayerView simpleExoPlayerView;
    private ProgressBar progressBar;
    private TextView textView;

    public VidPlayerView(Context context, SimpleExoPlayer player) {
        super(context);
        simpleExoPlayerView = new PlayerView(context);
        progressBar = new ProgressBar(context);
        textView = new TextView(context);
        textView.setX(40);
        textView.setY(20);
        textView.setTextColor(Color.parseColor("#FFFFFF"));
        textView.setTextSize(16);
        textView.setText("");
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100,100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        simpleExoPlayerView.setLayoutParams(new PlayerView.LayoutParams(
                PlayerView.LayoutParams.MATCH_PARENT,
                PlayerView.LayoutParams.MATCH_PARENT
        ));
        setLayoutParams(new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        ));
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
        addView(simpleExoPlayerView);
        addView(textView);
        addView(progressBar,params);
        simpleExoPlayerView.setUseController(true);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setPlayer(player);
        simpleExoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility==0){
                    textView.setVisibility(VISIBLE);
                }else {
                    textView.setVisibility(GONE);
                }
            }
        });
    }

    public VidPlayerView(Context context) {
        super(context);
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public TextView getTextView() {
        return textView;
    }
		public void rqustFocus() {
        simpleExoPlayerView.requestFocus();
    }
		public void setPlayer(SimpleExoPlayer player) {
        simpleExoPlayerView.setPlayer(player);
    }
}
