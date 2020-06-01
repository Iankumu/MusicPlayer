package com.example.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.musicplayer.Services.NotificationActionService;

public class CreateNotification {
    public static final String CHANNEL_ID ="Channel";
    public static final String ACTION_PREVIOUS ="actionPrevious";
    public static final String ACTION_PLAY ="actionPlay";
    public static final String ACTION_NEXT ="actionNext";
    private static MediaSessionCompat mediaSessionCompat;


    public static Notification notification;
    //instance of the playerActivity class
    PlayerActivity play = new PlayerActivity();

    public static void  createNotification(Context context,MusicFiles musicFiles, int playbutton, int pos,int size){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
             mediaSessionCompat = new MediaSessionCompat(context,"tag");
//            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.ic_repeat_on);

            PendingIntent pendingIntentPrevious;
            int dew_previous;

            if(pos == 0){
                pendingIntentPrevious = null;
                dew_previous = 0;
            }else
            {
                Intent intentPrevious = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_PREVIOUS);
                pendingIntentPrevious = PendingIntent.getBroadcast(context,0,intentPrevious,PendingIntent.FLAG_UPDATE_CURRENT);
                dew_previous = R.drawable.ic_skip_previous;
            }

            Intent intentPlay = new Intent(context, NotificationActionService.class)
                    .setAction(ACTION_PLAY);
            PendingIntent pendingIntentPlay  = PendingIntent.getBroadcast(context,0,intentPlay,PendingIntent.FLAG_UPDATE_CURRENT);

            PendingIntent pendingIntentNext;
            int drw_Next;

            if(pos == size){
                pendingIntentNext = null;
                drw_Next = 0;
            }else
            {
                Intent intentNext = new Intent(context, NotificationActionService.class)
                        .setAction(ACTION_NEXT);
                pendingIntentNext = PendingIntent.getBroadcast(context,0,intentNext,PendingIntent.FLAG_UPDATE_CURRENT);
                drw_Next = R.drawable.ic_skip_next;
            }

            //creating a notification
            notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentText(musicFiles.getTitle())
//                    .setLargeIcon(icon)
                    .setOnlyAlertOnce(true)
                    .setShowWhen(false)
                    .addAction(dew_previous,"Previous",pendingIntentPrevious)
                    .addAction(playbutton,"Play",pendingIntentPlay)
                    .addAction(drw_Next,"Next",pendingIntentNext)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)
                            .setMediaSession(mediaSessionCompat
                                    .getSessionToken()))
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .build();

            notificationManagerCompat.notify(1,notification);
        }


    }



}
