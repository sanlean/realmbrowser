package io.realm.browser;

import android.animation.Animator.AnimatorListener;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import io.realm.browser.R.drawable;

public class DragOverlayView extends View {
    private Drawable mShadow;
    private int mShortAnimTime;
    private int mPositionX;
    private int mMinLeft;
    private DragOverlayView.OnDragFinished mDragFinishListener;

    public DragOverlayView(Context context) {
        super(context);
        this.init();
    }

    public DragOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public DragOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init();
    }

    @TargetApi(21)
    public DragOverlayView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.init();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int viewHeight = this.getMeasuredHeight();
        int left = this.mPositionX;
        this.mShadow.setBounds(left, 0, left + this.mShadow.getIntrinsicWidth(), viewHeight);
        this.mShadow.draw(canvas);
    }

    public boolean onDragEvent(DragEvent event) {
        int action = event.getAction();
        switch(action) {
            case 1:
                this.animate().alpha(1.0F).setDuration((long)this.mShortAnimTime).setListener((AnimatorListener)null);
                break;
            case 2:
                this.setShadowPosition((int)event.getX());
            case 3:
            default:
                break;
            case 4:
                if(this.mDragFinishListener != null) {
                    this.mDragFinishListener.onDragFinished(this.mPositionX);
                }

                this.mPositionX = 0;
                this.mMinLeft = 0;
                this.animate().alpha(0.0F).setDuration((long)this.mShortAnimTime).setListener((AnimatorListener)null);
        }

        return true;
    }

    public void setMinLeft(int minLeft) {
        this.mMinLeft = minLeft;
    }

    public void setShadowPosition(int x) {
        this.mPositionX = Math.max(this.mMinLeft, x);
        this.invalidate();
    }

    public void setOnDragFinishedListener(DragOverlayView.OnDragFinished listener) {
        this.mDragFinishListener = listener;
    }

    private void init() {
        this.mShadow = ResourcesCompat.getDrawable(this.getResources(), drawable.realm_browser_dummy_drag_divider_vertical, this.getContext().getTheme());
        this.mShortAnimTime = this.getResources().getInteger(17694720);
    }

    public interface OnDragFinished {
        void onDragFinished(int var1);
    }
}
