package com.example.tung.mynewmp3;

import java.util.ArrayList;

/**
 * Created by tungnt244 on 7/4/16.
 */
public class MessageEvent {
    ArrayList<Song> listSong;
    public MessageEvent (ArrayList<Song> temp){
        listSong = temp;
    }
    public ArrayList<Song> getListSong(){
        return listSong;
    }
}
