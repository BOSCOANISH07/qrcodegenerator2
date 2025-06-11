package com.example.qrcodegenerator.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.qrcodegenerator.R;

public class DashboardAdapter extends BaseAdapter {

    private Context context;
    private String[] options;
    private int[] icons;
    private LayoutInflater inflater;

    public DashboardAdapter(Context context, String[] options, int[] icons) {
        this.context = context;
        this.options = options;
        this.icons = icons;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return options.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_dashboard, null);
        }

        ImageView icon = convertView.findViewById(R.id.ivIcon);
        TextView text = convertView.findViewById(R.id.tvOption);

        icon.setImageResource(icons[position]);
        text.setText(options[position]);

        return convertView;
    }
}