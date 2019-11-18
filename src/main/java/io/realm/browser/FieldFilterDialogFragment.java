package io.realm.browser;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.reflect.Field;
import java.util.List;

import io.realm.RealmObject;

public class FieldFilterDialogFragment extends DialogFragment {
    private static final String ARG_CLASS_NAME = "canonical class name";
    private Class mClass;
    private List<Field> mFields;
    private boolean[] mCheckedItems = null;
    private FieldFilterDialogFragment.FieldFilterDialogInteraction mListener;
    private String mDeselectAllText;
    private String mSelectAllText;
    private final DialogInterface.OnMultiChoiceClickListener mChoiceListener = new DialogInterface.OnMultiChoiceClickListener() {
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            FieldFilterDialogFragment.this.mCheckedItems[which] = isChecked;
            FieldFilterDialogFragment.this.updateSelectionButtonText();
        }
    };
    private final DialogInterface.OnClickListener mOkButtonClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            FragmentActivity c = FieldFilterDialogFragment.this.getActivity();
            FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(c);

            for(int i = 0; i < FieldFilterDialogFragment.this.mCheckedItems.length; ++i) {
                prefs.setFieldDisplayed(FieldFilterDialogFragment.this.mClass, (Field)FieldFilterDialogFragment.this.mFields.get(i), FieldFilterDialogFragment.this.mCheckedItems[i]);
            }

            FieldFilterDialogFragment.this.mListener.onFieldListChange();
        }
    };
    private final DialogInterface.OnClickListener mCancelClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
    };
    private final android.view.View.OnClickListener mChangeSelectionClickListener = new android.view.View.OnClickListener() {
        public void onClick(View view) {
            Button btn = (Button)view;
            String btnText = btn.getText().toString();
            boolean newValue = !btnText.equals(FieldFilterDialogFragment.this.mDeselectAllText);
            AlertDialog dialog = (AlertDialog)FieldFilterDialogFragment.this.getDialog();
            ListView list = dialog.getListView();

            for(int i = 0; i < FieldFilterDialogFragment.this.mCheckedItems.length; ++i) {
                FieldFilterDialogFragment.this.mCheckedItems[i] = newValue;
                list.setItemChecked(i, newValue);
            }

            FieldFilterDialogFragment.this.updateSelectionButtonText();
        }
    };

    public FieldFilterDialogFragment() {
    }

    public static FieldFilterDialogFragment createInstance(Class<? extends RealmObject> clazz) {
        Bundle args = new Bundle();
        args.putString("canonical class name", clazz.getCanonicalName());
        FieldFilterDialogFragment fragment = new FieldFilterDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mListener = (FieldFilterDialogFragment.FieldFilterDialogInteraction)activity;
    }

    public void onDetach() {
        super.onDetach();
        this.mListener = null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDeselectAllText = this.getResources().getString(R.string.realm_browser_deselect_all);
        this.mSelectAllText = this.getResources().getString(R.string.realm_browser_select_all);
    }

    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String className = this.getArguments().getString("canonical class name");
        FieldFilterPreferences prefs = FieldFilterPreferences.getInstance(this.getActivity());
        CharSequence[] items = null;

        try {
            this.mClass = Class.forName(className);
            this.mFields = RealmUtils.getFields(this.mClass);
            items = new CharSequence[this.mFields.size()];
            this.mCheckedItems = new boolean[this.mFields.size()];

            for(int dialog = 0; dialog < this.mFields.size(); ++dialog) {
                Field f = (Field)this.mFields.get(dialog);
                items[dialog] = f.getName();
                this.mCheckedItems[dialog] = prefs.isFieldDisplayed(this.mClass, f);
            }
        } catch (ClassNotFoundException var7) {
            var7.printStackTrace();
        }

        AlertDialog var8 = (new AlertDialog.Builder(this.getActivity())).setTitle(R.string.realm_browser_field_filter_dialog_title).setMultiChoiceItems(items, this.mCheckedItems, this.mChoiceListener).setPositiveButton(R.string.realm_browser_ok, this.mOkButtonClickListener).setNegativeButton(R.string.realm_browser_cancel, this.mCancelClickListener).setNeutralButton(R.string.realm_browser_deselect_all, (DialogInterface.OnClickListener)null).create();
        var8.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialog) {
                Button selectionButton = ((AlertDialog)FieldFilterDialogFragment.this.getDialog()).getButton(-3);
                selectionButton.setOnClickListener(FieldFilterDialogFragment.this.mChangeSelectionClickListener);
                FieldFilterDialogFragment.this.updateSelectionButtonText();
            }
        });
        return var8;
    }

    private void updateSelectionButtonText() {
        boolean isSelectedAll = true;

        for(int selectionButton = 0; selectionButton < this.mCheckedItems.length; ++selectionButton) {
            if(!this.mCheckedItems[selectionButton]) {
                isSelectedAll = false;
                break;
            }
        }

        Button var3 = ((AlertDialog)this.getDialog()).getButton(-3);
        var3.setText(isSelectedAll?this.mDeselectAllText:this.mSelectAllText);
    }

    interface FieldFilterDialogInteraction {
        void onFieldListChange();
    }
}
