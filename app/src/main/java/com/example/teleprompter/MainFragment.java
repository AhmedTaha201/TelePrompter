package com.example.teleprompter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.teleprompter.database.File;
import com.example.teleprompter.viewmodels.MainViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainFragment extends Fragment implements FilesAdapter.ListItemOnClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private FilesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new FilesAdapter(getActivity(), null, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);

        setupMainViewModel();
        return view;
    }

    void setupMainViewModel() {
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewModel.class);

        mainViewModel.getFileList().observe(this, new Observer<List<File>>() {
            @Override
            public void onChanged(@Nullable List<File> files) {
                mAdapter.setFilesList(files);
            }
        });
    }

    @Override
    public void onListItemClick(int position) {
        Toast.makeText(getActivity(), "Item Clicked : " + String.valueOf(position), Toast.LENGTH_SHORT).show();

    }
}
