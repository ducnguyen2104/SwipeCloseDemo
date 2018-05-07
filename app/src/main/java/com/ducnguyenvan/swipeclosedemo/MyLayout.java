package com.ducnguyenvan.swipeclosedemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MyLayout extends FrameLayout implements NestedScrollingParent {

    private float mFirstMotionX;
    private float mLastMotionX;
    private float mFirstMotionY;
    private float mLastMotionY;
    private static final int MIN_DISTANCE_FOR_FLING = 50;
    private VelocityTracker mVelocityTracker;
    float xVelocity;
    float yVelocity;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private boolean mIsBeingDraggedX;
    private boolean mIsBeingDraggedY;
    private int mTouchSlop;
    private int mSwipingSlopX;
    private int mSwipingSlopY;
    private RecyclerView mChildView;
    private Context context;
    private View mBackgroundView;
    private int mViewWidth = 1;
    private int mViewHeight = 1;
    private boolean mAnimationRunning = false;
    private final NestedScrollingParentHelper parentHelper;
    private float myDyUnconsumed = 0;
    private float myDyConsumed = 0;


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mChildView = (RecyclerView) findViewById(R.id.recycler_view);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewWidth = mChildView.getWidth();
        mViewHeight = mChildView.getHeight();
    }

    void init() {
        context = getContext();
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mBackgroundView = new View(context);
        mBackgroundView.setBackgroundColor(0x80000000);
        mBackgroundView.setVisibility(GONE);
        addView(mBackgroundView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        if (child != mBackgroundView)
            mChildView = (RecyclerView) child;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mAnimationRunning)
            return false;
        final int action = ev.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_CANCEL | MotionEvent.ACTION_UP: {
                mIsBeingDraggedX = false;
                mIsBeingDraggedY = false;
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mFirstMotionY = 0;
                mFirstMotionX = 0;
                return false;
            }
            case MotionEvent.ACTION_DOWN: {
                mFirstMotionX = mLastMotionX = ev.getRawX();
                mFirstMotionY = mLastMotionY = ev.getRawY();
                mIsBeingDraggedX = false;
                mIsBeingDraggedY = false;
            }
            default: { //action MOVE
                final float x = ev.getRawX();
                final float deltaX = x - mFirstMotionX;
                final float xDiff = Math.abs(x - mLastMotionX);
                final float y = ev.getRawY();
                final float deltaY = y - mFirstMotionY;
                final float yDiff = Math.abs(y - mLastMotionY);
                mSwipingSlopX = (deltaX > 0 ? mTouchSlop : -mTouchSlop);
                mSwipingSlopY = (deltaY > 0 ? mTouchSlop : -mTouchSlop);
                if ((xDiff) > mTouchSlop && (xDiff) / 2 > (yDiff)) {
                    mIsBeingDraggedX = true;
                    mIsBeingDraggedY = false;
                } else  if (((deltaY < 0 && !mChildView.canScrollVertically(1)) || (deltaY > 0 && !mChildView.canScrollVertically(-1))) && yDiff > mTouchSlop && yDiff/2 > xDiff){
                    mIsBeingDraggedX = false;
                    mIsBeingDraggedY = true;
                }
                else {
                    mIsBeingDraggedY = false;
                    mIsBeingDraggedY = false;
                }

                if (mIsBeingDraggedX) {
                    performDragX(deltaX);
                    mLastMotionX = x;
                    mLastMotionY = y;
                } else if (mIsBeingDraggedY) {
                    performDragY(deltaY);
                    mLastMotionX = x;
                    mLastMotionY = y;
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
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mFirstMotionX = mLastMotionX = ev.getRawX();
                mFirstMotionY = mLastMotionY = ev.getRawY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (!mIsBeingDraggedY && !mIsBeingDraggedX)
                    return false;

                final float x = ev.getRawX();
                final float y = ev.getRawY();
                final float deltaX = x - mFirstMotionX;
                final float deltaY = y - mFirstMotionY;
                if (mIsBeingDraggedX) {
                    performDragX(deltaX);
                }
                else if (mIsBeingDraggedY) {
                    performDragY(deltaY);
                }
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            }
            case MotionEvent.ACTION_UP: {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                xVelocity = velocityTracker.getXVelocity();
                yVelocity = velocityTracker.getYVelocity();
                float x = ev.getRawX();
                float y = ev.getRawY();
                final float deltaX = x - mFirstMotionX;
                final float deltaY = y - mFirstMotionY;
                mLastMotionY = x;
                mLastMotionY = y;
                if (mIsBeingDraggedX) {
                    endDragX(deltaX);
                }
                else if (mIsBeingDraggedY) {
                    endDragY(deltaY);
                }
                mVelocityTracker.recycle();
                mVelocityTracker = null;
                mFirstMotionX = 0;
                mFirstMotionY = 0;
            }
            case MotionEvent.ACTION_CANCEL: {
                if(mVelocityTracker != null) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    xVelocity = velocityTracker.getXVelocity();
                    yVelocity = velocityTracker.getYVelocity();
                }
                float x = ev.getRawX();
                float y = ev.getRawY();
                final float deltaX = x - mFirstMotionX;
                final float deltaY = y - mFirstMotionY;
                mLastMotionY = x;
                mLastMotionY = y;
                if (mIsBeingDraggedX) {
                    endDragX(deltaX);
                }
                else if (mIsBeingDraggedY) {
                    endDragY(deltaY);
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mFirstMotionX = 0;
                mFirstMotionY = 0;
            }
        }
        return true;
    }

    private void endDragX(float deltaX) {
        if (Math.abs(xVelocity) >= mMinimumVelocity && Math.abs(deltaX) >= MIN_DISTANCE_FOR_FLING) {
            smoothScrollXToEnd(deltaX);
        } else if (Math.abs(deltaX) <= getWidth() / 2) {
            smoothScrollXToStart();
        } else {
            smoothScrollXToEnd(deltaX);
        }
        mIsBeingDraggedX = false;
    }

    private void endDragY(float deltaY) {
        Log.i("deltaY", deltaY + ", " + (getHeight()/2));
        /*if (Math.abs(yVelocity) >= mMinimumVelocity && Math.abs(deltaY) >= MIN_DISTANCE_FOR_FLING) {
            smoothScrollYToEnd(deltaY);
        }
        else*/ if(Math.abs(deltaY) <= getHeight() / 2) {
            smoothScrollYToStart();
        }
        else {
            smoothScrollYToEnd(deltaY);
        }
        mIsBeingDraggedY = false;
    }

    private void performDragX(float deltaX) {
        mChildView.setTranslationX(deltaX - mSwipingSlopX);
        updateBackgroundViewX(mChildView.getTranslationX());
    }

    private void performDragY(float deltaY) {
        mChildView.setTranslationY(deltaY - mSwipingSlopY);
        updateBackgroundViewY(mChildView.getTranslationY());
    }

    void smoothScrollYToEnd(float deltaY) {
        Log.i("smooth",""+deltaY);
        mChildView.animate().translationY(deltaY > 0 ? mViewHeight : -mViewHeight).alpha(0).setDuration(400)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        updateBackgroundViewY(mChildView.getTranslationY());
                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimationRunning = false;
                        performDismiss();
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAnimationRunning = true;
                    }
                });
    }

    void smoothScrollYToStart() {
        mChildView.animate().translationY(0).alpha(1).setDuration(400)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        updateBackgroundViewY(mChildView.getTranslationY());
                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimationRunning = false;
                        updateBackgroundViewY(mViewHeight);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAnimationRunning = true;
                    }
                });
    }

    void smoothScrollXToEnd(float deltaX) {
        mChildView.animate().translationX(deltaX > 0 ? mViewWidth : -mViewWidth).alpha(0).setDuration(250)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        updateBackgroundViewX(mChildView.getTranslationX());
                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimationRunning = false;
                        performDismiss();
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAnimationRunning = true;
                    }
                });
    }

    void smoothScrollXToStart() {
        mChildView.animate().translationX(0).alpha(1).setDuration(250)
                .setUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        updateBackgroundViewX(mChildView.getTranslationX());
                    }
                })
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAnimationRunning = false;
                        updateBackgroundViewX(mViewWidth);
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        mAnimationRunning = true;
                    }
                });
    }

    private void updateBackgroundViewX(float translationX) {
        //mBackgroundView.setVisibility(translationX == 0 ? GONE : VISIBLE);
        mBackgroundView.setVisibility(VISIBLE);
        if (translationX > 0)
            mBackgroundView.setTranslationX(translationX - mViewWidth);
        else
            mBackgroundView.setTranslationX(translationX + mViewWidth);
        mBackgroundView.setAlpha(Math.max(0f, Math.min(1f, 1f - Math.abs(translationX) / mViewWidth)));
    }

    private void updateBackgroundViewY(float translationY) {
        //mBackgroundView.setVisibility(translationY == 0 ? GONE : VISIBLE);
        mBackgroundView.setVisibility(VISIBLE);
        if (translationY > 0)
            mBackgroundView.setTranslationY(translationY - mViewHeight);
        else
            mBackgroundView.setTranslationY(translationY + mViewHeight);
        mBackgroundView.setAlpha(Math.max(0f, Math.min(1f, 1f - Math.abs(translationY) / mViewHeight)));
    }

    private void performDismiss() {
        mBackgroundView.setVisibility(GONE);
        mChildView.setVisibility(GONE);
        ((Activity) context).finish();
    }

    private static boolean isRecyclerViewScrolledToTop(RecyclerView rv) {
        final LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
        return lm.findFirstVisibleItemPosition() == 0
                && lm.findViewByPosition(0).getTop() == 0;
    }

    private static boolean isRecyclerViewOnTop(RecyclerView rv) {
        return rv.getTranslationY() == 0;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i("dy","consumed: " + dyConsumed + ", dyUncomsumed: " + dyUnconsumed);

        final RecyclerView recyclerView = (RecyclerView) target;
        if(dyUnconsumed < 0  && isRecyclerViewScrolledToTop(recyclerView)) {
            myDyUnconsumed -= dyUnconsumed;

            //Log.i("myDyUnconsumed", "" + myDyUnconsumed);
            performDragY(myDyUnconsumed);
            myDyConsumed = recyclerView.getTranslationY();
        }
        if(!isRecyclerViewOnTop(recyclerView) && dyConsumed != 0) {
            recyclerView.scrollToPosition(0);
            myDyConsumed -= 2*dyConsumed;
            performDragY(myDyConsumed);
            myDyUnconsumed = recyclerView.getTranslationY();
            if(recyclerView.getTranslationY() < 0) {
                recyclerView.setTranslationY(0);
                stopNestedScroll();
            }
        }
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(target, dx, dy, consumed);
    }

    @Override
    public int getNestedScrollAxes() {
        return parentHelper.getNestedScrollAxes();
    }

    public MyLayout(@NonNull Context context) {
        super(context);
        parentHelper = new NestedScrollingParentHelper(this);
        init();
    }

    public MyLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        parentHelper = new NestedScrollingParentHelper(this);
        init();
    }

    public MyLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parentHelper = new NestedScrollingParentHelper(this);
        init();
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        /*if(!isRecyclerViewScrolledToTop((RecyclerView) child) && nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL) {
            mIsBeingDraggedY = false;
            return true;
        }
        else
            return false;*/
        return true;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        //parentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View child) {
        Log.i("stop", "...");
        if(!mIsBeingDraggedY) {
            endDragY(myDyUnconsumed);
        }
        myDyUnconsumed = 0;
        myDyConsumed = 0;
        stopNestedScroll();
    }
}
