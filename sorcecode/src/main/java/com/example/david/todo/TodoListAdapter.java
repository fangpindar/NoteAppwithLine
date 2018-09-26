package com.example.david.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;

import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder>
                                implements ItemTouchHelperAdapter{
    private List<TaskData> tasks;
    private int lastPosition = -1;
    private Context context;

    TodoListAdapter(List<TaskData> t, Context context) {
        tasks = new ArrayList<TaskData>();
        tasks.addAll(t);
        Collections.sort(tasks, new dateComparator());
        this.context = context;
    }

    class dateComparator implements Comparator<TaskData> {
        public int compare(TaskData t1, TaskData t2) {
            return t1.getDate().compareTo( t2.getDate());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.todo_cell, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.donemark = view.findViewById(R.id.donemark);
        holder.taskname = view.findViewById(R.id.taskname);
        holder.taskdate = view.findViewById(R.id.taskdate);

        holder.donemark.setImageResource(R.drawable.minus);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        SimpleDateFormat df = new SimpleDateFormat("MM/dd");
        holder.donemark.setImageResource(R.drawable.minus);
        holder.taskname.setText(tasks.get(position).getTitle());
        String t = df.format(tasks.get(position).getDate());
        Date d = tasks.get(position).getDate();
        setAnimation(holder.itemView, position);

        if (t.equals(df.format(new Date()))) {
            holder.taskdate.setText("今天");
        }
        else  if (d.before(new Date())) holder.taskdate.setText("已過期");
        else holder.taskdate.setText(t);

    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView donemark;
        public TextView taskname;
        public TextView taskdate;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    // 設定動畫效果
    private void setAnimation(View view, int position) {
        // 如果是最後一個項目
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(
                    context, android.R.anim.fade_in);
            animation.setDuration(1200);
            view.startAnimation(animation);
            lastPosition = position;
        }
    }

    public void add(TaskData newtask) {
        tasks.add(newtask);
        notifyItemInserted(tasks.size());
    }

    public void remove(int position) {
        tasks.remove(position);
        // 通知資料項目已經刪除
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, tasks.size());
    }

    // 實作 ItemTouchHelperAdapter 介面的方法
    // 左右滑動項目
    @Override
    public void onItemDismiss(int position) {
        // 刪除項目
        remove(position);
    }

    // 實作 ItemTouchHelperAdapter 介面的方法
    // 長按並移動項目
    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            // 如果是往下拖拉
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(tasks, i, i + 1);
            }
        } else {
            // 如果是往上拖拉
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(tasks, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    public void saveTask(SharedPreferences pref) {
        String data = new String();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getTitle().isEmpty()) continue;
            data += tasks.get(i).getTitle() + "-=-=-=-";
            data += tasks.get(i).getDate().getTime() + "\n";
        }
        pref.edit().putString("tasks", data).apply();
    }
}

