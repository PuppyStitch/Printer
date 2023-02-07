package com.simcom.printer;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SpinnerAdapter extends BaseAdapter {

    String itemStr[];
    Context context;
    ViewHolder viewHolder;

    public SpinnerAdapter(Context context, String[] str) {
        this.context = context;
        itemStr = str;
    }

    @Override
    public int getCount() {
        return itemStr.length;
    }

    @Override
    public Object getItem(int position) {
        return itemStr[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null){
            convertView = View.inflate(context, R.layout.spinner_item, null);
            viewHolder = new ViewHolder();
            viewHolder.description = (TextView) convertView.findViewById(R.id.item_description);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.description.setText(itemStr[position]);

        return convertView;
    }

    class ViewHolder {
        private TextView description;
    }
}
