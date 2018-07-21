package com.example.teleprompter;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.teleprompter.database.File;
import com.example.teleprompter.database.FileDatabase;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditFragment extends Fragment {

    @BindView(R.id.et_edit_title)
    EditText et_title;

    @BindView(R.id.et_edit_contents)
    EditText et_contents;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_save) {
            //Insert the file into the database
            String title = et_title.getText().toString().trim();
            String contents = et_contents.getText().toString().trim();
            contents = contents.substring(0, contents.length() > 300 ? 300 : contents.length());
            final File currentFile = new File(title, contents, new Date());

            //Todo -- Check if the name already exists

            final FileDatabase db = FileDatabase.getInstance(getActivity());
            AppExecutors.getInstance().diskIo().execute(new Runnable() {
                @Override
                public void run() {
                    db.fileDao().insertFile(currentFile);
                }
            });
            if (getActivity() != null) getActivity().finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
