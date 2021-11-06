package com.dktechub.apkextractor2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder>{
    ArrayList<App> apps = new ArrayList<>();
    OnItemClickListener onItemClickListener;

    public AppAdapter(OnItemClickListener onItemClickListener)
    {
        this.onItemClickListener=onItemClickListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context=parent.getContext();
        LayoutInflater inflator = LayoutInflater.from(context);
        View VideoView = inflator.inflate(R.layout.app_layout,parent,false);
        return new ViewHolder(VideoView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(apps.get(position).name);
        if(apps.get(position).icon!=null)
        holder.icon.setImageDrawable(apps.get(position).icon);
        holder.size.setText(getSize(apps.get(position).size));
        holder.cont.setOnClickListener(view -> onItemClickListener.onItemClicked(apps.get(position)));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name,size;
        public ImageView icon;
        public LinearLayout cont;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            size=itemView.findViewById(R.id.size);
            icon=itemView.findViewById(R.id.icon);
            cont=itemView.findViewById(R.id.cont);
        }
    }
    public static String getSize(long num)
    {
        String str;
        int KB = 1000;
        int MB = KB*1000;
        int GB = MB*1000;
        if(num>=GB)
            str=String.format("%.2f GB",(float)num/GB);
        else if(num>=MB)
            str=String.format("%.2f MB",(float)num/MB);
        else if(num>=KB)
            str=String.format("%.2f KB",(float)num/KB);
        else str=String.format("%s B", num);
        return str;


    }
    public interface OnItemClickListener{
        void onItemClicked(App app);
    }
}
