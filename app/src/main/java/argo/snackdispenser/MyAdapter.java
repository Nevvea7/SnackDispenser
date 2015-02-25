package argo.snackdispenser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Snack> list;

    public MyAdapter(Context context, ArrayList<Snack> list) {
        this.context = context;
        this.list = list;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View gridView;

        if (convertView == null) {
            gridView = new View(context);


        } else {
            gridView = (View) convertView;

        }

        // get layout from cell.xml
        gridView = inflater.inflate(R.layout.cell, null);

        Snack snack = list.get(position);
        System.out.print(snack.getName());
        // set value into textview
        TextView snName = (TextView) gridView
                .findViewById(R.id.sn_label);
        snName.setText(snack.getName());
        TextView snQuat = (TextView) gridView
                .findViewById(R.id.sn_quat);
        snQuat.setText(Integer.toString(snack.getQuat()));

        // set image
        ImageView imageView = (ImageView) gridView
                .findViewById(R.id.sn_image);
        imageView.setImageResource(snack.getIconid());


        return gridView;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

}
