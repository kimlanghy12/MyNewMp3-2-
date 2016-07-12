package com.example.tung.mynewmp3;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import org.greenrobot.eventbus.*;

/**
 * A simple {@link Fragment} subclass.
 */
public class Songs extends Fragment {

    ListView listSongView;
    ArrayList<Song> listSong;

    public Songs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_songs, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        View view = getView();
        listSongView = (ListView) view.findViewById(R.id.list_song);
        SongAdapter temp = new SongAdapter(getContext(), listSong);
        listSongView.setAdapter(temp);
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
