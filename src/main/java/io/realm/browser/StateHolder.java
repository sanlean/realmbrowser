package io.realm.browser;

import java.lang.reflect.Field;

import io.realm.RealmObject;

public class StateHolder {
    private String caption;
    private RealmObject obj;
    private Field field;

    public StateHolder(String caption, RealmObject obj, Field field) {
        this.caption = caption;
        this.obj = obj;
        this.field = field;
    }

    public String getCaption() {
        return this.caption;
    }

    public Field getField() {
        return this.field;
    }

    public RealmObject getObject() {
        return this.obj;
    }
}
