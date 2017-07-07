package io.realm.browser;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.MenuItemCompat.OnActionExpandListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.browser.R.id;
import io.realm.browser.R.layout;
import io.realm.browser.R.string;
import io.realm.browser.R.style;

public class DbTableFragment extends Fragment implements DatabaseClassAdapter.OnCellClickListener, EditDialogFragment.OnFieldEditDialogInteraction, FieldFilterDialogFragment.FieldFilterDialogInteraction, IOnBreadCrumbListener, ColumnWidthMediator.ColumnWidthProvider {
    private static final String LOG_TAG = DbTableFragment.class.getSimpleName();
    private BreadCrumbsView mCrumbsList;
    private RecyclerView mList;
    private DatabaseClassAdapter mAdapter;
    private RowView mTableHeader;
    private RowView mTableHeaderType;
    private View mHintGroup;
    private DragOverlayView mDragView;
    private MenuItem mSearchItem;
    private ActionBar mActionBar;
    private Class<? extends RealmObject> mClazz;
    private DbTableFragment.SpanHolder mSpanHolder = new DbTableFragment.SpanHolder();
    private HorizontalScrollMediator mScrollMediator;
    private ColumnWidthMediator mColWidthMediator;
    private DbTableFragment.DbTableInteraction mListener;
    private List<RealmObject> mOriginalData;
    private OnActionExpandListener mSearchExpandListener = new OnActionExpandListener() {
        public boolean onMenuItemActionExpand(MenuItem item) {
            return true;
        }

        public boolean onMenuItemActionCollapse(MenuItem item) {
            DbTableFragment.this.setSearchHintVisible(false);
            DbTableFragment.this.mAdapter.updateData(DbTableFragment.this.mOriginalData);
            return true;
        }
    };
    private OnQueryTextListener mSearchListener = new OnQueryTextListener() {
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        public boolean onQueryTextChange(String newText) {
            Log.d(DbTableFragment.LOG_TAG, "onQueryTextChange: " + newText);
            if(!TextUtils.isEmpty(newText)) {
                DbTableFragment.this.makeSearchRequest(newText);
            }

            return false;
        }
    };

