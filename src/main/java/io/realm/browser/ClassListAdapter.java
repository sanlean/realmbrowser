package io.realm.browser;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.browser.R.id;
import io.realm.browser.R.layout;
import io.realm.Realm;
import io.realm.RealmObject;

class ClassListAdapter extends Adapter<ClassListAdapter.ViewHolder> {
    private List<ClassListAdapter.DataHolder> mData;
    private ClassListAdapter.OnItemClickListener mItemClickListener;

    ClassListAdapter(@NonNull List<Class<? extends RealmObject>> data, ClassListAdapter.OnItemClickListener listener) {
        if(this.mData == null) {
            this.mData = new ArrayList(data.size());
        }

        this.updateData(data);
        this.mItemClickListener = listener;
    }

    public void updateData(@NonNull List<Class<? extends RealmObject>> data) {
        this.mData.clear();
        Iterator i$ = data.iterator();

        while(i$.hasNext()) {
            Class clazz = (Class)i$.next();
            Realm realm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(clazz));
            int recordsNumber = realm.where(clazz).findAll().size();
            this.mData.add(new ClassListAdapter.DataHolder(clazz.getSimpleName(), recordsNumber));
            realm.close();
        }

    }

    public ClassListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(layout.realm_browser_class_list_item, viewGroup, false);
        return new ClassListAdapter.ViewHolder(v, this.mItemClickListener);
    }

    public void onBindViewHolder(ClassListAdapter.ViewHolder viewHolder, int position) {
        ClassListAdapter.DataHolder holder = (ClassListAdapter.DataHolder)this.mData.get(position);
        viewHolder.textView.setText(holder.className);
        viewHolder.recordsNumber.setText(Integer.toString(holder.itemNumber));
    }

    public int getItemCount() {
        return this.mData != null?this.mData.size():0;
    }

    private static class DataHolder {
        String className;
        int itemNumber;

        DataHolder(String className, int itemNumber) {
            this.className = className;
            this.itemNumber = itemNumber;
        }
    }

    static class ViewHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements OnClickListener {
        final TextView textView;
        final TextView recordsNumber;
        private final ClassListAdapter.OnItemClickListener mListener;

        public ViewHolder(View itemView, ClassListAdapter.OnItemClickListener listener) {
            super(itemView);
            this.textView = (TextView)itemView.findViewById(id.class_list_item_name);
            itemView.setOnClickListener(this);
            this.mListener = listener;
            this.recordsNumber = (TextView)itemView.findViewById(id.class_list_counter);
        }

        public void onClick(View v) {
            if(this.mListener != null) {
                this.mListener.onItemClick(v, this.getAdapterPosition());
            }

        }
    }

    interface OnItemClickListener {
        void onItemClick(View var1, int var2);
    }
}
