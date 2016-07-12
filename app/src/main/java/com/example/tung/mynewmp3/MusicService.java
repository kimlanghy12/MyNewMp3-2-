package com.example.tung.mynewmp3;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Random;

public class MusicService extends Service implements 
MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
MediaPlayer.OnCompletionListener {

	private MediaPlayer player;
	private ArrayList<Song> songs;
	private int songPosn;
	private final IBinder musicBind = new MusicBinder();
	private String songTitle="";
	private static final int NOTIFY_ID=1;
	private boolean shuffle=false;
	private Random rand;

	public void onCreate(){
		super.onCreate();
		songPosn=0;
		rand=new Random();
		player = new MediaPlayer();
		initMusicPlayer();
	}

	public void initMusicPlayer(){
		player.setWakeMode(getApplicationContext(),
				PowerManager.PARTIAL_WAKE_LOCK);
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	public void setList(ArrayList<Song> theSongs){
		songs=theSongs;
	}

	public class MusicBinder extends Binder {
		MusicService getService() { 
			return MusicService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return musicBind;
	}

	@Override
	public boolean onUnbind(Intent intent){
		//player.stop();
		//player.release();
		return false;
	}

	public void playSong(){
		player.reset();
		Song playSong = songs.get(songPosn);
		songTitle=playSong.getTitle();
		long currSongID = playSong.getID();
		Uri trackUri = ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				currSongID);
		try{
			player.setDataSource(getApplicationContext(), trackUri);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		player.prepareAsync(); 
	}

	public void setSong(int songIndex){
		songPosn=songIndex;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if(player.getCurrentPosition()>0){
			playNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.v("MUSIC PLAYER", "Playback Error");
		mp.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
		Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent pendInt = PendingIntent.getActivity(this, 0,
				notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		//stop everything
		Intent serviceIntent = new Intent(this, MusicService.class);
		serviceIntent.setAction("STOP_EVERY_THING");
		PendingIntent stopPenIntent = PendingIntent.getService(this, 0 , serviceIntent, 0);
		//
		builder.setContentIntent(pendInt)
		.setSmallIcon(android.R.drawable.ic_media_play)
		.setTicker(songTitle)
		.setOngoing(true)
		.setContentTitle("Playing")
		.setContentText(songTitle)
		.addAction(android.R.drawable.ic_delete,"Stop",stopPenIntent);

        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);

		EventBus.getDefault().postSticky("SERVICE_PREPARED");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent.getAction()!=null){
			if(intent.getAction().equals("STOP_EVERY_THING")){
				player.stop();
				player.release();
				stopForeground(true);
				stopSelf();
				this.onDestroy();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public int getPosn(){
		return player.getCurrentPosition();
	}

	public int getDur(){
		return player.getDuration();
	}

	public boolean isPlaying(){
		try{
			return player.isPlaying() ;
		}catch (Exception e){
			e.printStackTrace();
		}

		return false;
	}


	public void pausePlayer(){
		player.pause();
	}

	public void seek(int posn){
		player.seekTo(posn);
	}

	public void start(){
		player.start();
	}

	public void playPrev(){
		songPosn--;
		if(songPosn<0) songPosn=songs.size()-1;
		playSong();
	}

	public void playNext(){
		if(shuffle){
			int newSong = songPosn;
			while(newSong==songPosn){
				newSong=rand.nextInt(songs.size());
			}
			songPosn=newSong;
		}
		else{
			songPosn++;
			if(songPosn>=songs.size()) songPosn=0;
		}
		playSong();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void setShuffle(){
		if(shuffle) shuffle=false;
		else shuffle=true;
	}

}
