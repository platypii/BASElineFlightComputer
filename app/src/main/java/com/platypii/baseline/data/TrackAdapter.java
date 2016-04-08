package com.platypii.baseline.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.platypii.baseline.R;

import java.util.List;

public class TrackAdapter extends ArrayAdapter<Jump> {

    private final List<Jump> jumpList;

    public TrackAdapter(Context context, int resource, List<Jump> jumpList) {
        super(context, resource, jumpList);
        this.jumpList = jumpList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Jump jump = jumpList.get(position);
        if(convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.jump_list_item, parent, false);
        }
        final TextView textView = (TextView) convertView.findViewById(R.id.list_name);
        final TextView sizeView = (TextView) convertView.findViewById(R.id.list_filesize);
        final ProgressBar spinnerView = (ProgressBar) convertView.findViewById(R.id.list_spinner);

        textView.setText(jump.toString());
        sizeView.setText(jump.getSize());
        if(jump.getCloudData() != null) {
            spinnerView.setVisibility(View.GONE);
        } else {
            spinnerView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

}
