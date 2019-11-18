package io.realm.browser;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.browser.R.id;

public class BrowserActivity extends AppCompatActivity implements DbConfigBrowserFragment.DbConfigInteraction, DbTableFragment.DbTableInteraction, EditDialogFragment.OnFieldEditDialogInteraction, FieldFilterDialogFragment.FieldFilterDialogInteraction {
    private static String STATE_DRAWER_LOCKED = BrowserActivity.class.getSimpleName() + "_drawer_locked";
    private static String STATE_CURRENT_CLASS = BrowserActivity.class.getSimpleName() + "_ class_name";
    private Toolbar mToolbar;
    private DbTableFragment mDbTableFragment;
    private DrawerLayout mDrawer;
    private BrowserActivity.RetainFragment mRetainFragment;
    private boolean isDrawerLocked = true;
    private Realm mRealm;
    private Class<? extends RealmObject> mSelectedClass;

    public BrowserActivity() {
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, BrowserActivity.class);
        context.startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.realm_browser_browser_activity);
        this.mRetainFragment = this.getRetainFragment();
        if(this.mRetainFragment == null) {
            this.mRetainFragment = new BrowserActivity.RetainFragment();
            this.getSupportFragmentManager().beginTransaction().add(this.mRetainFragment, BrowserActivity.RetainFragment.TAG).commit();
        }

        this.mToolbar = (Toolbar)this.findViewById(id.toolbar);
        this.setSupportActionBar(this.mToolbar);
        this.mDbTableFragment = (DbTableFragment)this.getSupportFragmentManager().findFragmentById(id.table_fragment);
        if(savedInstanceState != null) {
            this.isDrawerLocked = savedInstanceState.getBoolean(STATE_DRAWER_LOCKED);
            if(this.getNavigationStates() == null) {
                this.isDrawerLocked = true;
            }

            String className = savedInstanceState.getString(STATE_CURRENT_CLASS);
            if(className != null) {
                try {
                    this.mSelectedClass = (Class<? extends RealmObject>) Class.forName(className);
                } catch (ClassNotFoundException var4) {
                    var4.printStackTrace();
                }

                this.mRealm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(this.mSelectedClass));
            }
        }

    }

    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mDrawer = (DrawerLayout)this.findViewById(id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, this.mDrawer, this.mToolbar, 0, 0);
        this.mDrawer.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        if(this.isDrawerLocked) {
            this.mDrawer.setDrawerLockMode(2);
        }

    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_DRAWER_LOCKED, this.isDrawerLocked);
        if(this.mSelectedClass != null) {
            outState.putString(STATE_CURRENT_CLASS, this.mSelectedClass.getCanonicalName());
        }

    }

    public void onClassSelected(Class<? extends RealmObject> clazz) {
        this.isDrawerLocked = false;
        this.mDrawer.setDrawerLockMode(0);
        this.mDrawer.closeDrawers();
        RealmConfiguration clazzConfig = RealmBrowser.getInstance().getRealmConfig(clazz);
        if(this.mRealm != null && !this.mRealm.getConfiguration().equals(clazzConfig)) {
            this.mRealm.close();
        }

        this.mRealm = Realm.getInstance(clazzConfig);
        this.mDbTableFragment.setClass(clazz);
        this.mSelectedClass = clazz;
    }

    public void onFieldListChange() {
        this.mDbTableFragment.onFieldListChange();
    }

    public void onRowWasEdit(int position) {
        this.mDbTableFragment.onRowWasEdit(position);
    }

    private BrowserActivity.RetainFragment getRetainFragment() {
        BrowserActivity.RetainFragment result = this.mRetainFragment != null?this.mRetainFragment:(BrowserActivity.RetainFragment)this.getSupportFragmentManager().findFragmentByTag(BrowserActivity.RetainFragment.TAG);
        this.mRetainFragment = result;
        return this.mRetainFragment;
    }

    public List<StateHolder> getNavigationStates() {
        return this.getRetainFragment().getNavigationStates();
    }

    public void saveNavigationStates(List<StateHolder> states) {
        this.getRetainFragment().setNavigationStates(states);
    }

    public Realm getRealm() {
        return this.mRealm;
    }

    public static class RetainFragment extends Fragment {
        static final String TAG = BrowserActivity.RetainFragment.class.getSimpleName() + "_tag";
        List<StateHolder> mNavigationStates;

        public RetainFragment() {
        }

        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setRetainInstance(true);
        }

        void setNavigationStates(List<StateHolder> states) {
            this.mNavigationStates = states;
        }

        List<StateHolder> getNavigationStates() {
            return this.mNavigationStates;
        }
    }
}
