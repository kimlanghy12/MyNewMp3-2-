package com.example.tung.mynewmp3;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Artist extends Fragment {
    ArrayList<Song> listSongWithArtist;
    ArrayList<String> artistList;
    ArrayList<Song> listSong;

    public Artist() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        listSongWithArtist = new ArrayList<>();
        if (artistList == null) {
            artistList = new ArrayList<>();
            for (int i = 0; i < listSong.size(); i++) {
                String artist = listSong.get(i).getArtist();
                if (artist != null && !artistList.contains(artist)) {
                    artistList.add(artist);
                }
            }
        }
        View view = getView();
        ListView artistListView = (ListView) view.findViewById(R.id.artist_list_view);
        ArrayAdapter<String> temp = new ArrayAdapter<String>(getContext(), R.layout.list_string_item, R.id.list_content, artistList);
        artistListView.setAdapter(temp);
        artistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nameOfArtist = artistList.get(position);
                getSongListWithArtist(nameOfArtist);
                Intent temp = new Intent(getContext(), DetailSongList.class);
                EventBus.getDefault().removeStickyEvent(MessageEvent.class);
                EventBus.getDefault().postSticky(new MessageEvent(listSongWithArtist));
                startActivity(temp);
            }
        });
    }

    public void getSongListWithArtist(String nameOfArtist) {
        for (int i = 0; i < listSong.size(); i++) {
            if (listSong.get(i).getArtist().equals(nameOfArtist)) {
                listSongWithArtist.add(listSong.get(i));
            }
        }
    }

    @Subscribe(sticky = true)
    public void onEvent(MessageEvent event) {
        if (listSong == null) {
            listSong = event.getListSong();
        }
    }
}
