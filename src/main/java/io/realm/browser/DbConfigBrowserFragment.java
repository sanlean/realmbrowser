package io.realm.browser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.realm.browser.R.id;
import io.realm.browser.R.layout;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmResults;

public class DbConfigBrowserFragment extends Fragment {
    private static final String SAVED_STATE_SELECTED_FILE_POS = "selected file position";
    private Spinner mFileNameSpinner;
    private TextView mFileNameText;
    private TextView mFileSize;
    private TextView mFilePath;
    private View mFillDataBtn;
    private RecyclerView mClassList;
    private ClassListAdapter mClassListAdapter;
    private List<RealmConfiguration> mRealmConfigurations;
    List<Class<? extends RealmObject>> mClasses;
    private int mSelectedFilePosition = 0;
    private DbConfigBrowserFragment.DbConfigInteraction mListener;
    private final OnItemSelectedListener mSpinnerSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            DbConfigBrowserFragment.this.mSelectedFilePosition = position;
            DbConfigBrowserFragment.this.updateUIData();
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    };
    private final ClassListAdapter.OnItemClickListener mListItemClickListener = new ClassListAdapter.OnItemClickListener() {
        public void onItemClick(View v, int position) {
            Class clazz = (Class)DbConfigBrowserFragment.this.mClasses.get(position);
            DbConfigBrowserFragment.this.mListener.onClassSelected(clazz);
        }
    };

    public DbConfigBrowserFragment() {
    }

    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            this.mListener = (DbConfigInteraction)context;
        } catch (ClassCastException var3) {
            throw new IllegalArgumentException("Context " + context == null?"null":context.toString() + "should implement " + DbConfigBrowserFragment.DbConfigInteraction.class.getSimpleName() + " interface!");
        }
    }

    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(layout.realm_browser_fragment_db_config, container);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        this.findViews(view);
        this.setListeners();
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState != null) {
            this.mSelectedFilePosition = savedInstanceState.getInt("selected file position");
        }

        this.mRealmConfigurations = RealmBrowser.getInstance().getActiveRealmConfigs();
        this.initUI();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected file position", this.mSelectedFilePosition);
    }

    private void findViews(View view) {
        this.mFillDataBtn = view.findViewById(id.fill_btn);
        this.mClassList = (RecyclerView)view.findViewById(id.class_list);
        this.mFileNameSpinner = (Spinner)view.findViewById(id.file_name_spinner);
        this.mFileNameText = (TextView)view.findViewById(id.file_name);
        this.mFileSize = (TextView)view.findViewById(id.file_size);
        this.mFilePath = (TextView)view.findViewById(id.file_path);
    }

    private void setListeners() {
        this.mFillDataBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                RealmConfiguration config = (RealmConfiguration)DbConfigBrowserFragment.this.mRealmConfigurations.get(DbConfigBrowserFragment.this.mSelectedFilePosition);
                Realm realm = Realm.getInstance(config);
                Iterator i$ = DbConfigBrowserFragment.this.mClasses.iterator();

                Class clazz;
                while(i$.hasNext()) {
                    clazz = (Class)i$.next();
                    RealmUtils.clearClassData(realm, clazz);
                }

                i$ = DbConfigBrowserFragment.this.mClasses.iterator();

                while(i$.hasNext()) {
                    clazz = (Class)i$.next();
                    RealmResults rows = realm.where(clazz).findAll();
                    if(rows.size() < 50) {
                        RealmUtils.generateData(realm, clazz, 50);
                    }
                }

                realm.close();
                DbConfigBrowserFragment.this.updateUIData();
            }
        });
    }

    private void initUI() {
        ArrayList names = new ArrayList(this.mRealmConfigurations.size());
        Iterator adapter = this.mRealmConfigurations.iterator();

        while(adapter.hasNext()) {
            RealmConfiguration realmConfiguration = (RealmConfiguration)adapter.next();
            File f = new File(realmConfiguration.getPath());
            names.add(f.getName());
        }

        if(names.size() > 1) {
            this.mFileNameSpinner.setVisibility(View.VISIBLE);
            this.mFileNameText.setVisibility(View.GONE);
            ArrayAdapter adapter1 = new ArrayAdapter(this.getContext(), layout.realm_browser_class_list_file_spinner, names);
            this.mFileNameSpinner.setAdapter(adapter1);
            this.mFileNameSpinner.setOnItemSelectedListener(this.mSpinnerSelectedListener);
            this.mFileNameSpinner.setSelection(this.mSelectedFilePosition);
        } else if(names.size() > 0) {
            this.mFileNameSpinner.setVisibility(View.GONE);
            this.mFileNameText.setVisibility(View.VISIBLE);
            this.mFileNameText.setText((CharSequence)names.get(0));
            this.mSelectedFilePosition = 0;
            this.updateUIData();
        }

        this.mClassList.setLayoutManager(new LinearLayoutManager(this.getContext()));
    }

    private void updateUIData() {
        RealmConfiguration realmConfiguration = (RealmConfiguration)this.mRealmConfigurations.get(this.mSelectedFilePosition);
        File file = new File(realmConfiguration.getPath());
        this.mFilePath.setText(file.getParent());
        this.mFileSize.setText(Long.toString(file.length() / 1024L) + " Kb");
        this.mClasses = RealmBrowser.getInstance().getDisplayedRealmObjects(realmConfiguration);
        if(this.mClassListAdapter == null) {
            this.mClassListAdapter = new ClassListAdapter(this.mClasses, this.mListItemClickListener);
            this.mClassList.setAdapter(this.mClassListAdapter);
        } else {
            this.mClassListAdapter.updateData(this.mClasses);
            this.mClassListAdapter.notifyDataSetChanged();
        }

    }

    interface DbConfigInteraction {
        void onClassSelected(Class<? extends RealmObject> var1);
    }
}
