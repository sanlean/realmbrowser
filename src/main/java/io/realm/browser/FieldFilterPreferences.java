package io.realm.browser;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;

public class FieldFilterPreferences {
    private static final String PREFS_FILE_NAME = "fieldsFilter.prefs";
    private static FieldFilterPreferences ourInstance;
    private SharedPreferences mPrefs;

    public static FieldFilterPreferences getInstance(Context context) {
        if(ourInstance == null) {
            ourInstance = new FieldFilterPreferences(context);
        }

        return ourInstance;
    }

    private FieldFilterPreferences(Context context) {
        this.mPrefs = context.getApplicationContext().getSharedPreferences("fieldsFilter.prefs", 0);
    }

    void setFieldDisplayed(@NonNull Class clazz, @NonNull Field field, boolean isFieldDisplayed) {
        Editor editor = this.mPrefs.edit();
        editor.putBoolean(this.getFieldPrefKey(clazz, field), isFieldDisplayed);
        editor.commit();
    }

    boolean isFieldDisplayed(@NonNull Class clazz, @NonNull Field field) {
        String key = this.getFieldPrefKey(clazz, field);
        return this.mPrefs.getBoolean(key, true);
    }

    private String getFieldPrefKey(@NonNull Class clazz, @NonNull Field field) {
        return clazz.getCanonicalName() + ":" + field.getName();
    }
}
