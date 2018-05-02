package com.ducnguyenvan.swipeclosedemo;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

public class MyLayout extends FrameLayout {

    /**
     * Position of the last motion event.
     */
    private float mFirstMotionX;
    private float mLastMotionX;
    private float mFirstMotionY;
    private float mLastMotionY;
    private float deltaX;
    private float deltaY;
    private int mActivePointerId; //= INVALID_POINTER;
    private static final int INVALID_POINTER = -1;
    private static final int MIN_DISTANCE_FOR_FLING = 50;
    private VelocityTracker mVelocityTracker;
    float xVelocity;
    float yVelocity;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private boolean mIsBeingDraggedX;
    private boolean mIsBeingDraggedY;
    private int mTouchSlop;
    //private TextView txt ;
    private RecyclerView recyclerView;
    //private ScrollView scrollView;
    private Context context;

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //txt = (TextView)findViewById(R.id.txt);
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        //scrollView = (ScrollView)findViewById(R.id.scrollView);
        //scrollView.requestDisallowInterceptTouchEvent(true);
    }

    void init(){
        context = getContext();
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_CANCEL | MotionEvent.ACTION_UP: {
                //Log.i("cancel, up", "..");
                mActivePointerId = INVALID_POINTER;
                mIsBeingDraggedX = false;
                mIsBeingDraggedY = false;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                return false;
            }
            case MotionEvent.ACTION_DOWN: {
                mFirstMotionX = mLastMotionX =  ev.getX();
                mFirstMotionY = mLastMotionY = ev.getY();
                mIsBeingDraggedX = false;
                mIsBeingDraggedY = false;
            }
            default: { //action MOVE
                //Log.i("move","..");
                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    //Log.i("break","..");
                    break;
                }
                //Log.i("move 2","..");
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                final float x = MotionEventCompat.getX(ev, pointerIndex);
                deltaX = x - mFirstMotionX;
                final float xDiff = Math.abs(x - mLastMotionX);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                deltaY = y - mFirstMotionY;
                Log.i("delta Y", "" + deltaY);
                final float yDiff = Math.abs(y - mLastMotionY);

                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    mIsBeingDraggedX = true;
                    //Log.i("intercept","!");
                } else {
                    mIsBeingDraggedX = false;
                    //Log.i("no intercept", "..");
                }
                if(mIsBeingDraggedX) {
                    performDragX();
                    mLastMotionX = x;
                    mLastMotionY = y;
                }
                else if (deltaY < 0 && (!recyclerView.canScrollVertically(1)) || (deltaY > 0 && !recyclerView.canScrollVertically(-1))) {
                    mIsBeingDraggedY = true;
                    performDragY();
                }
            }
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);

        return mIsBeingDraggedX || mIsBeingDraggedY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mFirstMotionX = mLastMotionX = ev.getX();
                mFirstMotionY = mLastMotionY = ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!mIsBeingDraggedX || !mIsBeingDraggedY) {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, pointerIndex);
                    final float xDiff = Math.abs(x - mLastMotionX);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = Math.abs(y - mLastMotionY);
                    if (yDiff > mTouchSlop) {
                        mIsBeingDraggedY = true;
                        mIsBeingDraggedX = false;
                    }
                    if (xDiff > mTouchSlop && xDiff > yDiff) {
                        mIsBeingDraggedX = true;
                        mIsBeingDraggedY = false;
                    }
                }
                if (mIsBeingDraggedX) {
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    deltaX = x - mFirstMotionX;
                    deltaY = y - mFirstMotionY;
                    performDragX();
                    mLastMotionX = x;
                    mLastMotionY = y;
                }
                if (mIsBeingDraggedY) {
                    final int activePointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float x = MotionEventCompat.getX(ev, activePointerIndex);
                    final float y = MotionEventCompat.getY(ev, activePointerIndex);
                    deltaX = x - mFirstMotionX;
                    deltaY = y - mFirstMotionY;
                    performDragY();
                    mLastMotionX = x;
                    mLastMotionY = y;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                xVelocity = velocityTracker.getXVelocity(mActivePointerId);
                yVelocity = velocityTracker.getYVelocity(mActivePointerId);
                float x = MotionEventCompat.getX(ev, pointerIndex);
                float y = MotionEventCompat.getY(ev,pointerIndex);
                deltaX = x - mFirstMotionX;
                deltaY = y - mFirstMotionY;
                if (mIsBeingDraggedX) {
                    endDragX();
                }
                if (mIsBeingDraggedY) {
                    endDragY();
                }
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mIsBeingDraggedX) {
                    endDragX();
                }
                if (mIsBeingDraggedY) {
                    endDragY();
                }
            }
        }
        return true;
    }

    private void endDragX() {
        if (xVelocity >= mMinimumVelocity && deltaX >= MIN_DISTANCE_FOR_FLING) {
            //txt.setTranslationX(getWidth());
            recyclerView.setTranslationX(getWidth());
            //scrollView.setTranslationX(getWidth());
            ((Activity)context).finish();
        }
        if(Math.abs(deltaX) <= getWidth()/2) {
            //txt.setTranslationX(0);
            recyclerView.setTranslationX(0);
            //scrollView.setTranslationX(0);
            this.setAlpha(1);
        }
        else {
            //txt.setTranslationX(getWidth());
            recyclerView.setTranslationX(getWidth());
            //scrollView.setTranslationX(getWidth());
            ((Activity)context).finish();
        }
        mIsBeingDraggedX = false;
    }

    private void endDragY() {
        if (yVelocity >= mMinimumVelocity && deltaY >= MIN_DISTANCE_FOR_FLING) {
            recyclerView.setTranslationY(getHeight());
            ((Activity)context).finish();
        }
        if(Math.abs(deltaY) <= getHeight() / 2) {
            recyclerView.setTranslationY(0);
            //scrollView.setTranslationY(0);
            this.setAlpha(1);
        }
        else {
            recyclerView.setTranslationY(getHeight());
            //scrollView.setTranslationY(getHeight());
            ((Activity)context).finish();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    private void performDragX() {
        //txt.setTranslationX(deltaX);
        recyclerView.setTranslationX(deltaX);
        //scrollView.setTranslationX(deltaX);
        this.setAlpha(1-(deltaX/getWidth()));
    }

    private void performDragY() {
        recyclerView.setTranslationY(deltaY);
        //scrollView.setTranslationY(deltaY);
        this.setAlpha(1-(deltaY/getHeight()));
    }

    public MyLayout(@NonNull Context context) {
        super(context);
        init();
    }

    public MyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MyLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MyLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }
}
