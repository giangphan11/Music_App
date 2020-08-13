package phanbagiang.com.musicapp.notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import phanbagiang.com.musicapp.R;
import phanbagiang.com.musicapp.model.MusicFile;

public class CreateNotification {
    public static final String CHANNEL_ID="chanel1";
    public static final String ACTION_PREVIOUS="actionprevious";
    public static final String ACTION_NEXT="actionnext";
    public static final String ACTION_PLAY="actionplay";


    public static Notification notification;

    public static void CreateNotification(Context context, MusicFile musicFile, int playButton, int position, int size){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationManagerCompat notificationManagerCompat= NotificationManagerCompat.from(context);
            MediaSessionCompat mediaSessionCompat=new MediaSessionCompat(context,"tag");
            byte []art=getAlbumArt(musicFile.getPath());
            Bitmap bitmap;
            if(art!=null){
                 bitmap=BitmapFactory.decodeByteArray(art,0,art.length);
            }
            else{
                bitmap=BitmapFactory.decodeResource(context.getResources(), R.drawable.catw);
            }

            // previous
            Intent intentPrevious=new Intent(context,ActionService.class)
                    .setAction(ACTION_PREVIOUS);
            PendingIntent pendingIntentPrevious=PendingIntent.getBroadcast(context,0,
                    intentPrevious,PendingIntent.FLAG_UPDATE_CURRENT);
            int drw_pre=R.drawable.ic_skip_previous;


            // play
            Intent intentPlay=new Intent(context,ActionService.class)
                    .setAction(ACTION_PLAY);
             PendingIntent pendingIntentPlay=PendingIntent.getBroadcast(context,0,
                    intentPlay,PendingIntent.FLAG_UPDATE_CURRENT);

             // next
            // previous
            Intent intentNext=new Intent(context,ActionService.class)
                    .setAction(ACTION_NEXT);
            PendingIntent pendingIntentNext=PendingIntent.getBroadcast(context,0,
                    intentNext,PendingIntent.FLAG_UPDATE_CURRENT);
            int drw_next=R.drawable.ic_skip_next;

            // create notification
            notification=new NotificationCompat.Builder(context,CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_music_note)
                    .setContentTitle(musicFile.getTitle())
                    .setContentText(musicFile.getArtist())
                    .setLargeIcon(bitmap)
                    .addAction(drw_next,"Next",pendingIntentNext)
                    .addAction(drw_pre,"Previous",pendingIntentPrevious)
                    .addAction(playButton,"Play",pendingIntentPlay)
                    .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                            .setShowActionsInCompactView(0,1,2)
                            .setMediaSession(mediaSessionCompat.getSessionToken()))
                    .setOnlyAlertOnce(true) // show notification only first time
                    .setShowWhen(false)
                    .setPriority(NotificationManagerCompat.IMPORTANCE_HIGH)
                    .build();

            notificationManagerCompat.notify(1,notification);

        }
    }

    public static byte [] getAlbumArt(String path){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(path);
        byte []art=retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
