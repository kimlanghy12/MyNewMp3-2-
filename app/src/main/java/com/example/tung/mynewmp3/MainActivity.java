package com.example.tung.mynewmp3;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;

import java.util.ArrayList;

import org.greenrobot.eventbus.*;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl{
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPageAdapter viewPageAdapter;
    ArrayList<Song> listSong;
    MusicService musicService;
    Boolean musicBound = false;
    Intent playIntent;
    ServiceConnection serviceConnection= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(listSong);
            musicBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound=false;
        }
    };

    Boolean playbackPaused = false;
    MusicController musicController;
    Boolean isShuffle = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupViewPaperAdapter();
        viewPager = (ViewPager) findViewById(R.id.viewpaper);
        viewPager.setAdapter(viewPageAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //get the song from storage
        listSong = new ArrayList<>();
        getSongList();
        //send the list to bus
        MessageEvent temp = new MessageEvent(listSong);
        EventBus.getDefault().register(this);
        EventBus.getDefault().postSticky(temp);
        playIntent = new Intent(this, MusicService.class);

        setMusicController();

    }

    void setMusicController(){
        if (musicController==null) musicController = new MusicController(this);
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        musicController.setAnchorView(findViewById(R.id.viewpaper));
        musicController.setMediaPlayer(this);
        musicController.setEnabled(true);
    }

    private void playNext(){
        musicService.playNext();
        musicController.show(0);
    }

    //play previous
    private void playPrev(){
        musicService.playPrev();
        musicController.show(0);
    }

    //get list song method
    public void getSongList(){
        ContentResolver contentResolver = this.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor c = contentResolver.query(musicUri,null,null,null,null);
        if(c!=null && c.moveToFirst()){
            int titleCol = c.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistCol = c.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int idCol = c.getColumnIndex(MediaStore.Audio.Media._ID);
            int albumCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            do{
                long id = c.getLong(idCol);
                String title = c.getString(titleCol);
                String artist = c.getString(artistCol);
                String album = c.getString(albumCol);
                listSong.add(new Song(id,title,artist, album));
            }while(c.moveToNext());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startService(playIntent);
        bindService(playIntent,serviceConnection, Context.BIND_AUTO_CREATE);

    }

    public void songPicked(View view){
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            playbackPaused=false;
            setMusicController();
        }
        musicController.show(0);
    }
    @Subscribe(sticky = true)
    public void PreparedEvent (String temp){
        musicController.show(0);
        EventBus.getDefault().removeStickyEvent(String.class);
    }
    public void setupViewPaperAdapter(){
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        viewPageAdapter.addNewFragment(new Songs(), "Songs");
        viewPageAdapter.addNewFragment(new Album(), "Album");
        viewPageAdapter.addNewFragment(new Artist(), "Artist");

    }

    @Override
    public void start() {
        musicService.start();
    }

    @Override
    public void pause() {
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if(musicService !=null && musicBound && musicService.isPlaying()){
            return musicService.getDur();
        } else return 0;
    }

    @Override
    public int getCurrentPosition() {
        if(musicService !=null && musicBound && musicService.isPlaying()){
            return musicService.getPosn();
        }else return 0;
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound){
            return musicService.isPlaying();
        }else{
            return false;
        }
    }


    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    class ViewPageAdapter extends FragmentPagerAdapter{
        ArrayList<Fragment> fragmentArrayList = new ArrayList<>();
        ArrayList<String> title = new ArrayList<>();
        public ViewPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentArrayList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentArrayList.size();
        }

        public void addNewFragment(Fragment mf, String titl){
            fragmentArrayList.add(mf);
            title.add(titl);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title.get(position);
        }
        public ArrayList<Fragment> getFragment(){
            return fragmentArrayList;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){

            case R.id.action_shuffle:
                if(!isShuffle){
                    isShuffle=true;
                    musicService.setShuffle();
                    item.setIcon(R.drawable.repeat_48);
                }else{
                    isShuffle=false;
                    musicService.setShuffle();
                    item.setIcon(R.drawable.shuffle_48);
                }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
