package com.example.teleprompter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.teleprompter.database.File;

import java.text.SimpleDateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileViewHolder> {

    private Context mContext;
    private List<File> mFilesList;
    private ListItemOnClickListener mItemClickListener;

    public FilesAdapter(Context mContext, List<File> mFilesList, ListItemOnClickListener mItemClickListener) {
        this.mContext = mContext;
        this.mFilesList = mFilesList;
        this.mItemClickListener = mItemClickListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.recycler_item_main, parent, false);

        return new FileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File currentFile = mFilesList.get(position);
        String fileName = currentFile.getFileName();
        holder.itemView.setTag(fileName);
        holder.fileName.setText(fileName);
        holder.fileContents.setText(currentFile.getFileContents());
        holder.fileDate.setText(new SimpleDateFormat("dd/MM/yy kk:mm").format(currentFile.getDate()));
    }

    @Override
    public int getItemCount() {
        return mFilesList != null ? mFilesList.size() : 0;
    }

    public void setFilesList(List<File> filesList) {
        if (mFilesList != filesList) {
            mFilesList = filesList;
            notifyDataSetChanged();
        }
    }

    class FileViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.tv_main_file_name)
        TextView fileName;

        @BindView(R.id.tv_main_file_contents)
        TextView fileContents;

        @BindView(R.id.tv_main_file_date)
        TextView fileDate;

        public FileViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener == null) return;
            mItemClickListener.onListItemClick(getAdapterPosition());
        }
    }

    public interface ListItemOnClickListener {
        void onListItemClick(int position);
    }

}
