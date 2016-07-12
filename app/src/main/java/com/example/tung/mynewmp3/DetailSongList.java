package com.example.tung.mynewmp3;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

public class DetailSongList extends AppCompatActivity implements MediaController.MediaPlayerControl{
    ArrayList<Song> listSong;
    Boolean musicBound = false;
    MusicService musicService;
    Intent playIntent;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(listSong);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };
    Boolean playbackPaused = false;
    MusicController musicController;
    Boolean isShuffle =false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_song_list);
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
        musicController.setAnchorView(findViewById(R.id.detail_song_container));
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

    @Subscribe(sticky = true)
    public void onEvent(MessageEvent event) {
        listSong = event.getListSong();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        EventBus.getDefault().postSticky(new MessageEvent(listSong));
        playIntent = new Intent(this, MusicService.class);
        startService(playIntent);
        bindService(playIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    public void songPicked(View view) {
        musicService.setSong(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if(playbackPaused){
            playbackPaused=false;
        }
        setMusicController();
        musicController.show(0);
    }

    @Override
    protected void onStop() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
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
                    musicService.setShuffle();
                    item.setIcon(R.drawable.repeat_48);
                }else{
                    musicService.setShuffle();
                    item.setIcon(R.drawable.shuffle_48);
                }

        }
        return super.onOptionsItemSelected(item);
    }
}
