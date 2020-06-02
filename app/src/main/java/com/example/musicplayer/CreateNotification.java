package com.example.musicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat  ;

import com.example.musicplayer.Services.NotificationActionService;

public class CreateNotification {
    public static final String CHANNEL_ID ="Channel";
    public static final String ACTION_PREVIOUS ="actionPrevious";
    public static final String ACTION_PLAY ="actionPlay";
    public static final String ACTION_NEXT ="actionNext";
    private static final int NOTIFICATION_ID = 101;


    public static Notification notification;




    public static void  createNotification(Context context,MusicFiles musicFiles, int playbutton, int pos,int size){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat = new MediaSessionCompat(context, "tag");
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra(" com.example.musicplayer.notifyId", NOTIFICATION_ID);

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
            Bitmap icon = BitmapFactory.decodeResource(context.getResources(),R.drawable.headphone);

            //creating a notification
            notification = new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentText(musicFiles.getTitle())
                    .setLargeIcon(icon)
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

            notificationManagerCompat.notify(NOTIFICATION_ID,notification);
        }




    }



}
