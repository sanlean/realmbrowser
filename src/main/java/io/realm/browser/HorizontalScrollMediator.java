package io.realm.browser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HorizontalScrollMediator implements RowView.OnScrollChangedListener {
    private List<RowView> mViews = new ArrayList();
    private int mScrollX;
    private int mScrollY;

    public HorizontalScrollMediator() {
    }

    public void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(this.mScrollX != l) {
            this.mScrollX = l;
            this.mScrollY = t;
            Iterator i$ = this.mViews.iterator();

            while(i$.hasNext()) {
                RowView v = (RowView)i$.next();
                v.scrollTo(l, t);
            }

        }
    }

    public int getScrollX() {
        return this.mScrollX;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    public void addView(RowView v) {
        v.setOnScrollChangedListener(this);
        this.mViews.add(v);
    }

    public void removeAllViews() {
        this.mViews.clear();
    }
}
