package phanbagiang.com.musicapp.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.palette.graphics.Palette;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import phanbagiang.com.musicapp.notification.CreateNotification.*;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import phanbagiang.com.musicapp.R;
import phanbagiang.com.musicapp.model.MusicFile;
import phanbagiang.com.musicapp.notification.CreateNotification;

import static phanbagiang.com.musicapp.activities.MainActivity.musicFiles;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    private static final String TAG = "PlayerActivity";

    ImageView btnBack, btnMenu, btn_song_suffer,btn_song_previous,btn_song_next, btn_song_repeat,imageAlbum ;
    TextView song_name, song_artist, txt_song_duration_end,txt_song_duration_start;
    SeekBar seekBar;
    FloatingActionButton btnPlayPause;

    NotificationManager notificationManager;

    static ArrayList<MusicFile>listOfMusic=new ArrayList<>();
    private Intent intent;
    private int position=-1;

    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler=new Handler();

    //
    private Thread playThread, nextThread, preThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        addControls();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChanel();
        }
        addEvents();
    }

    private void createChanel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel= new NotificationChannel(CreateNotification.CHANNEL_ID
            ,"Phan", NotificationManager.IMPORTANCE_HIGH);

            notificationManager=getSystemService(NotificationManager.class);
            if(notificationManager!=null){
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void addControls(){
        listOfMusic=musicFiles;
        intent=getIntent();
        position=intent.getIntExtra("position",-1);
        btnBack=findViewById(R.id.btn_back);
        btnMenu=findViewById(R.id.btn_menu);
        btn_song_suffer=findViewById(R.id.song_suffer);
        btn_song_next=findViewById(R.id.song_next);
        btn_song_repeat=findViewById(R.id.song_repeat);
        btn_song_previous=findViewById(R.id.song_previous);
        song_name=findViewById(R.id.song_name);
        song_artist=findViewById(R.id.song_artist);
        imageAlbum=findViewById(R.id.imageAlbum);
        seekBar=findViewById(R.id.seek_bar);

        btnPlayPause=findViewById(R.id.play_pause);
        txt_song_duration_end=findViewById(R.id.song_duration_end);
        txt_song_duration_start=findViewById(R.id.song_duration_start);

        if(listOfMusic!=null){
            song_name.setText(listOfMusic.get(position).getTitle());
            song_artist.setText(listOfMusic.get(position).getArtist());
            btnPlayPause.setImageResource(R.drawable.ic_pause);
            uri=Uri.parse(listOfMusic.get(position).getPath());
        }
        if(mediaPlayer!=null){ // nếu đang chạy thì dừng và tạo bài mưới
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else{ // nếu chưa chọn bài thì khởi tạo và chạy nhạc
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        setMetaData(uri);
    }

    @Override
    protected void onResume() {
        playThreadBtn();
        preThreadBtn();
        nextThreadBtn();
        super.onResume();
    }
    private void playThreadBtn(){
        playThread=new Thread(){
            @Override
            public void run() {
                super.run();
                btnPlayPause.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }
    private void playPauseBtnClicked(){
        if(mediaPlayer.isPlaying()){
            btnPlayPause.setImageResource(R.drawable.ic_play);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int current_position=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else{
            btnPlayPause.setImageResource(R.drawable.ic_pause);

            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int current_position=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }

    }
    private void preThreadBtn(){
        preThread=new Thread(){
            @Override
            public void run() {
                super.run();
                btn_song_previous.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        preBtnClicked();
                    }
                });
            }
        };
        preThread.start();
    }
    private void preBtnClicked(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            position=((position-1)<0 ?(listOfMusic.size()-1) :(position-1)); // if 3 ngôi
             // nếu biểu thức đúng thì vế thứ nhất, sai thì vế thứ 2
            //Log.e(TAG, "position: "+position );
            CreateNotification.CreateNotification(getApplicationContext(),listOfMusic.get(position),R.drawable.ic_play,position,listOfMusic.size()-1);

            uri= Uri.parse(listOfMusic.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            setMetaData(uri);
            song_artist.setText(listOfMusic.get(position).getArtist());
            song_name.setText(listOfMusic.get(position).getTitle());

            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int current_position=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else{
            mediaPlayer.stop();
            mediaPlayer.release();
            position=((position-1)<0 ?(listOfMusic.size()-1) :(position-1)); // if 3 ngôi
            // nếu biểu thức đúng thì vế thứ nhất, sai thì vế thứ 2
            uri= Uri.parse(listOfMusic.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            setMetaData(uri);
            song_artist.setText(listOfMusic.get(position).getArtist());
            song_name.setText(listOfMusic.get(position).getTitle());

            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int current_position=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);

                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);
        }
    }
    private void nextThreadBtn(){
        nextThread=new Thread(){
            @Override
            public void run() {
                super.run();
                btn_song_next.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }
    private void nextBtnClicked(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            position=((position+1)%listOfMusic.size());

            //Log.e(TAG, "position: "+position );
            CreateNotification.CreateNotification(getApplicationContext(),listOfMusic.get(position),R.drawable.ic_play,position,listOfMusic.size()-1);


            uri= Uri.parse(listOfMusic.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            setMetaData(uri);
            song_artist.setText(listOfMusic.get(position).getArtist());
            song_name.setText(listOfMusic.get(position).getTitle());

            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int current_position=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this); // next xong bai chay
            btnPlayPause.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();
        }
        else{
            mediaPlayer.stop();
            mediaPlayer.release();
            position=((position+1)%listOfMusic.size());
            uri= Uri.parse(listOfMusic.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            setMetaData(uri);
            song_artist.setText(listOfMusic.get(position).getArtist());
            song_name.setText(listOfMusic.get(position).getTitle());

            seekBar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int current_position=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(current_position);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            btnPlayPause.setBackgroundResource(R.drawable.ic_play);
        }
    }
    private void addEvents(){
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mediaPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress*1000);
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
                if(mediaPlayer!=null){
                    int current_position=mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(current_position);
                    txt_song_duration_start.setText(formattedTime(current_position));
                }
                handler.postDelayed(this,1000);
            }
        });
    }
    private String formattedTime(int currentTime){
        String totalOut="";
        String totalNew="";
        String seconds=String.valueOf(currentTime%60);
        String minutes=String.valueOf(currentTime/60);
        totalOut=minutes+":"+seconds;
        totalNew=minutes+":"+"0"+seconds;
        if(seconds.length()==1){
            return totalNew;
        }
        else{
            return totalOut;
        }
    }
    private void setMetaData(Uri uri){
        MediaMetadataRetriever mediaMetadataRetriever=new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(uri.toString());
        int durationTotal=Integer.parseInt(listOfMusic.get(position).getDuration());
        txt_song_duration_end.setText(formattedTime(durationTotal/1000));
        byte []art=mediaMetadataRetriever.getEmbeddedPicture();

        Bitmap bitmap;
        if(art!=null){
            bitmap= BitmapFactory.decodeByteArray(art,0,art.length);
            ImageAnimation(this,imageAlbum,bitmap);

            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch=palette.getDominantSwatch();
                    if(swatch!=null){
                        ImageView gradient=findViewById(R.id.imageViewGradient);
                        RelativeLayout relativeLayout=findViewById(R.id.main_layout);
                        gradient.setBackgroundResource(R.drawable.header_bg);
                        relativeLayout.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{swatch.getRgb(),0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawable_main=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{swatch.getRgb(),swatch.getRgb()});
                        relativeLayout.setBackground(gradientDrawable_main);
                        song_name.setTextColor(swatch.getTitleTextColor());
                        song_artist.setTextColor(swatch.getBodyTextColor());
                    }
                    else{
                        ImageView gradient=findViewById(R.id.imageViewGradient);
                        RelativeLayout relativeLayout=findViewById(R.id.main_layout);
                        gradient.setBackgroundResource(R.drawable.header_bg);
                        relativeLayout.setBackgroundResource(R.drawable.main_bg);

                        GradientDrawable gradientDrawable=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{0xff000000,0x00000000});
                        gradient.setBackground(gradientDrawable);

                        GradientDrawable gradientDrawable_main=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                                new int[]{0xff000000,0xff000000});
                        relativeLayout.setBackground(gradientDrawable_main);
                        song_name.setTextColor(Color.WHITE);
                        song_artist.setTextColor(Color.DKGRAY);
                    }
                }

            });
        }
        else{
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.catw)
                    .into(imageAlbum);
            ImageView gradient=findViewById(R.id.imageViewGradient);
            RelativeLayout relativeLayout=findViewById(R.id.main_layout);
            gradient.setBackgroundResource(R.drawable.header_bg);
            relativeLayout.setBackgroundResource(R.drawable.main_bg);
            song_name.setTextColor(Color.WHITE);
            song_artist.setTextColor(Color.DKGRAY);
        }
    }

    BroadcastReceiver broadcastReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getExtras().getString("actionname");
            switch (action){
                case CreateNotification.ACTION_PLAY:
                    playPauseBtnClicked();
                    break;
                case CreateNotification.ACTION_PREVIOUS:
                    preBtnClicked();
                    break;
                case CreateNotification.ACTION_NEXT:
                    nextBtnClicked();
                    break;
            }
        }
    };

    private void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        Animation animOut= AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
        final Animation animIn= AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if(mediaPlayer!=null){
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
        }
    }
}