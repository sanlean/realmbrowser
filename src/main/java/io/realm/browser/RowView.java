package io.realm.browser;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import androidx.core.content.res.ResourcesCompat;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import io.realm.browser.R.dimen;
import io.realm.browser.R.drawable;

public class RowView extends HorizontalScrollView {
    private static final int MIN_COLUMN_WIDTH_PX = 75;
    private static final int ITEM_PADDING_LEFT_RIGHT_DP = 4;
    private LinearLayout mRootTable;
    private int mMinColumnWidth;
    private int mMinColumnHeight = -1;
    private int mColumnsNumber = -1;
    private int mWidth = -1;
    private int mItemPaddingLeftRight;
    private RowView.OnScrollChangedListener mScrollListener;
    private RowView.OnCellClickListener mCellClickListener;
    private RowView.OnColumnWidthChangeListener mColumnWidthChangeListener;
    private SparseArray<CharSequence> mTextBuffer = new SparseArray();
    private int mTextAppearanceResourceId;
    private int mCellGravity = 48;
    private final OnClickListener mInternalCellClickListener = new OnClickListener() {
        public void onClick(View v) {
            if(RowView.this.mCellClickListener != null) {
                int position = ((Integer)v.getTag()).intValue();
                RowView.this.mCellClickListener.onCellClick(RowView.this, position);
            }

        }
    };
    private final OnLongClickListener mInternalLongClickListener = new OnLongClickListener() {
        private int[] location = new int[2];

        public boolean onLongClick(View v) {
            if(RowView.this.mColumnWidthChangeListener != null) {
                v.getLocationOnScreen(this.location);
                int position = ((Integer)v.getTag()).intValue();
                RowView.this.mColumnWidthChangeListener.startColumnWidthChange(this.location[0] + 75, this.location[0], this.location[0] + v.getWidth(), position);
            }

            return true;
        }
    };

    public RowView(Context context) {
        super(context);
        this.init();
    }

    public RowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public RowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @TargetApi(21)
    public RowView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    public void setOnScrollChangedListener(RowView.OnScrollChangedListener listener) {
        this.mScrollListener = listener;
    }

    public void setColumnsNumber(int number) {
        if(number < 0) {
            throw new IllegalArgumentException("Invalid columns number");
        } else {
            this.mColumnsNumber = number;
            this.initColumns();
        }
    }

    public int getMinColumnHeight() {
        return this.mMinColumnHeight;
    }

    public void setMinColumnHeight(int mMinColumnHeight) {
        this.mMinColumnHeight = mMinColumnHeight;
    }

    public int getColumnsNumber() {
        return this.mColumnsNumber;
    }

    public void setColumnText(CharSequence text, int position) {
        if(this.mWidth == -1) {
            this.mTextBuffer.append(position, text);
        } else {
            this.checkPositionRange(position);
            TextView tv = (TextView)this.mRootTable.getChildAt(position);
            tv.setText(text);
        }

    }

    public void setTextAppearance(int resId) {
        this.mTextAppearanceResourceId = resId;
        if(this.mRootTable.getChildCount() > 0) {
            for(int i = 0; i < this.mRootTable.getChildCount(); ++i) {
                TextView tv = (TextView)this.mRootTable.getChildAt(i);
                tv.setTextAppearance(this.getContext(), this.mTextAppearanceResourceId);
            }
        }

    }

    public void setCellsGravity(int gravity) {
        this.mCellGravity = gravity;
        if(this.mRootTable.getChildCount() > 0) {
            for(int i = 0; i < this.mRootTable.getChildCount(); ++i) {
                TextView tv = (TextView)this.mRootTable.getChildAt(i);
                tv.setGravity(this.mCellGravity);
            }
        }

    }

    public void showDividers(boolean showDividers) {
        Resources res = this.getResources();
        Theme theme = this.getContext().getTheme();
        int dividerId = showDividers?drawable.realm_browser_divider_vertical:drawable.realm_browser_placeholder_1dp;
        this.mRootTable.setDividerDrawable(ResourcesCompat.getDrawable(res, dividerId, theme));
    }

