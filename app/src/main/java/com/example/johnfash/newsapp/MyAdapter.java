package com.example.johnfash.newsapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    public static ClickListener clickListener;
    Context mContext;
    List<Model> modelList;
    ArrayList<Model> arrayList;

    public MyAdapter(Context context, List<Model> modelList) {
        this.mContext = context;
        this.modelList = modelList;
        this.arrayList = new ArrayList<>();
        this.arrayList.addAll(modelList);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        MyAdapter.clickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.mNewsSectionTv.setText(modelList.get(position).getNewSection());
        holder.mNewsTitleTv.setText(modelList.get(position).getNewsTitle());
        holder.mNewsDateTv.setText(modelList.get(position).getNewsDate());
        holder.mNewsExactHourTv.setText(modelList.get(position).getNiceDate());
        if (modelList.get(position).getNewsAuthor().equals("")) {
            holder.mNewsAuthorTv.setText(R.string.default_author);
        } else {
            String bind = mContext.getString(R.string.author) + " " + modelList.get(position).getNewsAuthor();
            holder.mNewsAuthorTv.setText(bind);
        }
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView mNewsSectionTv, mNewsTitleTv, mNewsDateTv, mNewsExactHourTv, mNewsAuthorTv;

        public MyViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            mNewsSectionTv = view.findViewById(R.id.sectionName);
            mNewsTitleTv = view.findViewById(R.id.webTitle);
            mNewsDateTv = view.findViewById(R.id.webPublicationDate);
            mNewsExactHourTv = view.findViewById(R.id.hoursAgo);
            mNewsAuthorTv = view.findViewById(R.id.author);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }
}