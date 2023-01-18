package com.mobilestyx.JLRMaximizer.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.mobilestyx.JLRMaximizer.R;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class CustomList extends ArrayAdapter<String>{

    private final Activity context;
    private final Integer[] imageId;

    public CustomList(Activity context, Integer[] imageId) {
        super(context, R.layout.list_single);
        this.context = context;

        this.imageId = imageId;

    }
    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.list_single, null, true);

        ImageView imageView = (ImageView) rowView.findViewById(R.id.img);

        imageView.setImageResource(imageId[position]);
        return rowView;
    }

    @Override
    public int getCount() {
        return imageId.length;
    }
}