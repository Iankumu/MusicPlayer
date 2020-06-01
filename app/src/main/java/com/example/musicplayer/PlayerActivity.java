package com.example.musicplayer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.musicplayer.Services.OnClearFromRecentService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import static com.example.musicplayer.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity implements Playable {

    TextView song_name,artist_name,durationPlayed,durationTotal;
    ImageView cover_art,nextBtn,PrevBtn,backBtn,repeatBtn;
    FloatingActionButton play_pauseBtn;
    SeekBar seekBar;
    int position = -1;
    static ArrayList<MusicFiles> listofsongs= new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread,prevThrean,NextThread;
    NotificationManager notificationManager;

    private boolean ongoingCall = false;
    public static PhoneStateListener phoneStateListener;
    public static TelephonyManager telephonyManager;

    private int resumePosition;
    int positions = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();
        backBtnClicked();
        repeatBtnClicked();
        callStateListener();
        registerBecomingNoisyReceiver();
        song_name.setText(listofsongs.get(position).getTitle());
        artist_name.setText(listofsongs.get(position).getArtist());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer !=null && fromUser){
                    mediaPlayer.seekTo(progress * 1000);
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer != null){
                    int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedText(mCurrentPosition));
                }
                handler.postDelayed(this,1000);
            }
        });

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChannel();
            registerReceiver(broadcastReceiver,new IntentFilter("TRACKS_TRACKS"));
            startService(new Intent(getBaseContext(), OnClearFromRecentService.class));
        }
        CreateNotification.createNotification(PlayerActivity.this,musicFiles.get(position),R.drawable.ic_pause,1, musicFiles.size()-1);

        Intent intent = new Intent( getApplicationContext(), CreateNotification.class );
        intent.setAction( CreateNotification.ACTION_PLAY );
        startService( intent );

        NotificationManager notificationmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    }



    private void repeatBtnClicked() {
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(true) {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_one);
                    mediaPlayer.setLooping(true);
                    play_pauseBtn.setImageResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
                else
                {
                    repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                    mediaPlayer.setLooping(false);
                    play_pauseBtn.setImageResource(R.drawable.ic_pause);
                    mediaPlayer.start();
                }
            }

        });
    }

    private void backBtnClicked() {
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                mediaPlayer.stop();
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        playThreadBtn();
        nextThreadBtn();
        previousThreadBtn();
        super.onResume();
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                play_pauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
                super.run();
            }
        };
        playThread.start();
    }

    private void playPauseBtnClicked() {
        if(mediaPlayer.isPlaying()){
            //pause the media
            play_pauseBtn.setImageResource(R.drawable.ic_play_arrow);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else
        {
            //play the media
             play_pauseBtn.setImageResource(R.drawable.ic_pause);
             mediaPlayer.start();
             seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
    }

    private void nextThreadBtn() {
        NextThread = new Thread(){
            @Override
            public void run() {
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
                super.run();
            }
        };
        NextThread.start();
    }

    private void nextBtnClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1)% listofsongs.size());
            uri = Uri.parse(listofsongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listofsongs.get(position).getTitle());
            artist_name.setText(listofsongs.get(position).getArtist());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            play_pauseBtn.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1)% listofsongs.size());
            uri = Uri.parse(listofsongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listofsongs.get(position).getTitle());
            artist_name.setText(listofsongs.get(position).getArtist());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            play_pauseBtn.setImageResource(R.drawable.ic_play_arrow);

        }
    }

    private void previousThreadBtn() {
        prevThrean = new Thread(){
            @Override
            public void run() {
                PrevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
                super.run();
            }
        };
        prevThrean.start();
    }

    private void prevBtnClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position - 1) <0 ? (listofsongs.size() -1) :(position-1));
            uri = Uri.parse(listofsongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listofsongs.get(position).getTitle());
            artist_name.setText(listofsongs.get(position).getArtist());
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            play_pauseBtn.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position - 1) <0 ? (listofsongs.size() -1) :(position-1));
            uri = Uri.parse(listofsongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            song_name.setText(listofsongs.get(position).getTitle());
            artist_name.setText(listofsongs.get(position).getArtist());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer != null){
                        int mCurrentPosition =mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            play_pauseBtn.setImageResource(R.drawable.ic_play_arrow);

        }
    }


    private String  formattedText(int mCurrentPosition) {
        String totalout ="";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition %60);
        String minutes = String.valueOf(mCurrentPosition/60);
        totalout = minutes +":"+ seconds;
        totalNew = minutes +":"+"0"+seconds;
        if(seconds.length()==1){
            return totalNew;
        }
        else
        {
            return totalout;
        }

    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position",-1);
        listofsongs = musicFiles;
        if(listofsongs != null){
            play_pauseBtn.setImageResource(R.drawable.ic_pause);
            uri = Uri.parse(listofsongs.get(position).getPath());
        }
         if(mediaPlayer != null){
             mediaPlayer.stop();
             mediaPlayer.release();
             mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
             mediaPlayer.start();
         }
         else {
             mediaPlayer =MediaPlayer.create(getApplicationContext(),uri);
             mediaPlayer.start();
         }
         seekBar.setMax(mediaPlayer.getDuration() / 1000);
         metaData(uri);
    }

    //connect the views with the member variables
    private void initViews() {
        song_name = findViewById(R.id.songname);
        artist_name = findViewById(R.id.artist_name);
        durationPlayed = findViewById(R.id.durationPlayed);
        durationTotal = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.next);
        PrevBtn = findViewById(R.id.prev);
        backBtn = findViewById(R.id.back_btn);
        seekBar = findViewById(R.id.seekbar);
        play_pauseBtn = findViewById(R.id.play_pause);
        repeatBtn = findViewById(R.id.repeat);
    }

    private void metaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int duration_Total = Integer.parseInt(listofsongs.get(position).getDuration())/1000;
        durationTotal.setText(formattedText(duration_Total));
        byte[] art = retriever.getEmbeddedPicture();
        if(art!= null){
            Glide.with(this)
                    .asBitmap()
                    .load(art)
                    .into(cover_art);
        }
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.music)
                    .into(cover_art);
        }
    }

    //Handle incoming phone calls
    private void callStateListener() {
        // Get the telephony manager
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        //Starting listening for PhoneState changes
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    //if at least one call exists or the phone is ringing
                    //pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle. Start playing.
                        if (mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };
        // Register the listener with the telephony manager
        // Listen for changes to the device call state.
        telephonyManager.listen(phoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void createChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(CreateNotification.CHANNEL_ID,"Music Player", NotificationManager.IMPORTANCE_LOW);

            notificationManager = getSystemService(NotificationManager.class);
            if(notificationManager != null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia();
            play_pauseBtn.setImageResource(R.drawable.ic_play_arrow);
//            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private void registerBecomingNoisyReceiver() {
        //register after getting audio focus
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReceiver, intentFilter);

    }

    BroadcastReceiver   broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getExtras().getString("actionname");

            switch (action){
                case CreateNotification.ACTION_PREVIOUS:
                    onTrackPrevious();
                    break;
                case CreateNotification.ACTION_PLAY:
                    if(mediaPlayer.isPlaying()){
                        onTrackPause();
                    }else
                    {
                        onTrackPlay();
                    }
                    break;
                case CreateNotification.ACTION_NEXT:
                        onTrackNext();
                        break;
                default:
                    break;
            }
        }
    };
    @Override
    public void onTrackPrevious() {
        positions = ((position - 1) <0 ? (listofsongs.size() -1) :(position-1));
        CreateNotification.createNotification(PlayerActivity.this,musicFiles.get(positions),R.drawable.ic_pause,positions,musicFiles.size()-1);
        skipToPrevious();


    }

    @Override
    public void onTrackPlay() {

        CreateNotification.createNotification(PlayerActivity.this,musicFiles.get(position),R.drawable.ic_pause,position,musicFiles.size()-1);
           play_pauseBtn.setImageResource(R.drawable.ic_pause);
           resumeMedia();

    }

    @Override
    public void onTrackPause() {
        CreateNotification.createNotification(PlayerActivity.this, musicFiles.get(position), R.drawable.ic_play_arrow, position, musicFiles.size() - 1);
        play_pauseBtn.setImageResource(R.drawable.ic_play_arrow);
        pauseMedia();

    }

    @Override
    public void onTrackNext() {
        positions = ((position + 1)% listofsongs.size());
        CreateNotification.createNotification(PlayerActivity.this,musicFiles.get(positions),R.drawable.ic_pause,positions,musicFiles.size()-1);
        skipToNext();

    }


    //pause media if playing
    private void pauseMedia() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            resumePosition = mediaPlayer.getCurrentPosition();
        }
    }
    //resume media if paused
    private void resumeMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.seekTo(resumePosition);
            mediaPlayer.start();
        }
    }
    //play media if not playing
    private void playMedia() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void skipToNext() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position + 1) % listofsongs.size());
            uri = Uri.parse(listofsongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listofsongs.get(position).getTitle());
            artist_name.setText(listofsongs.get(position).getArtist());
            playMedia();
        }
    }
    private void skipToPrevious() {

        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position - 1) < 0 ? (listofsongs.size() - 1) : (position - 1));
            uri = Uri.parse(listofsongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            metaData(uri);
            song_name.setText(listofsongs.get(position).getTitle());
            artist_name.setText(listofsongs.get(position).getArtist());
            playMedia();
        }
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            notificationManager.cancelAll();
        }
        unregisterReceiver(broadcastReceiver);
    }
}
