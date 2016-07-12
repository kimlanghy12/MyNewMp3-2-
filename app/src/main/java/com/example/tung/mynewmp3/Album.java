package com.example.tung.mynewmp3;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import org.greenrobot.eventbus.*;


/**
 * A simple {@link Fragment} subclass.
 */
public class Album extends Fragment {
    ArrayList<Song> listSong;
    ArrayList<String> albumList;
    ArrayList<Song> listSongWithAlbum;

    public Album() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        listSongWithAlbum = new ArrayList<>();
        if (albumList == null) {
            albumList = new ArrayList<>();
            for (int i = 0; i < listSong.size(); i++) {
                String album = listSong.get(i).getAlbum();
                if (album != null && !albumList.contains(album)) {
                    albumList.add(album);
                }
            }
        }
        View view = getView();
        ListView albumListView = (ListView) view.findViewById(R.id.album_list_view);
        ArrayAdapter<String> temp = new ArrayAdapter<String>(getContext(), R.layout.list_string_item,R.id.list_content, albumList);
        albumListView.setAdapter(temp);
        albumListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nameOfAlbum = albumList.get(position);
                getSongListWithAlbum(nameOfAlbum);
                Intent temp = new Intent(getContext(), DetailSongList.class);
                EventBus.getDefault().removeStickyEvent(MessageEvent.class);
                EventBus.getDefault().postSticky(new MessageEvent(listSongWithAlbum));
                startActivity(temp);
            }
        });
    }

    public void getSongListWithAlbum(String nameOfAlbum) {
        for (int i = 0; i < listSong.size(); i++) {
            if (listSong.get(i).getAlbum().equals(nameOfAlbum)) {
                listSongWithAlbum.add(listSong.get(i));
            }
        }
    }

    @Subscribe(sticky = true)
    public void onEvent(MessageEvent event) {
        if (listSong == null) {
            listSong = event.getListSong();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
