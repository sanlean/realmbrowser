package io.realm.browser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.RealmObject;
import io.realm.browser.R.dimen;
import io.realm.browser.R.id;

class DatabaseClassAdapter extends RecyclerView.Adapter<DatabaseClassAdapter.ItemViewHolder> {
    private List<Field> mFields;
    private List<? extends RealmObject> mData;
    private HorizontalScrollMediator mScrollMediator;
    private ColumnWidthMediator mWidthMediator;
    private DatabaseClassAdapter.OnCellClickListener mExternalCellClickListener;
    private final RowView.OnCellClickListener mInternalCellClickListener = new RowView.OnCellClickListener() {
        public void onCellClick(RowView view, int position) {
            if(DatabaseClassAdapter.this.mExternalCellClickListener != null) {
                int row = ((Integer)view.getTag()).intValue();
                RealmObject object = (RealmObject)DatabaseClassAdapter.this.mData.get(row);
                Field field = (Field)DatabaseClassAdapter.this.mFields.get(position);
                DatabaseClassAdapter.this.mExternalCellClickListener.onCellClick(object, field, row);
            }

        }
    };

    DatabaseClassAdapter(Context context, Class<? extends RealmObject> clazz, List<? extends RealmObject> data) {
        this.mData = data;
        this.updateFields(context, clazz);
    }

    public void updateData(List<? extends RealmObject> data) {
        this.mData = data;
        this.notifyDataSetChanged();
    }

    public void updateDisplayedFields(Context context, Class<? extends RealmObject> clazz) {
        updateFields(context, clazz);
        this.notifyDataSetChanged();
    }

    public void updateDisplayedFields(Context context, Class<? extends RealmObject> clazz, List<? extends RealmObject> data) {
        mData = data;
        updateDisplayedFields(context, clazz);
    }

    private void updateFields(Context context, Class<? extends RealmObject> clazz) {
        mFields = new ArrayList();
        FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(context);
        Iterator i$ = RealmUtils.getFields(clazz).iterator();

        while(i$.hasNext()) {
            Field f = (Field)i$.next();
            if(prefs.isFieldDisplayed(clazz, f)) {
                mFields.add(f);
            }
        }

    }

    public DatabaseClassAdapter.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.realm_browser_database_class_item, parent, false);
        int colNmb = mFields.size();
        DatabaseClassAdapter.ItemViewHolder holder = new DatabaseClassAdapter.ItemViewHolder(v, colNmb);
        holder.mRowView.setCellsGravity(16);
        holder.mRowView.setMinColumnHeight((int)context.getResources().getDimension(dimen.realm_browser_database_list_item_min_height));
        mScrollMediator.addView(holder.mRowView);
        mWidthMediator.addView(holder.mRowView);
        holder.mRowView.setOnScrollChangedListener(mScrollMediator);
        return holder;
    }

    public void onBindViewHolder(final DatabaseClassAdapter.ItemViewHolder holder, int position) {
        RealmObject obj = (RealmObject)mData.get(position);
        if(mFields.size() != holder.mRowView.getColumnsNumber()) {
            holder.mRowView.setColumnsNumber(mFields.size());
        }

        Context c = holder.mRowView.getContext();
        int backColorId = position % 2 == 0?android.R.color.white: R.color.realm_browser_lt_gray;

        for(int i = 0; i < mFields.size(); ++i) {
            Field field = (Field)mFields.get(i);
            String value = RealmUtils.getFieldDisplayedName(obj, field);
            holder.mRowView.setColumnText(value, i);
            holder.mRowView.setColumnWidth(mWidthMediator.getColWidth(i), i);
        }

        holder.mRowView.post(new Runnable() {
            public void run() {
                holder.mRowView.scrollTo(DatabaseClassAdapter.this.mScrollMediator.getScrollX(), DatabaseClassAdapter.this.mScrollMediator.getScrollY());
            }
        });
        holder.mRowView.setBackgroundColor(c.getResources().getColor(backColorId));
        holder.mRowView.setTag(Integer.valueOf(position));
        holder.mRowView.setOnCellClickListener(this.mInternalCellClickListener);
        holder.mRowNumber.setText(Integer.toString(position));
    }

    public int getItemCount() {
        return mData.size();
    }

    void setScrollMediator(HorizontalScrollMediator mediator) {
        mScrollMediator = mediator;
    }

    void setColumnWidthMediator(ColumnWidthMediator mediator) {
        mWidthMediator = mediator;
    }

    void setCellClickListener(DatabaseClassAdapter.OnCellClickListener listener) {
        mExternalCellClickListener = listener;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView mRowNumber;
        RowView mRowView;

        public ItemViewHolder(View itemView, int colNumber) {
            super(itemView);
            mRowNumber = (TextView)itemView.findViewById(id.db_class_list_row_number);
            mRowView = (RowView)itemView.findViewById(id.db_class_list_row);
            mRowView.setColumnsNumber(colNumber);
        }
    }

    interface OnCellClickListener {
        void onCellClick(RealmObject var1, Field var2, int var3);
    }
}
