package io.realm.browser;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import dalvik.system.DexFile;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmObject;
import io.realm.RealmSchema;

public class RealmBrowser {
    private static RealmBrowser sInstance = new RealmBrowser();
    private Map<Class<? extends RealmObject>, RealmConfiguration> displayedRealmConfigs = new HashMap();
    private List<RealmConfiguration> activeRealmConfigs = new ArrayList();

    static RealmBrowser getInstance() {
        return sInstance;
    }

    private RealmBrowser() {
    }

    List<Class<? extends RealmObject>> getDisplayedRealmObjects(RealmConfiguration realm) {
        ArrayList result = new ArrayList();
        Iterator i$ = this.displayedRealmConfigs.keySet().iterator();

        while(i$.hasNext()) {
            Class clazz = (Class)i$.next();
            RealmConfiguration config = (RealmConfiguration)this.displayedRealmConfigs.get(clazz);
            if(config.getPath().equals(realm.getPath())) {
                result.add(clazz);
            }
        }

        Collections.sort(result, new Comparator<Class<? extends RealmObject>>() {
            public int compare(Class<? extends RealmObject> lhs, Class<? extends RealmObject> rhs) {
                return lhs.getSimpleName().compareTo(rhs.getSimpleName());
            }
        });
        return result;
    }

    RealmConfiguration getRealmConfig(Class<? extends RealmObject> clazz) {
        return (RealmConfiguration)this.displayedRealmConfigs.get(clazz);
    }

    List<RealmConfiguration> getActiveRealmConfigs() {
        return this.activeRealmConfigs;
    }

    private void addRealmConfig(RealmConfiguration config) {
        if(this.activeRealmConfigs.isEmpty()) {
            this.activeRealmConfigs.add(config);
        } else {
            Iterator i$ = this.activeRealmConfigs.iterator();

            while(i$.hasNext()) {
                RealmConfiguration configuration = (RealmConfiguration)i$.next();
                if(configuration.getPath().equals(config.getPath())) {
                    return;
                }
            }

            this.activeRealmConfigs.add(config);
        }

    }

    public static class Builder {
        private Context mContext;
        private RealmBrowser mBrowser;

        public Builder(Context c) {
            this.mContext = c;
            this.mBrowser = RealmBrowser.sInstance;
        }

        public RealmBrowser.Builder add(Realm realm, List<Class<? extends RealmObject>> classes) {
            Iterator i$ = classes.iterator();

            while(i$.hasNext()) {
                Class clazz = (Class)i$.next();
                this.add(realm, clazz);
            }

            return this;
        }

        public RealmBrowser.Builder add(Realm realm, Class<? extends RealmObject> clazz) {
            this.add(realm.getConfiguration(), clazz);
            return this;
        }

        public RealmBrowser.Builder add(RealmConfiguration config, List<Class<? extends RealmObject>> classes) {
            Iterator i$ = classes.iterator();

            while(i$.hasNext()) {
                Class clazz = (Class)i$.next();
                this.add(config, clazz);
            }

            return this;
        }

        public RealmBrowser.Builder add(RealmConfiguration config, Class<? extends RealmObject> clazz) {
            this.mBrowser.displayedRealmConfigs.put(clazz, config);
            this.mBrowser.addRealmConfig(config);
            return this;
        }

        public void show() {
            BrowserActivity.startActivity(this.mContext);
        }

        public void showNotification() {
            RealmBrowserService.startService(this.mContext);
        }
    }
}
