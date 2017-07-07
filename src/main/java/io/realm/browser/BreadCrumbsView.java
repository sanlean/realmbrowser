package io.realm.browser;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.realm.browser.R.drawable;
import io.realm.browser.R.style;

public class BreadCrumbsView extends LinearLayout {
    private static final String COLLAPSE_INDICATOR = "...";
    private static final int COLLAPSE_INDICATOR_TAG = -1;
    private static final int COLLAPSE_INDICATOR_PADDING_DP = 2;
    private static final int CRUMB_MAX_LINES = 1;
    private static final int CRUMB_TEXT_APP_RESOURCE;
    private static final int INACTIVE_CRUMB_COLOR;
    private static final int ACTIVE_CRUMB_COLOR;
    private int mCollapseIndicatorPaddingPx;
    private int mWidth;
    private List<StateHolder> mCrumbStates;
    private IOnBreadCrumbListener mCrumbStatesListener;
    private Context mContext;
    private OnClickListener mOnCrumbClickListener = new OnClickListener() {
        public void onClick(View v) {
            int tag = ((Integer)v.getTag()).intValue();
            StateHolder holder = tag == -1?(StateHolder)BreadCrumbsView.this.mCrumbStates.get(BreadCrumbsView.this.mCrumbStates.size() - 2):(StateHolder)BreadCrumbsView.this.mCrumbStates.get(tag);
            BreadCrumbsView.this.activateCrumb(holder);
        }
    };

    public BreadCrumbsView(Context context) {
        super(context);
        this.init(context);
    }

    public BreadCrumbsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public BreadCrumbsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    @TargetApi(21)
    public BreadCrumbsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        this.mCrumbStates = new ArrayList();
        this.setOrientation(HORIZONTAL);
        this.setGravity(16);
        DisplayMetrics m = this.getResources().getDisplayMetrics();
        this.mCollapseIndicatorPaddingPx = (int)TypedValue.applyDimension(1, 2.0F, m);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.updateView(w);
    }

    private void activateCrumb(StateHolder stateHolder) {
        int position = this.mCrumbStates.indexOf(stateHolder);
        int count = this.mCrumbStates.size();
        if(count <= position) {
            throw new IllegalArgumentException(String.format("BreadcrumbsView: count = %d, position = %d", new Object[]{Integer.valueOf(count), Integer.valueOf(position)}));
        } else {
            this.mCrumbStates = this.mCrumbStates.subList(0, position + 1);
            this.updateView(this.mWidth);
            if(this.mCrumbStatesListener != null) {
                this.mCrumbStatesListener.onStateChanged(stateHolder);
            }

        }
    }

    public void addCrumb(StateHolder state) {
        this.mCrumbStates.add(state);
        this.updateView(this.mWidth);
    }

    public void clearCrumbs() {
        this.mCrumbStates.clear();
        this.updateView(this.mWidth);
    }

    public void setCrumbStates(List<StateHolder> crumbStates) {
        this.mCrumbStates = crumbStates;
        this.updateView(this.mWidth);
        if(this.mCrumbStatesListener != null && this.mCrumbStates != null && !this.mCrumbStates.isEmpty()) {
            this.mCrumbStatesListener.onStateChanged((StateHolder)this.mCrumbStates.get(this.mCrumbStates.size() - 1));
        }

    }

    public List<StateHolder> getCrumbStates() {
        return this.mCrumbStates;
    }

    private TextView createDefaultView(String text, int position, int totalSize) {
        TextView view = new TextView(this.mContext);
        LayoutParams layoutParams = new LayoutParams(-2, -1);
        view.setLayoutParams(layoutParams);
        view.setPadding(this.mCollapseIndicatorPaddingPx, 0, 0, 0);
        view.setGravity(16);
        view.setMaxLines(1);
        view.setEllipsize(TruncateAt.END);
        if(this.mContext.getResources().getResourceName(CRUMB_TEXT_APP_RESOURCE) != null) {
            view.setTextAppearance(this.mContext, CRUMB_TEXT_APP_RESOURCE);
            view.setText(Html.fromHtml(text));
        } else {
            view.setText(Html.fromHtml("<u>" + text + "</u>"));
        }

        view.setOnClickListener(this.mOnCrumbClickListener);
        view.setTag(Integer.valueOf(position));
        int textColor;
        if(position != totalSize - 1) {
            view.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable.realm_browser_ic_breadcrumb, 0);
            view.setCompoundDrawablePadding(this.mCollapseIndicatorPaddingPx);
            textColor = INACTIVE_CRUMB_COLOR;
        } else {
            textColor = ACTIVE_CRUMB_COLOR;
        }

        view.setTextColor(textColor);
        view.setGravity(16);
        return view;
    }

    private String getSimpleName(String text) {
        String[] path = text.split("\\.");
        return path.length > 0?path[path.length - 1]:text;
    }

    private void updateView(int parentViewWidth) {
        this.removeAllViews();
        if(this.mCrumbStates != null && !this.mCrumbStates.isEmpty()) {
            int count = this.mCrumbStates.size();
            TextView rootView = this.createDefaultView(this.getSimpleName(((StateHolder)this.mCrumbStates.get(0)).getCaption()), 0, count);
            this.addView(rootView);

            for(int i = 1; i < count; ++i) {
                TextView view = this.createDefaultView(this.getSimpleName(((StateHolder)this.mCrumbStates.get(i)).getCaption()), i, count);
                this.addView(view);
                if(this.calculateWidth() > parentViewWidth) {
                    this.removeViews(1, this.getChildCount() - 2);
                    view = this.createDefaultView("...", -1, count);
                    this.addView(view, 1);
                }
            }

        }
    }

    public void setOnCrumbClickListener(IOnBreadCrumbListener mCrumbClickListener) {
        this.mCrumbStatesListener = mCrumbClickListener;
    }

    private int calculateWidth() {
        this.measure(0, 0);
        return this.getMeasuredWidth();
    }

    static {
        CRUMB_TEXT_APP_RESOURCE = style.realm_browser_database_bread_crumb_style;
        INACTIVE_CRUMB_COLOR = Color.rgb(148, 148, 148);
        ACTIVE_CRUMB_COLOR = Color.rgb(255, 255, 255);
    }
}