    public DbTableFragment() {
    }

    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.mListener = (DbTableFragment.DbTableInteraction)context;
        } catch (ClassCastException var3) {
            throw new IllegalArgumentException("Context " + context == null?"null":context.toString() + "should implement " + DbTableFragment.DbTableInteraction.class.getSimpleName() + " interface!");
        }
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(layout.realm_browser_fragment_db_table, container);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.findViews(view);
        this.init();
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mActionBar = ((AppCompatActivity)this.getActivity()).getSupportActionBar();
        List states = this.mListener.getNavigationStates();
        if(states != null) {
            this.mCrumbsList.setCrumbStates(states);
        }

    }

    private void findViews(View view) {
        this.mTableHeader = (RowView)view.findViewById(id.table_header);
        this.mTableHeaderType = (RowView)view.findViewById(id.table_header_type);
        this.mList = (RecyclerView)view.findViewById(id.databaseList);
        this.mHintGroup = view.findViewById(id.invalid_request_hint_group);
        this.mCrumbsList = (BreadCrumbsView)view.findViewById(id.crumbs_list);
        this.mDragView = (DragOverlayView)view.findViewById(id.drag_view);
    }

    private void init() {
        this.mList.setLayoutManager(new LinearLayoutManager(this.getContext()));
        this.mCrumbsList.setOnCrumbClickListener(this);
        this.mColWidthMediator = new ColumnWidthMediator(this.mDragView, this);
        this.mColWidthMediator.addView(this.mTableHeader);
        this.mColWidthMediator.addView(this.mTableHeaderType);
        this.mTableHeader.setOnColumnWidthChangeListener(this.mColWidthMediator);
        this.mTableHeaderType.setOnColumnWidthChangeListener(this.mColWidthMediator);
        this.mScrollMediator = new HorizontalScrollMediator();
        this.mScrollMediator.addView(this.mTableHeader);
        this.mScrollMediator.addView(this.mTableHeaderType);
    }

    public void setClass(Class<? extends RealmObject> clazz) {
        fillTable(clazz, true);
        mCrumbsList.addCrumb(new StateHolder(clazz.getCanonicalName(), (RealmObject)null, (Field)null));
    }

    private void fillTable(Class<? extends RealmObject> clazz, boolean resetBreadcrumbs) {
        this.mClazz = clazz;
        if(resetBreadcrumbs) {
            this.mCrumbsList.clearCrumbs();
        }
        mOriginalData = new ArrayList<>();
        for (RealmObject realmObject : this.mListener.getRealm().where(this.mClazz).findAll())
            mOriginalData.add(realmObject);
        updateData(this.mOriginalData);
    }

    private void fillTable(RealmObject realmObj, Field field) {
        if(RealmUtils.isFieldRealmList(field)) {
            ParameterizedType pType = (ParameterizedType)field.getGenericType();
            this.mClazz = (Class)pType.getActualTypeArguments()[0];
            this.mOriginalData = RealmUtils.getRealmListFieldValue(realmObj, field);
        } else {
            if(!RealmUtils.isFieldRealmObject(field)) {
                throw new IllegalArgumentException("Unsupported field type: " + field);
            }

            this.mClazz = (Class<? extends RealmObject>) field.getType();
            this.mOriginalData = new ArrayList(1);
            this.mOriginalData.add(RealmUtils.getRealmObjectFieldValue(realmObj, field));
        }

        updateData(this.mOriginalData);
    }

    private void updateData(List<? extends RealmObject> data) {
        this.initTableHeader(this.mClazz);
        if(mAdapter == null) {
            if(data == null) {
                data = this.mListener.getRealm().where(this.mClazz).findAll();
                mAdapter = new DatabaseClassAdapter(this.getContext(), this.mClazz, (List)data);
            } else {
                mAdapter = new DatabaseClassAdapter(this.getContext(), this.mClazz, (List)data);
            }

            mAdapter.setScrollMediator(mScrollMediator);
            mAdapter.setColumnWidthMediator(mColWidthMediator);
            mAdapter.setCellClickListener(this);
            mList.setAdapter(mAdapter);
            mList.getRecycledViewPool().setMaxRecycledViews(0, 40);
        }

        if(data != null) {
            mAdapter.updateDisplayedFields(getContext(), mClazz, (List)data);
        } else {
            mAdapter.updateDisplayedFields(getContext(), mClazz);
        }

        if(mActionBar != null) {
            mActionBar.setTitle(String.format("%s (%d)", new Object[]{mClazz.getSimpleName(), Integer.valueOf(mAdapter.getItemCount())}));
        }
    }

    public void onStateChanged(StateHolder state) {
        if(state.getObject() != null && state.getField() != null) {
            fillTable(state.getObject(), state.getField());
        } else if(state.getCaption() != null) {
            try {
                fillTable((Class<? extends RealmObject>) Class.forName(state.getCaption()), false);
            } catch (ClassNotFoundException var3) {
                var3.printStackTrace();
            }
        }

    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.realm_browser_database_class_menu, menu);
        mSearchItem = menu.findItem(id.database_search);
        SearchView searchView = (SearchView)mSearchItem.getActionView();
        searchView.setOnQueryTextListener(mSearchListener);
        searchView.setQueryHint(getString(string.realm_browser_search_hint_short));
        View searchButton = searchView.findViewById(id.search_close_btn);
        if(searchButton != null) {
            searchButton.setEnabled(false);
            searchButton.setAlpha(0.0F);
        }

        MenuItemCompat.setOnActionExpandListener(mSearchItem, mSearchExpandListener);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if(itemId == id.field_filter) {
            FieldFilterDialogFragment.createInstance(mClazz).show(getChildFragmentManager(), FieldFilterDialogFragment.class.getSimpleName());
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListener.saveNavigationStates(mCrumbsList.getCrumbStates());
    }

    private void initTableHeader(Class<? extends RealmObject> clazz) {
        ArrayList fields = new ArrayList();
        FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(getContext());
        Iterator fieldCount = RealmUtils.getFields(clazz).iterator();

        while(fieldCount.hasNext()) {
            Field i = (Field)fieldCount.next();
            if(prefs.isFieldDisplayed(clazz, i)) {
                fields.add(i);
            }
        }

        mTableHeader.setColumnsNumber(fields.size());
        mTableHeader.setCellsGravity(80);
        mTableHeader.showDividers(true);
        mTableHeaderType.setColumnsNumber(fields.size());
        mTableHeaderType.setTextAppearance(style.realm_browser_database_column_title_type_style);
        mTableHeaderType.showDividers(true);
        int var9 = fields.size();

        for(int var10 = 0; var10 < var9; ++var10) {
            Field f = (Field)fields.get(var10);
            SpannableStringBuilder headerNameBuilder = new SpannableStringBuilder();
            addRealmAnnotations(f, headerNameBuilder);
            int start = headerNameBuilder.length();
            headerNameBuilder.append(f.getName());
            headerNameBuilder.setSpan(mSpanHolder.getHeaderStyle(getContext()), start, headerNameBuilder.length(), 33);
            mTableHeader.setColumnText(headerNameBuilder.subSequence(0, headerNameBuilder.length()), var10);
            mTableHeaderType.setColumnText(f.getType().getSimpleName(), var10);
        }

    }

    public void onCellClick(RealmObject obj, Field field, int position) {
        Class clazz;
        if(RealmUtils.isFieldRealmObject(field)) {
            clazz = field.getType();
        } else if(RealmUtils.isFieldRealmList(field)) {
            ParameterizedType fieldRealmConfig = (ParameterizedType)field.getGenericType();
            clazz = (Class)fieldRealmConfig.getActualTypeArguments()[0];
        } else {
            clazz = null;
        }

        if(!RealmUtils.isFieldRealmObject(field) || RealmUtils.getRealmObjectFieldValue(obj, field) != null) {
            MenuItemCompat.collapseActionView(mSearchItem);
            RealmConfiguration fieldRealmConfig1 = RealmBrowser.getInstance().getRealmConfig(clazz);
            Class objClass = obj.getClass().getSuperclass();
            RealmConfiguration objRealmConfig = RealmBrowser.getInstance().getRealmConfig(objClass);
            if(clazz != null && (fieldRealmConfig1 == null || !fieldRealmConfig1.getPath().equals(objRealmConfig.getPath()))) {
                if(fieldRealmConfig1 == null) {
                    showInvalidClassInfoDialog(clazz);
                } else {
                    showInvalidFileInfoDialog(clazz, fieldRealmConfig1);
                }
            } else if(!RealmUtils.isFieldRealmList(field) && !RealmUtils.isFieldRealmObject(field) && field.getType() != byte[].class && field.getType() != Byte[].class) {
                EditDialogFragment.createInstance(obj, field, position).show(getChildFragmentManager(), EditDialogFragment.class.getSimpleName());
            } else if (RealmUtils.isFieldRealmList(field) || RealmUtils.isFieldRealmObject(field)){
                fillTable(obj, field);
                mCrumbsList.addCrumb(new StateHolder(mClazz.getCanonicalName(), obj, field));
            }

        }
    }

    private void showInvalidClassInfoDialog(Class<? extends RealmObject> clazz) {
        String messagePattern = getResources().getString(string.realm_browser_realm_class_notification);
        (new Builder(getContext())).setMessage(String.format(messagePattern, new Object[]{clazz.getSimpleName()})).setPositiveButton(string.realm_browser_ok, (OnClickListener)null).show();
    }

    private void showInvalidFileInfoDialog(Class<? extends RealmObject> clazz, RealmConfiguration fieldRealmConfig) {
        String messagePattern = getResources().getString(string.realm_browser_realm_file_notification);
        String fileName = (new File(fieldRealmConfig.getPath())).getName();
        (new Builder(getContext())).setMessage(String.format(messagePattern, new Object[]{clazz.getSimpleName(), fileName})).setPositiveButton(string.realm_browser_ok, (OnClickListener)null).show();
    }

    private void addRealmAnnotations(Field f, SpannableStringBuilder builder) {
        ArrayList classes = new ArrayList(3);
        classes.add(PrimaryKey.class);
        classes.add(Ignore.class);
        classes.add(Index.class);
        Iterator i$ = classes.iterator();

        while(i$.hasNext()) {
            Class c = (Class)i$.next();
            if(f.isAnnotationPresent(c)) {
                addAnnotationName(builder, f.getAnnotation(c));
            }
        }

    }

    private void addAnnotationName(SpannableStringBuilder builder, Annotation a) {
        int start = builder.length();
        builder.append("@");
        builder.append(a.annotationType().getSimpleName());
        builder.setSpan(mSpanHolder.getHeaderAnnotationStyle(getContext()), start, builder.length(), 33);
        builder.append(System.getProperty("line.separator"));
    }

    public void onRowWasEdit(int position) {
        Adapter adapter = mList.getAdapter();
        if(adapter != null) {
            adapter.notifyItemChanged(position);
        }

    }

    private void makeSearchRequest(@NonNull String query) {
        int separatorIndex = query.indexOf(58);
        if(separatorIndex != -1) {
            String fieldName = query.substring(0, separatorIndex).trim();
            String value = query.length() - 1 > separatorIndex?query.substring(separatorIndex + 1, query.length()).trim():"";
            Log.d(LOG_TAG, "fieldName: " + fieldName + "; value: " + value);

            try {
                Field e = mClazz.getDeclaredField(fieldName);
                RealmQuery realmQuery;
                if(mOriginalData instanceof RealmResults) {
                    realmQuery = ((RealmResults)mOriginalData).where();
                } else {
                    if(!(mOriginalData instanceof RealmList)) {
                        processInvalidQuery(query);
                        return;
                    }

                    realmQuery = ((RealmList)mOriginalData).where();
                }

                Class type = e.getType();
                if(type == String.class) {
                    realmQuery.contains(fieldName, value);
                } else if(type != Boolean.class && type != Boolean.TYPE) {
                    if(type != Short.class && type != Short.TYPE) {
                        if(type != Integer.class && type != Integer.TYPE) {
                            if(type != Long.class && type != Long.TYPE) {
                                if(type != Float.class && type != Float.TYPE) {
                                    if(type != Double.class && type != Double.TYPE) {
                                        if(type == Date.class) {
                                            processInvalidQuery(query);
                                            return;
                                        }

                                        if(type != Byte[].class && type != byte[].class) {
                                            if(RealmObject.class.isAssignableFrom(type)) {
                                                processInvalidQuery(query);
                                                return;
                                            }

                                            if(RealmList.class.isAssignableFrom(type)) {
                                                processInvalidQuery(query);
                                                return;
                                            }
                                            Toast.makeText(getContext(), "Invalid value type:" + type, Toast.LENGTH_SHORT).show();
                                            processInvalidQuery(query);
                                            return;
                                        }

                                        processInvalidQuery(query);
                                        return;
                                    }

                                    try {
                                        realmQuery.equalTo(fieldName, Double.valueOf(value));
                                    } catch (NumberFormatException var9) {
                                        processInvalidQuery(query);
                                        return;
                                    }
                                } else {
                                    try {
                                        realmQuery.equalTo(fieldName, Float.valueOf(value));
                                    } catch (NumberFormatException var10) {
                                        processInvalidQuery(query);
                                        return;
                                    }
                                }
                            } else {
                                try {
                                    realmQuery.equalTo(fieldName, Long.valueOf(value));
                                } catch (NumberFormatException var11) {
                                    processInvalidQuery(query);
                                    return;
                                }
                            }
                        } else {
                            try {
                                realmQuery.equalTo(fieldName, Integer.valueOf(value));
                            } catch (NumberFormatException var12) {
                                processInvalidQuery(query);
                                return;
                            }
                        }
                    } else {
                        try {
                            realmQuery.equalTo(fieldName, Short.valueOf(value));
                        } catch (NumberFormatException var13) {
                            processInvalidQuery(query);
                            return;
                        }
                    }
                } else {
                    boolean result;
                    if("true".equalsIgnoreCase(value)) {
                        result = true;
                    } else {
                        if(!"false".equalsIgnoreCase(value)) {
                            processInvalidQuery(query);
                            return;
                        }

                        result = false;
                    }

                    realmQuery.equalTo(fieldName, Boolean.valueOf(result));
                }

                RealmResults result1 = realmQuery.findAll();
                setSearchHintVisible(false);
                mAdapter.updateData(result1);
            } catch (NoSuchFieldException var14) {
                processInvalidQuery(query);
            }
        } else {
            processInvalidQuery(query);
        }

    }

    private void processInvalidQuery(@NonNull String query) {
        Log.d(LOG_TAG, "processInvalidQuery: " + query);
        setSearchHintVisible(true);
    }

    private void setSearchHintVisible(boolean isHintVisible) {
        mList.setVisibility(isHintVisible?View.GONE:View.VISIBLE);
        mHintGroup.setVisibility(isHintVisible?View.VISIBLE:View.GONE);
    }

    public void onFieldListChange() {
        initTableHeader(mClazz);
        mAdapter.updateDisplayedFields(getContext(), mClazz);
    }

    public int getColumnWidth(int position) {
        return mTableHeader.getColumnWidth(position);
    }

    private class SpanHolder {
        private TextAppearanceSpan headerAnnotationStyle;
        private TextAppearanceSpan headerStyle;

        private SpanHolder() {
        }

        public TextAppearanceSpan getHeaderAnnotationStyle(Context c) {
            if(this.headerAnnotationStyle == null) {
                this.headerAnnotationStyle = new TextAppearanceSpan(c, style.realm_browser_database_column_title_annotation_style);
            }

            return this.headerAnnotationStyle;
        }

        public TextAppearanceSpan getHeaderStyle(Context c) {
            if(this.headerStyle == null) {
                this.headerStyle = new TextAppearanceSpan(c, style.realm_browser_database_column_title_style);
            }

            return this.headerStyle;
        }
    }

    interface DbTableInteraction {
        List<StateHolder> getNavigationStates();

        void saveNavigationStates(List<StateHolder> var1);

        Realm getRealm();
    }
}
