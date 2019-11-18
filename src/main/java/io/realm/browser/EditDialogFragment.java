package io.realm.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import io.realm.browser.R.id;
import io.realm.browser.R.layout;
import io.realm.browser.R.string;
import io.realm.Realm;
import io.realm.RealmObject;

public class EditDialogFragment extends DialogFragment {
    private static final String ARG_POSITION = "ream object position";
    private RealmObject mRealmObject;
    private Field mField;
    private int mPosition;
    private EditDialogFragment.OnFieldEditDialogInteraction mListener;
    private EditText mEditText;
    private TextView mErrorText;
    private TabHost mTabHost;
    private DatePicker mDatePicker;
    private TimePicker mTimePicker;
    private RadioGroup mRadioGroup;
    private TextView mByteTextView;
    private final OnClickListener mResetToNullClickListener = new OnClickListener() {
        public void onClick(View v) {
            EditDialogFragment.this.saveNewValue((Object)null);
            EditDialogFragment.this.mListener.onRowWasEdit(EditDialogFragment.this.mPosition);
            EditDialogFragment.this.dismiss();
        }
    };
    private final OnClickListener mOkClickListener = new OnClickListener() {
        public void onClick(View view) {
            Class type = EditDialogFragment.this.mField.getType();
            Object newValue;
            if(type == String.class) {
                newValue = EditDialogFragment.this.mEditText.getText().toString();
            } else if(type != Boolean.class && type != Boolean.TYPE) {
                if(type != Short.class && type != Short.TYPE) {
                    if(type != Integer.class && type != Integer.TYPE) {
                        if(type != Long.class && type != Long.TYPE) {
                            if(type != Float.class && type != Float.TYPE) {
                                if(type != Double.class && type != Double.TYPE) {
                                    if(type == Date.class) {
                                        Class objClass = EditDialogFragment.this.mRealmObject.getClass().getSuperclass();
                                        Realm realm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(objClass));
                                        Date currentValue = (Date)RealmUtils.getNotParamFieldValue(EditDialogFragment.this.mRealmObject, EditDialogFragment.this.mField);
                                        realm.close();
                                        Calendar calendar = Calendar.getInstance();
                                        if(currentValue != null) {
                                            calendar.setTime(currentValue);
                                        }

                                        calendar.set(Calendar.YEAR, EditDialogFragment.this.mDatePicker.getYear());
                                        calendar.set(Calendar.MONTH, EditDialogFragment.this.mDatePicker.getMonth());
                                        calendar.set(Calendar.DAY_OF_MONTH, EditDialogFragment.this.mDatePicker.getDayOfMonth());
                                        calendar.set(Calendar.HOUR_OF_DAY, EditDialogFragment.this.mTimePicker.getCurrentHour().intValue());
                                        calendar.set(Calendar.MINUTE, EditDialogFragment.this.mTimePicker.getCurrentMinute().intValue());
                                        newValue = calendar.getTime();
                                    } else {
                                        if(type == Byte[].class || type == byte[].class) {
                                            EditDialogFragment.this.dismiss();
                                            return;
                                        }

                                        newValue = null;
                                    }
                                } else {
                                    try {
                                        newValue = Double.valueOf(EditDialogFragment.this.mEditText.getText().toString());
                                    } catch (NumberFormatException var8) {
                                        var8.printStackTrace();
                                        newValue = null;
                                    }
                                }
                            } else {
                                try {
                                    newValue = Float.valueOf(EditDialogFragment.this.mEditText.getText().toString());
                                } catch (NumberFormatException var9) {
                                    var9.printStackTrace();
                                    newValue = null;
                                }
                            }
                        } else {
                            try {
                                newValue = Long.valueOf(EditDialogFragment.this.mEditText.getText().toString());
                            } catch (NumberFormatException var10) {
                                var10.printStackTrace();
                                newValue = null;
                            }
                        }
                    } else {
                        try {
                            newValue = Integer.valueOf(EditDialogFragment.this.mEditText.getText().toString());
                        } catch (NumberFormatException var11) {
                            var11.printStackTrace();
                            newValue = null;
                        }
                    }
                } else {
                    try {
                        newValue = Short.valueOf(EditDialogFragment.this.mEditText.getText().toString());
                    } catch (NumberFormatException var12) {
                        var12.printStackTrace();
                        newValue = null;
                    }
                }
            } else {
                newValue = Boolean.valueOf(EditDialogFragment.this.mRadioGroup.getCheckedRadioButtonId() == id.edit_boolean_true);
            }

            if(newValue != null) {
                EditDialogFragment.this.saveNewValue(newValue);
                EditDialogFragment.this.mListener.onRowWasEdit(EditDialogFragment.this.mPosition);
                EditDialogFragment.this.dismiss();
            } else {
                EditDialogFragment.this.showError(type);
            }

        }
    };
    private final android.content.DialogInterface.OnClickListener mCancelClickListener = new android.content.DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
    };

    public EditDialogFragment() {
    }

    public static EditDialogFragment createInstance(RealmObject obj, Field field, int position) {
        RealmObjectHolder realmObjectHolder = RealmObjectHolder.getInstance();
        realmObjectHolder.setObject(obj);
        realmObjectHolder.setField(field);
        Bundle args = new Bundle();
        args.putInt("ream object position", position);
        EditDialogFragment fragment = new EditDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mListener = (EditDialogFragment.OnFieldEditDialogInteraction)activity;
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.mRealmObject = RealmObjectHolder.getInstance().getObject();
        this.mField = RealmObjectHolder.getInstance().getField();
        this.mPosition = this.getArguments().getInt("ream object position");
        if(this.mRealmObject != null && this.mField != null) {
            int layoutId = -1;
            Class type = this.mField.getType();
            if(type != String.class && type != Short.class && type != Short.TYPE && type != Integer.class && type != Integer.TYPE && type != Long.class && type != Long.TYPE && type != Float.class && type != Float.TYPE && type != Double.class && type != Double.TYPE) {
                if(type != Boolean.class && type != Boolean.TYPE) {
                    if(type == Date.class) {
                        layoutId = R.layout.realm_browser_date_edit_layout;
                    } else if(type == Byte[].class || type == byte[].class) {
                        layoutId = layout.realm_browser_byte_array_edit_layout;
                    }
                } else {
                    layoutId = layout.realm_browser_boolean_edit_layout;
                }
            } else {
                layoutId = layout.realm_browser_text_edit_layout;
            }

            LayoutInflater inflater = LayoutInflater.from(this.getActivity());
            View root = inflater.inflate(layoutId, (ViewGroup)null);
            this.findViews(root);
            this.initUI(this.mRealmObject, this.mField, type);
            Builder builder = new Builder(this.getActivity());
            if(layoutId == -1) {
                builder.setMessage("Unknown field type.");
            } else {
                builder.setView(root);
            }

            builder.setPositiveButton(string.realm_browser_ok, (android.content.DialogInterface.OnClickListener)null);
            if(type != Byte[].class && type != byte[].class) {
                builder.setNegativeButton(string.realm_browser_cancel, this.mCancelClickListener);
            }

            if(this.isTypeNullable(type).booleanValue()) {
                builder.setNeutralButton(string.realm_browser_reset_to_null, (android.content.DialogInterface.OnClickListener)null);
            }

            AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    Button positiveButton = ((AlertDialog)dialog).getButton(-1);
                    positiveButton.setOnClickListener(EditDialogFragment.this.mOkClickListener);
                    Button resetToNull = ((AlertDialog)dialog).getButton(-3);
                    if(resetToNull != null) {
                        resetToNull.setOnClickListener(EditDialogFragment.this.mResetToNullClickListener);
                    }

                }
            });
            return dialog;
        } else {
            throw new IllegalArgumentException("Use RealmObjectHolder to store data");
        }
    }

    private void findViews(View root) {
        this.mEditText = (EditText)root.findViewById(id.text_edit_dialog);
        this.mErrorText = (TextView)root.findViewById(id.error_message);
        this.mRadioGroup = (RadioGroup)root.findViewById(id.edit_boolean_group);
        this.mTabHost = (TabHost)root.findViewById(id.tabHost);
        this.mDatePicker = (DatePicker)root.findViewById(id.tab_date);
        this.mTimePicker = (TimePicker)root.findViewById(id.tab_time);
        this.mByteTextView = (TextView)root.findViewById(id.array);
    }

    private void initUI(RealmObject obj, Field field, Class<?> type) {
        if(type != String.class && type != Short.class && type != Short.TYPE && type != Integer.class && type != Integer.TYPE && type != Long.class && type != Long.TYPE && type != Float.class && type != Float.TYPE && type != Double.class && type != Double.TYPE) {
            if(type != Boolean.class && type != Boolean.TYPE) {
                if(type == Date.class) {
                    this.mTabHost.setup();
                    TabSpec var10 = this.mTabHost.newTabSpec("Date");
                    var10.setIndicator("Date");
                    var10.setContent(id.tab_date);
                    this.mTabHost.addTab(var10);
                    TabSpec var15 = this.mTabHost.newTabSpec("Time");
                    var15.setIndicator("Time");
                    var15.setContent(id.tab_time);
                    this.mTabHost.addTab(var15);
                    Date len$ = (Date)RealmUtils.getNotParamFieldValue(obj, field);
                    Calendar i$ = Calendar.getInstance();
                    i$.setTime(len$ != null?len$:new Date());
                    this.mDatePicker.updateDate(i$.get(Calendar.YEAR), i$.get(Calendar.MONTH), i$.get(Calendar.DAY_OF_MONTH));
                    this.mTimePicker.setCurrentHour(Integer.valueOf(i$.get(Calendar.HOUR)));
                    this.mTimePicker.setCurrentMinute(Integer.valueOf(i$.get(Calendar.MINUTE)));
                    this.mTimePicker.setIs24HourView(Boolean.valueOf(true));
                } else if(type == Byte[].class || type == byte[].class) {
                    byte[] var11 = (byte[])((byte[])RealmUtils.getNotParamFieldValue(obj, field));
                    if(var11 == null) {
                        this.mByteTextView.setText(string.realm_browser_byte_array_is_null);
                    } else {
                        byte[] var16 = var11;
                        int var13 = var11.length;

                        for(int var14 = 0; var14 < var13; ++var14) {
                            byte b = var16[var14];
                            this.mByteTextView.append(String.format("0x%02X", new Object[]{Byte.valueOf(b)}) + " ");
                        }
                    }
                }
            } else {
                Boolean var9 = (Boolean)RealmUtils.getNotParamFieldValue(obj, field);
                int var12;
                if(var9 == null) {
                    var12 = -1;
                } else if(var9.booleanValue()) {
                    var12 = id.edit_boolean_true;
                } else {
                    var12 = id.edit_boolean_false;
                }

                if(var12 != -1) {
                    ((RadioButton)this.mRadioGroup.findViewById(var12)).setChecked(true);
                }
            }
        } else {
            Object valueObj = RealmUtils.getNotParamFieldValue(obj, field);
            this.mEditText.setText(valueObj == null?"":valueObj.toString());
            short arr$;
            if(type == String.class) {
                arr$ = 1;
            } else if(type != Float.class && type != Float.TYPE && type != Double.class && type != Double.TYPE) {
                arr$ = 4098;
            } else {
                arr$ = 12290;
            }

            this.mEditText.setInputType(arr$);
        }

    }

    private Boolean isTypeNullable(Class type) {
        return Boolean.valueOf(type == Date.class || type == Boolean.class || type == String.class || type == Short.class || type == Integer.class || type == Long.class || type == Float.class || type == Double.class);
    }

    private void saveNewValue(Object newValue) {
        Class objClass = this.mRealmObject.getClass().getSuperclass();
        Realm realm = Realm.getInstance(RealmBrowser.getInstance().getRealmConfig(objClass));
        realm.beginTransaction();
        RealmUtils.setNotParamFieldValue(this.mRealmObject, this.mField, newValue);
        realm.commitTransaction();
        realm.close();
    }

    private void showError(Class<?> clazz) {
        String notFormatted = this.getString(string.realm_browser_value_edit_error);
        String error = String.format(notFormatted, new Object[]{this.mEditText.getText().toString(), clazz.getSimpleName()});
        this.mErrorText.setText(error);
    }

    public interface OnFieldEditDialogInteraction {
        void onRowWasEdit(int var1);
    }
}
