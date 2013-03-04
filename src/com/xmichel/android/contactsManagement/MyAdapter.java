package com.xmichel.android.contactsManagement;


import java.util.List;

import com.xmichel.android.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author xavier
 * Classe qui va contruire la listView
 */
public class MyAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Contact> contacts;
    private ViewHolder holder;

    public MyAdapter(Context c, List<Contact> contacts) {
        mContext = c;
        mInflater = LayoutInflater.from(mContext);
        this.contacts = contacts;
        
    }

    private static class ViewHolder {
        ImageView photo;
        TextView name;
        TextView birthday;
        TextView info;
    }

    public int getCount() {
        return contacts.size();
    }

    public Object getItem(int position) {
        return contacts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.contact, null);

            holder = new ViewHolder();
            holder.photo = (ImageView) convertView.findViewById(R.id.list_item_image);
            holder.name = (TextView) convertView.findViewById(R.id.list_item_name);
            holder.birthday = (TextView) convertView.findViewById(R.id.list_item_birthday);
            holder.info = (TextView) convertView.findViewById(R.id.list_item_info);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact c = (Contact) getItem(position);

        holder.photo.setImageBitmap(c.getBitmap());
        holder.name.setText(c.getName());
        holder.birthday.setText(c.getBirthday());
        holder.info.setText(c.getInfo());

        return convertView;
    }
}
