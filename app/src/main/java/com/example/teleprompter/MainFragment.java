package com.example.teleprompter;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.teleprompter.customview.CustomFloatingActionButton;
import com.example.teleprompter.database.File;
import com.example.teleprompter.viewmodels.MainViewModel;
import com.example.teleprompter.widget.FilesWidgetProvider;
import com.gordonwong.materialsheetfab.MaterialSheetFab;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static android.app.Activity.RESULT_OK;


public class MainFragment extends Fragment implements FilesAdapter.ListItemOnClickListener {

    public static final String INTENT_EXTRA_FILE_NAME = "title_extra";
    public static final String INTENT_EXTRA_FILE_PATH = "path_extra";
    private static final int PERMISSION_CODE_STORAGE = 11;
    private static final int REQUEST_CODE_TXT = 500;

    private FilesAdapter mAdapter;
    List<File> mFiles;

    MaterialSheetFab materialSheetFab;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab_main_add_new)
    CustomFloatingActionButton fab;

    @BindView(R.id.fab_sheet)
    View sheetView;

    @BindView(R.id.dim_overlay)
    View overlay;

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
        setupSheetFab();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_TXT && RESULT_OK == resultCode) {
            List<String> docPaths = new ArrayList<>(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
            //Start EditActivity with the file path
            Intent intent = new Intent(getActivity(), EditActivity.class);
            intent.putExtra(INTENT_EXTRA_FILE_PATH, docPaths.get(0));
            startActivity(intent);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            importNewFile();
        } else {
            Toast.makeText(getActivity(), R.string.permission_toast_text, Toast.LENGTH_SHORT).show();
        }
    }

    void setupMainViewModel() {
        MainViewModel mainViewModel = ViewModelProviders.of(getActivity())
                .get(MainViewModel.class);

        mainViewModel.getFileList().observe(this, new Observer<List<File>>() {
            @Override
            public void onChanged(@Nullable List<File> files) {
                mAdapter.setFilesList(files);
                mFiles = files;

                //Update app widgets
                AppWidgetManager manager = AppWidgetManager.getInstance(getActivity());
                int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(getActivity(), FilesWidgetProvider.class));
                manager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_file_names_list);
                FilesWidgetProvider.updateWidgets(getActivity(), manager, appWidgetIds);

            }
        });
    }

    void setupSheetFab() {
        int sheetColor = getResources().getColor(R.color.fab_sheet_background_color);
        int fabColor = getResources().getColor(R.color.colorAccent);

        // Initialize material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay,
                sheetColor, fabColor);
    }

    @OnClick(R.id.fab_entry_import_text)
    public void importNewFile() {
        if (materialSheetFab.isSheetVisible()) materialSheetFab.hideSheet();
        //Check storage permission
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_CODE_STORAGE);
        } else {
            String[] textTtype = new String[]{"txt"};
            FilePickerBuilder.getInstance()
                    .setMaxCount(1)
                    .addFileSupport(getString(R.string.file_picker_text_tab_title), textTtype)
                    .enableDocSupport(false)
                    .setActivityTheme(R.style.AppTheme)
                    .pickFile(this, REQUEST_CODE_TXT);

        }
    }

    @OnClick(R.id.fab_entry_create_new)
    public void createNewFile() {
        if (materialSheetFab.isSheetVisible()) materialSheetFab.hideSheet();
        Intent editIntent = new Intent(getActivity(), EditActivity.class);
        startActivity(editIntent);
    }

    @Override
    public void onListItemClick(final int position) {

        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(INTENT_EXTRA_FILE_NAME, mFiles.get(position).getFileName());
        startActivity(intent);
    }
}