    public void setOnCellClickListener(RowView.OnCellClickListener listener) {
        this.mCellClickListener = listener;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if(w != oldw && w != 0) {
            this.mWidth = w;
            this.updateColumns();
        }

    }

    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(this.mScrollListener != null) {
            this.mScrollListener.onScrollChanged(l, t, oldl, oldt);
        }

    }

    private void checkPositionRange(int position) {
        int childCnt = this.mRootTable.getChildCount();
        if(position >= childCnt || position < 0) {
            throw new IllegalArgumentException("Invalid position, position " + position + ", child count " + childCnt);
        }
    }

    private void init() {
        this.setOverScrollMode(2);
        DisplayMetrics m = this.getResources().getDisplayMetrics();
        this.mItemPaddingLeftRight = (int)TypedValue.applyDimension(1, 4.0F, m);
        this.mMinColumnWidth = this.getResources().getDimensionPixelSize(dimen.realm_browser_min_column_width);
        this.setHorizontalScrollBarEnabled(false);
        this.setClipChildren(false);
        this.setClipToPadding(false);
        this.mRootTable = new LinearLayout(this.getContext());
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        this.mRootTable.setLayoutParams(layoutParams);
        this.mRootTable.setOrientation(LinearLayout.HORIZONTAL);
        this.mRootTable.setClipChildren(false);
        this.mRootTable.setClipToPadding(false);
        this.mRootTable.setDividerDrawable(ResourcesCompat.getDrawable(this.getResources(), drawable.realm_browser_placeholder_1dp, this.getContext().getTheme()));
        this.mRootTable.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        this.addView(this.mRootTable);
    }

    private void initColumns() {
        int requiredWidth = this.mMinColumnWidth * this.mColumnsNumber;
        this.mRootTable.removeAllViews();
        if(this.mColumnsNumber != 0) {
            int columnWidth;
            if(this.mWidth != -1 && requiredWidth < this.mWidth) {
                columnWidth = this.mWidth / this.mColumnsNumber;
            } else {
                columnWidth = this.mMinColumnWidth;
            }

            for(int i = 0; i < this.mColumnsNumber; ++i) {
                TextView tv = new TextView(this.getContext());
                LayoutParams params = new LayoutParams(columnWidth, -1);
                tv.setLayoutParams(params);
                tv.setGravity(this.mCellGravity);
                tv.setTextAppearance(this.getContext(), this.mTextAppearanceResourceId);
                tv.setMaxLines(2);
                tv.setEllipsize(TruncateAt.END);
                tv.setPadding(this.mItemPaddingLeftRight, 0, this.mItemPaddingLeftRight, 0);
                if(this.mMinColumnHeight > 0) {
                    tv.setMinimumHeight(this.mMinColumnHeight);
                }

                tv.setTag(Integer.valueOf(i));
                tv.setOnClickListener(this.mInternalCellClickListener);
                tv.setOnLongClickListener(this.mInternalLongClickListener);
                this.mRootTable.addView(tv);
            }

        }
    }

    private void updateColumns() {
        if(this.mWidth != -1 && this.mColumnsNumber != 0) {
            if(this.mRootTable.getChildCount() != this.mColumnsNumber) {
                this.initColumns();
            }

            int requiredWidth = this.mMinColumnWidth * this.mColumnsNumber;
            int columnWidth;
            if(requiredWidth >= this.mWidth) {
                columnWidth = this.mMinColumnWidth;
            } else {
                columnWidth = this.mWidth / this.mColumnsNumber;
            }

            for(int i = 0; i < this.mColumnsNumber; ++i) {
                TextView tv = (TextView)this.mRootTable.getChildAt(i);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)tv.getLayoutParams();
                if(params.width != columnWidth) {
                    params.width = columnWidth;
                    tv.setLayoutParams(params);
                }

                tv.setText((CharSequence)this.mTextBuffer.get(i));
            }

        }
    }

    public void setColumnWidth(int width, int position) {
        if(this.mWidth != -1) {
            TextView tv = (TextView)this.mRootTable.getChildAt(position);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, -1);
            tv.setLayoutParams(params);
        }
    }

    public int getColumnWidth(int position) {
        TextView tv = (TextView)this.mRootTable.getChildAt(position);
        return tv.getWidth();
    }

    public void setOnColumnWidthChangeListener(RowView.OnColumnWidthChangeListener listener) {
        this.mColumnWidthChangeListener = listener;
    }

    public interface OnColumnWidthChangeListener {
        void startColumnWidthChange(int var1, int var2, int var3, int var4);
    }

    public interface OnCellClickListener {
        void onCellClick(RowView var1, int var2);
    }

    public interface OnScrollChangedListener {
        void onScrollChanged(int var1, int var2, int var3, int var4);
    }
}
