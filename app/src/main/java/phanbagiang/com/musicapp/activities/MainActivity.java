package phanbagiang.com.musicapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import phanbagiang.com.musicapp.R;
import phanbagiang.com.musicapp.fragment.AlbumFragment;
import phanbagiang.com.musicapp.fragment.MusicFragment;
import phanbagiang.com.musicapp.model.MusicFile;

public class MainActivity extends AppCompatActivity {
    ViewPager viewPager;
    TabLayout tabLayout;
    private static final String FILE_TYPE_NO_MEDIA = ".nomedia";
    private int REQUEST_CODE=1;
    public static ArrayList<MusicFile> musicFiles;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window=getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        permissions();
    }
    private void permissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                musicFiles=getAllAudio(this);
                initViewPager();
            } else {

                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            musicFiles=getAllAudio(this);
            initViewPager();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showMessage("Permission is granted");
            //Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            musicFiles=getAllAudio(this);
            initViewPager();
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void showMessage(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void initViewPager(){
        viewPager=findViewById(R.id.view_pager);
        tabLayout=findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter=new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new MusicFragment(),"Musics");
        viewPagerAdapter.addFragment(new AlbumFragment(),"Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager,true);
    }
    public static class ViewPagerAdapter extends FragmentPagerAdapter{
        List<Fragment>fragments;
        List<String>titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            fragments=new ArrayList<>();
            titles=new ArrayList<>();
        }

        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    public static ArrayList<MusicFile>getAllAudio(Context context){
        ArrayList<MusicFile>tempMusic=new ArrayList<>();
        Uri uri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String []projection={
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.DATA,// path
        };
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";// chọn mỗi file .mp3
        String selection2 = MediaStore.Audio.Media.DATA + " like ? "; // chọn đường dẫn đến thư mục
        String sortOrder=MediaStore.Audio.Media.DISPLAY_NAME+" ASC"; // sắp xép theo tên abc tăng
       Cursor cursor=context.getContentResolver().query(uri,projection,selection,null,sortOrder);
        //Cursor cursor=context.getContentResolver().query(uri,projection,selection2,new String[]{"%NCT%"},sortOrder);
        if(cursor!=null){
            while (cursor.moveToNext()){
                String title=cursor.getString(0);
                String duration=cursor.getString(1);
                String artist=cursor.getString(2);
                String album=cursor.getString(3);
                String path=cursor.getString(4);
                MusicFile musicFile=new MusicFile(path,title,duration,album,artist);
                Log.e("Path", "PATH:"+path+"-Duration:"+duration );
                tempMusic.add(musicFile);
            }
            cursor.close();
        }
        return tempMusic;
    }
}