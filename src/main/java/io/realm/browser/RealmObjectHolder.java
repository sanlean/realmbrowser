package io.realm.browser;

import java.lang.reflect.Field;

import io.realm.RealmObject;

public class RealmObjectHolder {
    private static RealmObjectHolder sInstance = new RealmObjectHolder();
    private RealmObject mObject;
    private Field mField;

    public static RealmObjectHolder getInstance() {
        return sInstance;
    }

    private RealmObjectHolder() {
    }

    public RealmObject getObject() {
        return this.mObject;
    }

    public void setObject(RealmObject object) {
        this.mObject = object;
    }

    public Field getField() {
        return this.mField;
    }

    public void setField(Field field) {
        this.mField = field;
    }
}
