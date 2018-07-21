package com.example.teleprompter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.teleprompter.database.File;
import com.example.teleprompter.database.FileDatabase;
import com.example.teleprompter.viewmodels.MainViewModel;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainFragment extends Fragment implements FilesAdapter.ListItemOnClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    private FilesAdapter mAdapter;
    List<File> mFiles;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new FilesAdapter(getActivity(), null, this);
        int spanCount = getActivity().getResources().getInteger(R.integer.recycler_item_count);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
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
                mFiles = files;
            }
        });
    }

    @OnClick(R.id.fab_main_add_new)
    public void addNewFile() {
        Intent editIntent = new Intent(getActivity(), EditActivity.class);
        startActivity(editIntent);
    }

    @Override
    public void onListItemClick(final int position) {
        Toast.makeText(getActivity(), "Item Deleted : " + String.valueOf(position), Toast.LENGTH_SHORT).show();
        AppExecutors.getInstance().diskIo().execute(new Runnable() {
            @Override
            public void run() {
                FileDatabase.getInstance(getActivity()).fileDao().deleteFile(mFiles.get(position));
            }
        });

    }
}
