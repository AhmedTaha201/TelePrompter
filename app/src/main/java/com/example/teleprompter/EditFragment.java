package com.example.teleprompter;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.teleprompter.database.File;
import com.example.teleprompter.database.FileDatabase;
import com.example.teleprompter.utils.FileUtils;
import com.example.teleprompter.viewmodels.FileViewModel;
import com.example.teleprompter.viewmodels.FileViewModelFactory;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

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
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(MainFragment.EDIT_INTENT_XTRA_TITLE)) {
            ReadFileTask readFileTask = new ReadFileTask(getActivity());
            readFileTask.execute(intent.getStringExtra(MainFragment.EDIT_INTENT_XTRA_TITLE));
        }

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
            saveCurrentFile();
            return true;
        }
        return false;
    }

    private void saveCurrentFile() {
        //Insert the file into the database
        final String title = et_title.getText().toString().trim();
        String contents = et_contents.getText().toString().trim();

        //Check for empty string
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(contents)) {
            showEmptyDialog(title);
            return;
        }
        contents = contents.substring(0, contents.length() > 300 ? 300 : contents.length());
        final File newFile = new File(title, contents, new Date());


        final FileDatabase db = FileDatabase.getInstance(getActivity());

        FileViewModelFactory factory = new FileViewModelFactory(db, title);
        final FileViewModel fileViewModel = ViewModelProviders.of(this, factory)
                .get(FileViewModel.class);
        fileViewModel.getmFile().observe(this, new Observer<File>() {
            @Override
            public void onChanged(@Nullable File file) {
                fileViewModel.getmFile().removeObserver(this);
                if (file != null && file.getFileName().equals(newFile.getFileName())) {
                    //A file with this name exists
                    showOverwriteDialog(newFile, file, db);
                } else {
                    //There is no file with this name
                    InsertFile(newFile, db);
                }
            }
        });

        //Save to the internal storage
        FileUtils.WriteFileTask writeFileTask = new FileUtils.WriteFileTask(getActivity());
        writeFileTask.execute(title, contents);


    }

    public class ReadFileTask extends AsyncTask<String, Void, String> {

        String fileName;
        private Context mContext;

        public ReadFileTask(Context mContext) {
            this.mContext = mContext;
        }


        @Override
        protected String doInBackground(String... strings) {
            fileName = strings[0];

            try {
                FileInputStream inputStream = mContext.openFileInput(fileName);
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder builder = new StringBuilder();
                String line = bufferedReader.readLine();

                while (line != null) {
                    builder.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                return builder.toString();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Timber.e("Failed to find the file with the name : %s", fileName);
            } catch (IOException e) {
                e.printStackTrace();
                Timber.e("Failed to read the file with the name : %s", fileName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            et_title.setText(fileName);
            et_contents.setText(s);


        }

    }

    //A helper method to show an alert dialog if either of the editTexts is empty
    private void showEmptyDialog(final String title) {
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.empty_dialog_title)
                .setPositiveButton(R.string.empty_dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        //Focus on the empty editText
                        if (TextUtils.isEmpty(title)) {
                            et_title.requestFocus();
                        } else {
                            et_contents.requestFocus();
                        }

                    }
                }).show();
    }

    //A helper method to show an alert dialog to overwrite the existing file or rename it
    private void showOverwriteDialog(final File newFile, final File currentFile, final FileDatabase db) {
        // Showing alert dialog
        // https://developer.android.com/reference/android/app/AlertDialog
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.overwrite_dialog_title)
                .setMessage(R.string.overwrite_dialog_message)
                .setPositiveButton(R.string.overwrite_dialog_button_rename, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        et_title.requestFocus(); //https://stackoverflow.com/questions/14327412/set-focus-on-edittext
                    }
                })
                .setNegativeButton(R.string.overwrite_dialog_button_overwrite, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppExecutors.getInstance().diskIo().execute(new Runnable() {
                            @Override
                            public void run() {
                                currentFile.setFileContents(newFile.getFileContents());
                                currentFile.setDate(newFile.getDate());
                                db.fileDao().updateFile(currentFile);
                                getActivity().finish();
                            }
                        });
                    }
                })
                .show();
    }

    //a helper method to insert the current file into the database
    private void InsertFile(final File currentFile, final FileDatabase db) {
        //inserting into the database
        AppExecutors.getInstance().diskIo().execute(new Runnable() {
            @Override
            public void run() {
                db.fileDao().insertFile(currentFile);
            }
        });
        if (getActivity() != null) getActivity().finish();
    }
}
