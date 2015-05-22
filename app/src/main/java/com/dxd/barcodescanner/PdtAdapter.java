package com.dxd.barcodescanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by dxd on 5/21/15.
 */
public class PdtAdapter extends ArrayAdapter<Products> {
    ArrayList<Products> prodKeysValues;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public PdtAdapter(Context context, int resource, ArrayList<Products> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        prodKeysValues = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.key = (TextView) v.findViewById(R.id.listKey);
            holder.value = (TextView) v.findViewById(R.id.listValue);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.key.setText(prodKeysValues.get(position).getKey());
        holder.value.setText(prodKeysValues.get(position).getValue());
        return v;
    }

    static class ViewHolder {
        public TextView key;
        public TextView value;
    }
}
