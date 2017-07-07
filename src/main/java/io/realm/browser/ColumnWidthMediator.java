package io.realm.browser;

import android.content.ClipData;
import android.view.View.DragShadowBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ColumnWidthMediator implements RowView.OnColumnWidthChangeListener, DragOverlayView.OnDragFinished {
    private int colPosition;
    private int colLeft;
    private DragOverlayView dragOverlayView;
    private ColumnWidthMediator.ColumnWidthProvider mColumnWidthProvider;
    private List<RowView> mViews = new ArrayList();

    ColumnWidthMediator(DragOverlayView view, ColumnWidthMediator.ColumnWidthProvider widthProvider) {
        this.dragOverlayView = view;
        this.dragOverlayView.setOnDragFinishedListener(this);
        this.mColumnWidthProvider = widthProvider;
    }

    public void startColumnWidthChange(int minX, int currentLeft, int currentRight, int position) {
        this.colLeft = currentLeft;
        this.colPosition = position;
        this.dragOverlayView.setMinLeft(minX);
        this.dragOverlayView.setShadowPosition(currentRight);
        this.dragOverlayView.startDrag((ClipData)null, new DragShadowBuilder(), (Object)null, 0);
    }

    public void onDragFinished(int position) {
        int newWidth = position - this.colLeft;
        Iterator i$ = this.mViews.iterator();

        while(i$.hasNext()) {
            RowView v = (RowView)i$.next();
            v.setColumnWidth(newWidth, this.colPosition);
        }

    }

    public int getColWidth(int position) {
        return this.mColumnWidthProvider.getColumnWidth(position);
    }

    public void addView(RowView v) {
        this.mViews.add(v);
    }

    public void removeAllViews() {
        this.mViews.clear();
    }

    interface ColumnWidthProvider {
        int getColumnWidth(int var1);
    }
}
