package com.allyants.boardview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;

import java.util.ArrayList;

/**
 * Created by jbonk on 4/17/2017.
 */

public class BoardView extends FrameLayout {

    private static final float GLOBAL_SCALE = 0.6f;
    private static final int SCROLL_ANIMATION_DURATION = 325;
    private boolean mCellIsMobile = false;
    private BitmapDrawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;
    private boolean columnSnap;
    private boolean isTouched;
    private boolean isSnapping;

    private int background_color;

    private HorizontalScrollView mRootLayout;
    private LinearLayout mParentLayout;

    private int originalPosition = -1;
    private int originalItemPosition = -1;

    public void setBackgroundColor(int color){
        background_color = color;
    }

    public int getBackgroundColor(){
        return background_color;
    }

    private DoneListener mDoneCallback = new DoneListener() {
        @Override
        public void onDone() {

        }
    };

    private FooterClickListener footerClickListener = new FooterClickListener() {
        @Override
        public void onClick(View v, int column_pos) {

        }
    };

    private HeaderClickListener headerClickListener = new HeaderClickListener() {
        @Override
        public void onClick(View v, int column_pos) {

        }
    };

    private ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onClick(View v, int column_pos, int item_pos) {

        }
    };

    private DragColumnStartCallback mDragColumnStartCallback = new DragColumnStartCallback() {
        @Override
        public void startDrag(View itemView, int originalPosition) {

        }

        @Override
        public void changedPosition(View itemView, int originalPosition, int newPosition) {

        }

        @Override
        public void dragging(View itemView, MotionEvent event) {

        }

        @Override
        public void endDrag(View itemView, int originalPosition, int newPosition) {

        }
    };

    private DragItemStartCallback mDragItemStartCallback = new DragItemStartCallback() {
        @Override
        public void startDrag(View itemView, int originalPosition,int originalColumn) {

        }

        @Override
        public void changedPosition(View itemView, int originalPosition,int originalColumn, int newPosition, int newColumn) {

        }

        @Override
        public void dragging(View itemView, MotionEvent event) {

        }

        @Override
        public void endDrag(View itemView, int originalPosition,int originalColumn, int newPosition, int newColumn) {

        }
    };

    public boolean constWidth = true;

    private final int LINE_THICKNESS = 15;

    private boolean can_scroll = false;
    private boolean created = false;

    private int mLastEventX = -1;
    private int mLastEventY = -1;
    private Scroller mScroller;

    public interface DragColumnStartCallback{
        void startDrag(View itemView, int originalPosition);
        void changedPosition(View itemView, int originalPosition, int newPosition);
        void dragging(View itemView, MotionEvent event);
        void endDrag(View itemView, int originalPosition, int newPosition);
    }

    public interface DragItemStartCallback{
        void startDrag(View itemView, int originalPosition,int originalColumn);
        void changedPosition(View itemView, int originalPosition,int originalColumn, int newPosition, int newColumn);
        void dragging(View itemView, MotionEvent event);
        void endDrag(View itemView, int originalPosition,int originalColumn, int newPosition, int newColumn);
    }

    public interface FooterClickListener{
        void onClick(View v,int column_pos);
    }


    public interface HeaderClickListener{
        void onClick(View v,int column_pos);
    }

    public interface ItemClickListener{
        void onClick(View v,int column_pos,int item_pos);
    }

    public void setOnHeaderClickListener(HeaderClickListener headerClickListener){
        this.headerClickListener = headerClickListener;
    }

    public void setOnFooterClickListener(FooterClickListener footerClickListener){
        this.footerClickListener = footerClickListener;
    }

    public void setOnItemClickListener(ItemClickListener itemClickListener){
        this.itemClickListener = itemClickListener;
    }

    public void setOnDragColumnListener(DragColumnStartCallback dragStartCallback){
        mDragColumnStartCallback = dragStartCallback;
    }

    public void setOnDragItemListener(DragItemStartCallback dragStartCallback){
        mDragItemStartCallback = dragStartCallback;
    }

    public void setOnDoneListener(DoneListener onDoneListener){
        mDoneCallback = onDoneListener;
    }

    long last_swap = System.currentTimeMillis();
    long last_swap_item = System.currentTimeMillis();

    final long ANIM_TIME = 300;
    long mDelaySwap = 400;
    long mDelaySwapItem = 400;

    private int mLastSwap = -1;
    private int mDownY = -1;
    private int mDownX = -1;

    private boolean mSwapped = false;
    private boolean canDragHorizontal = true;
    private boolean canDragVertical = true;

    private boolean mCellSubIsMobile = false;

    private int mTotalOffsetX = 0;
    private int mTotalOffsetY = 0;

    public BoardAdapter boardAdapter;

    private View mobileView;

    public BoardView(Context context) {
        super(context);
    }

    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.BoardView,0,0);
        try {
            background_color = ta.getColor(R.styleable.BoardView_boardItemBackground,Color.TRANSPARENT);
        }finally{
            ta.recycle();
        }
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.BoardView,0,0);
        try {
            background_color = ta.getColor(R.styleable.BoardView_boardItemBackground,Color.TRANSPARENT);
        }finally{
            ta.recycle();
        }
    }

    public interface DoneListener {
        void onDone();
    }

    public void setAdapter(BoardAdapter boardAdapter){
        this.boardAdapter = boardAdapter;
        Log.e("set","adapter");
        boardAdapter.boardView = this;
        mParentLayout.removeAllViews();
        boardAdapter.createColumns();
        for(int i = 0;i < boardAdapter.columns.size();i++){
            BoardAdapter.Column column = boardAdapter.columns.get(i);
            addColumnList(column.header,column.views,column.footer,i);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mRootLayout = new HorizontalScrollView(getContext());
        mRootLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));
        mParentLayout = new LinearLayout(getContext());
        mParentLayout.setOrientation(LinearLayout.HORIZONTAL);
        mScroller = new Scroller(mRootLayout.getContext(), new DecelerateInterpolator(1.2f));
        mParentLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        mRootLayout.addView(mParentLayout);
        addView(mRootLayout);
        SetColumnSnap(true);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if(!created){
            created = true;
            mDoneCallback.onDone();
        }
        if(mHoverCell != null){
            mHoverCell.draw(canvas);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean colValue = handleColumnDragEvent(event);
        return colValue || super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean colValue = handleColumnDragEvent(event);
        return colValue || super.onTouchEvent(event);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    VelocityTracker mVelocityTracker = VelocityTracker.obtain();
    public void SetColumnSnap(boolean columnSnap){
        this.columnSnap = columnSnap;
        mRootLayout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isTouched = true;
                isSnapping = false;
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        mVelocityTracker.clear();
                        mVelocityTracker.addMovement(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mVelocityTracker.addMovement(event);
                        mVelocityTracker.computeCurrentVelocity(1000);
                        break;
                    case MotionEvent.ACTION_UP:
                        isTouched = false;
                        Log.e("Velocity",String.valueOf(mVelocityTracker.getXVelocity()));
                        if(Math.abs(mVelocityTracker.getXVelocity()) < 230){
                            int pos = getPositionInListX(mRootLayout.getWidth()/2,mParentLayout);
                            Log.e("Pos",String.valueOf(pos));
                            scrollToColumn(pos,true);
                        }
                        break;
                }
                return false;
            }
        });
        if(this.columnSnap) {
            mRootLayout.setOnScrollChangeListener(onScrollChangeListener);
        }else{
            mRootLayout.setOnScrollChangeListener(null);
        }
    }

    OnScrollChangeListener onScrollChangeListener = new OnScrollChangeListener() {
        @Override
        public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            if(!isTouched && !isSnapping && mParentLayout.getChildCount() > 0) {
                int deltaX = Math.abs(oldScrollX - scrollX);
                if(deltaX < 3){
                    //Scroll To closest column
                    isSnapping = true;
                    int[] location = new int[2];
                    mParentLayout.getLocationOnScreen(location);
                    int offset = (mParentLayout.getChildAt(0).getWidth())/2;
                    if(oldScrollX - scrollX <= 0){
                        offset *= 2;
                    }
                    int x = scrollX+location[0]+offset;
                    int pos = getPositionInListX(x,mParentLayout);
                    scrollToColumn(pos,true);
                }
            }
        }
    };

    public void scrollToColumn(int column,boolean animate){
        if(column >= 0) {
            View childView = mParentLayout.getChildAt(column);
            if(childView != null) {
                final int newX = childView.getLeft() - (int) (((getMeasuredWidth() - childView.getMeasuredWidth()) / 2));
                if (animate) {
                    mRootLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            mRootLayout.smoothScrollTo(newX, 0);
                        }
                    });
                } else {
                    mRootLayout.scrollTo(newX, 0);
                }
            }
        }
    }

    public boolean handleColumnDragEvent(MotionEvent event){
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int)event.getX();
                mDownY = (int)event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                if(mDownY == -1){
                    mDownY = (int)event.getRawY();

                }
                if(mDownX == -1){
                    mDownX = (int)event.getX();
                }

                mLastEventX = (int) event.getX();
                mLastEventY = (int) event.getRawY();
                int deltaX = mLastEventX - mDownX;
                int deltaY = mLastEventY - mDownY;

                if(mCellSubIsMobile){
                    if(mDragItemStartCallback != null)
                        mDragItemStartCallback.dragging(mobileView,event);
                    int offsetX = 0;
                    if(canDragHorizontal){
                        offsetX =  deltaX;
                    }
                    int offsetY = mHoverCellOriginalBounds.top;
                    if(canDragVertical){
                        offsetY = mHoverCellOriginalBounds.top + deltaY;
                    }
                    mHoverCell.setBounds(rotatedBounds(mHoverCellCurrentBounds,0.0523599f));
                    mHoverCellCurrentBounds.offsetTo(offsetX,
                            mLastEventY - 330);
                    invalidate();
                    handleItemSwitchHorizontal();
                    return true;
                }else if (mCellIsMobile) {
                    if(mDragColumnStartCallback != null)
                        mDragColumnStartCallback.dragging(mobileView,event);
                    int offsetX = 0;
                    if(canDragHorizontal){
                        offsetX =  deltaX;
                    }
                    int offsetY = mHoverCellOriginalBounds.top;
                    if(canDragVertical){
                        offsetY = mHoverCellOriginalBounds.top + deltaY + mTotalOffsetY;
                    }
                    mHoverCell.setBounds(rotatedBounds(mHoverCellCurrentBounds,0.0523599f));
                    mHoverCellCurrentBounds.offsetTo(offsetX,
                            offsetY);
                    invalidate();
                    handleColumnSwitchHorizontal();

                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_CANCEL:
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                /* If a multitouch event took place and the original touch dictating
                 * the movement of the hover cell has ended, then the dragging event
                 * ends and the hover cell is animated to its corresponding position
                 * in the listview. */
//                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
//                        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//                final int pointerId = event.getPointerId(pointerIndex);
//                if (pointerId == mActivePointerId) {
//
//                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.isFinished() && mScroller.computeScrollOffset()) {
            int x = mScroller.getCurrX();
            int y = mScroller.getCurrY();
            if (getScrollX() != x || getScrollY() != y) {
                scrollTo(x, y);
            }

            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            super.computeScroll();
        }
    }

    //checks if item should be switched between columns
    private void handleItemSwitchHorizontal(){
        int itemPos = ((LinearLayout)(mobileView.getParent())).indexOfChild(mobileView);
        View aboveView = ((LinearLayout)(mobileView.getParent())).getChildAt(itemPos - 1);
        View belowView = ((LinearLayout)(mobileView.getParent())).getChildAt(itemPos + 1);
        int[] location = new int[2];
        mobileView.getLocationOnScreen(location);
        int[] parentLocation = new int[2];
        ScrollView parent = ((ScrollView)((LinearLayout)mobileView.getParent()).getParent());
        parent.getLocationOnScreen(parentLocation);
        if(mLastEventY < parentLocation[1]+200){
            parent.smoothScrollBy(0,-10);
        }
        if(mLastEventY > parentLocation[1]+parent.getHeight()-200){
            parent.smoothScrollBy(0,10);
        }
        //swapping above
        if(aboveView != null){
            int[] locationAbove = new int[2];
            aboveView.getLocationInWindow(locationAbove);

            if(locationAbove[1]+aboveView.getHeight()/2 > mLastEventY){
                switchItemFromPosition(-1,mobileView);
                Log.e("t","swap");
            }
        }
        //swapping below
        if(belowView != null){
            int[] locationBelow = new int[2];
            belowView.getLocationOnScreen(locationBelow);
            if(locationBelow[1] + belowView.getHeight()/2 < mLastEventY){
                switchItemFromPosition(1,mobileView);
            }
        }
        int columnPos = mParentLayout.indexOfChild((View)(mobileView.getParent().getParent().getParent()));

        int leftPos = 1;
        while (columnPos - leftPos > 0 && boardAdapter.columns.get(columnPos - leftPos).items_locked){
            leftPos++;
        }
        View leftView = mParentLayout.getChildAt(columnPos - leftPos);
        View currentView = mParentLayout.getChildAt(columnPos);
        int rightPos = 1;
        while(boardAdapter.columns.size() > columnPos+rightPos && boardAdapter.columns.get(columnPos + rightPos).items_locked){
            rightPos++;
        }
        View rightView = mParentLayout.getChildAt(columnPos + rightPos);
        View firstLeftView = mParentLayout.getChildAt(columnPos-1);
        View firstRightView = mParentLayout.getChildAt(columnPos+1);

        if(leftView != null){
            int[] locationLeft = new int[2];
            firstLeftView.getLocationOnScreen(locationLeft);
            if (locationLeft[0] + leftView.getWidth() > mLastEventX) {
                int pos = ((LinearLayout)mobileView.getParent()).indexOfChild(mobileView);
                if(last_swap_item <= System.currentTimeMillis() - mDelaySwapItem) {
                    last_swap_item = System.currentTimeMillis();
                    if(((LinearLayout)mobileView.getParent()) != null) {
                        ((LinearLayout) mobileView.getParent()).removeViewAt(pos);
                        LinearLayout layout = ((LinearLayout)((ScrollView)((ViewGroup)leftView).getChildAt(1)).getChildAt(0));
                        layout.addView(mobileView,getPositionInListY(mLastEventY,layout));
                        scrollToColumn(columnPos-leftPos,true);
                        int newItemPos = ((LinearLayout)((ViewGroup)leftView).getChildAt(0)).indexOfChild(mobileView)-1;
                        int newColumnPos = ((LinearLayout)mobileView.getParent().getParent().getParent()).indexOfChild((View)(mobileView.getParent().getParent()));
                        mDragItemStartCallback.changedPosition(mobileView,originalItemPosition,originalPosition,newItemPos,newColumnPos);
                    }
                }
            }
        }
        if(rightView != null){
            int[] locationRight = new int[2];
            firstRightView.getLocationOnScreen(locationRight);
            if (locationRight[0] < mLastEventX) {
                int pos = ((LinearLayout)mobileView.getParent()).indexOfChild(mobileView);
                if(last_swap_item <= System.currentTimeMillis() - mDelaySwapItem) {
                    last_swap_item = System.currentTimeMillis();
                    if(((LinearLayout)mobileView.getParent()) != null) {
                        ((LinearLayout) mobileView.getParent()).removeViewAt(pos);
                        LinearLayout layout = ((LinearLayout)((ScrollView)((ViewGroup)rightView).getChildAt(1)).getChildAt(0));
                        layout.addView(mobileView,getPositionInListY(mLastEventY,layout));
                        scrollToColumn(columnPos+rightPos,true);
                        int newItemPos = ((LinearLayout)((ViewGroup)rightView).getChildAt(0)).indexOfChild(mobileView)-1;
                        int newColumnPos = ((LinearLayout)mobileView.getParent().getParent().getParent()).indexOfChild((View)(mobileView.getParent().getParent()));
                        mDragItemStartCallback.changedPosition(mobileView,originalItemPosition,originalPosition,newItemPos,newColumnPos);
                    }
                }
            }
        }

    }

    //Gets the position of a item inside a list based on the y offset
    public int getPositionInListY(int y,LinearLayout layout){
        for(int i = 0; i < layout.getChildCount();i++){
            int[] location = new int[2];
            View view = layout.getChildAt(i);
            view.getLocationOnScreen(location);
            if(y > location[1] && y < location[1]+view.getHeight()){
                return i;
            }
        }
        return 0;
    }

    //Gets the position of a item inside a list based on the y offset
    public int getPositionInListX(int x,LinearLayout layout){
        for(int i = 0; i < layout.getChildCount();i++){
            int[] location = new int[2];
            View view = layout.getChildAt(i);
            int end = layout.getWidth();
            if(layout.getChildCount() > i+1){
                int[] end_location = new int[2];
                layout.getChildAt(i+1).getLocationOnScreen(end_location);
                end = end_location[0];
            }
            view.getLocationOnScreen(location);
            if(x >= location[0] && x <= end){
                return i;
            }
        }
        return 0;
    }

    //Change int change to position to fix problem with starting from the bottom
    private void switchItemFromPosition(int change,View view){
        LinearLayout parentLayout = (LinearLayout)(view.getParent());
        int columnPos = parentLayout.indexOfChild(view);
        if(columnPos+change >=  0 && columnPos+change < parentLayout.getChildCount()) {
            parentLayout.removeView(view);
            parentLayout.addView(view, columnPos + change);
            if(mDragItemStartCallback != null){
                int newPos = parentLayout.indexOfChild(view);
                last_swap = System.currentTimeMillis();
                mLastSwap = newPos;
                int newColumnPos = ((LinearLayout)mobileView.getParent().getParent().getParent().getParent()).indexOfChild((View)(mobileView.getParent().getParent().getParent()));
                mDragItemStartCallback.changedPosition(view,originalItemPosition,originalPosition,newPos,newColumnPos);
            }
        }
    }

    private void handleColumnSwitchHorizontal(){
        if(can_scroll && last_swap <= System.currentTimeMillis()-mDelaySwap) {
            int columnPos = mParentLayout.indexOfChild(mobileView);
            View leftView = mParentLayout.getChildAt(columnPos - 1);
            View rightView = mParentLayout.getChildAt(columnPos + 1);

            int[] locationRight = new int[2];
            if (rightView != null) {
                rightView.getLocationOnScreen(locationRight);
                if (locationRight[0] < mLastEventX) {
                    //Scroll to the right
                    switchColumnFromPosition(1,mobileView);
                    if (locationRight[0] + (rightView.getWidth() / 2) < mLastEventX) {

                    }
                }
            }

            int[] locationLeft = new int[2];
            if (leftView != null) {
                leftView.getLocationOnScreen(locationLeft);
                if (locationLeft[0] + leftView.getWidth() > mLastEventX) {
                    //Scroll to the right
                    switchColumnFromPosition(-1,mobileView);
                    //mRootLayout.scrollBy(-1 * 2, 0);
                    if (locationLeft[0] + (leftView.getWidth() / 2) > mLastEventX) {

                    }
                }
            }
        }
    }

    private void switchColumnFromPosition(int change,View view){
        int columnPos = mParentLayout.indexOfChild(view);
        if(columnPos+change >=  0 && last_swap <= System.currentTimeMillis()-mDelaySwap) {
            mParentLayout.removeView(view);
            mParentLayout.addView(view, columnPos + change);
            if(mDragColumnStartCallback != null){
                int newPos = mParentLayout.indexOfChild(view);
                last_swap = System.currentTimeMillis();
                mLastSwap = newPos;
                Handler handlerTimer = new Handler();
                handlerTimer.postDelayed(new Runnable(){
                    public void run() {
                        scrollToColumn(mLastSwap,true);
                    }}, 0);
                mDragColumnStartCallback.changedPosition(((LinearLayout)view).getChildAt(0),originalPosition,newPos);
            }
        }
    }

    private void touchEventsCancelled() {
        if(mCellSubIsMobile){
            mobileView.setVisibility(VISIBLE);
            mHoverCell = null;
            invalidate();
            LinearLayout parentLayout = (LinearLayout)(mobileView.getParent().getParent().getParent().getParent());
            int columnPos = parentLayout.indexOfChild((View)(mobileView.getParent().getParent().getParent()));
            int pos = ((LinearLayout)mobileView.getParent()).indexOfChild(mobileView);
            View tmpView = boardAdapter.columns.get(originalPosition).views.get(originalItemPosition);
            boardAdapter.columns.get(originalPosition).views.remove(originalItemPosition);
            boardAdapter.columns.get(columnPos).views.add(pos, tmpView);
            Object tmpObject = boardAdapter.columns.get(originalPosition).objects.get(originalItemPosition);
            boardAdapter.columns.get(originalPosition).objects.remove(originalItemPosition);
            boardAdapter.columns.get(columnPos).objects.add(pos, tmpObject);
            if (mDragItemStartCallback != null) {
                mDragItemStartCallback.endDrag(mobileView, originalPosition, originalItemPosition, pos, columnPos);
            }
        }else if(mCellIsMobile){
            for(int i = 0;i < mParentLayout.getChildCount();i++){
                BoardItem parentView = (BoardItem)mParentLayout.getChildAt(i);//Gets the parent layout
                for(int j = 0;j < parentView.getChildCount();j++) {
                    View childView = ((LinearLayout) parentView).getChildAt(j);
                    scrollToColumn(originalPosition, true);
                    scaleView(childView, parentView, GLOBAL_SCALE, 1f);
                }
            }
            mobileView.setVisibility(VISIBLE);
            mHoverCell = null;
            invalidate();
            if(mDragColumnStartCallback != null){
                int columnPos = mParentLayout.indexOfChild(mobileView);
                scrollToColumn(columnPos,true);
                BoardAdapter.Column column = boardAdapter.columns.get(originalPosition);
                boardAdapter.columns.remove(originalPosition);
                boardAdapter.columns.add(columnPos,column);
                mDragColumnStartCallback.endDrag(((LinearLayout)mobileView).getChildAt(0),originalPosition,columnPos);
            }
        }

        mDownX = -1;
        mDownY = -1;
        mCellSubIsMobile = false;
        mCellIsMobile = false;
    }

    Handler handler = new Handler();

    public void scaleView(final View v, final BoardItem parent, final float startScale, final float endScale) {
        final Animation anim = new ScaleAnimation(
                startScale, endScale, // Start and end values for the X axis scaling
                startScale, endScale, // Start and end values for the Y axis scaling
                Animation.RELATIVE_TO_SELF,0f, // Pivot point of X scaling
                Animation.RELATIVE_TO_SELF, 0f); // Pivot point of Y scaling
        anim.setFillAfter(true); // Needed to keep the result of the animation
        anim.setFillBefore(false);
        anim.setFillEnabled(true);
        anim.setDuration(ANIM_TIME);
        v.startAnimation(anim);
        final long startTime = System.currentTimeMillis();
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                handler.post(createRunnable(parent,startTime,startScale,endScale));
                parent.init();
                can_scroll = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                parent.setScale(endScale);
                scrollToColumn(mLastSwap,true);
                parent.requestLayout();
                parent.invalidate();
                can_scroll = true;

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private Runnable createRunnable(final BoardItem parent, final long startTime, final float startScale, final float endScale){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long time = System.currentTimeMillis()-startTime;
                float scale_time = time/(float)ANIM_TIME;
                if(scale_time > 1){
                    scale_time = 1;
                }
                scrollToColumn(mLastSwap,true);
                parent.setScale(startScale + (endScale - startScale)*scale_time);
                parent.requestLayout();
                parent.invalidate();
                if(scale_time != 1) {
                    handler.postDelayed(this,10);
                }
            }
        };
        return runnable;
    }

    private void removeParent(View view){
        ViewGroup viewGroup = ((ViewGroup)view.getParent());
        if(viewGroup != null){
            viewGroup.removeView(view);
        }
    }

    public void addColumnList(@Nullable View header, ArrayList<View> items, @Nullable final View footer,int column_pos){
        final BoardItem parent_layout = new BoardItem(getContext());
        parent_layout.setBackgroundColor(background_color);
        final LinearLayout layout = new LinearLayout(getContext());
        final ScrollView scroll_view = new ScrollView(getContext());
        ScrollView.LayoutParams scrollParams = new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT);
        scroll_view.setLayoutParams(scrollParams);
        final LinearLayout layout_children = new LinearLayout(getContext());
        layout_children.setOrientation(LinearLayout.VERTICAL);
        layout.setOrientation(LinearLayout.VERTICAL);
        parent_layout.setOrientation(LinearLayout.VERTICAL);
        if(constWidth) {
            int margin = calculatePixelFromDensity(8);
            LayoutParams params = new LayoutParams(calculatePixelFromDensity(240), LayoutParams.WRAP_CONTENT);
            LayoutParams parent_params = new LayoutParams(calculatePixelFromDensity(240), LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            parent_params.setMargins(margin,margin,margin,margin);
            parent_layout.setLayoutParams(parent_params);
        }else {
            int margin = calculatePixelFromDensity(8);
            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            LayoutParams parent_params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(params);
            parent_params.setMargins(margin,margin,margin,margin);
            parent_layout.setLayoutParams(parent_params);
        }
        parent_layout.addView(layout);
        if(header != null){
            removeParent(header);
            layout.addView(header);
            header.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = mParentLayout.indexOfChild(parent_layout);
                    scrollToColumn(pos,true);
                    headerClickListener.onClick(v,pos);
                }
            });
            if(!boardAdapter.columns.get(column_pos).column_locked) {
                header.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (mDragColumnStartCallback == null) {
                            return false;
                        }
                        originalPosition = mParentLayout.indexOfChild(parent_layout);
                        mDragColumnStartCallback.startDrag(layout, originalPosition);
                        mLastSwap = originalPosition;
                        for (int i = 0; i < mParentLayout.getChildCount(); i++) {
                            BoardItem parentView = (BoardItem) mParentLayout.getChildAt(i);//Gets the parent layout
                            for (int j = 0; j < parentView.getChildCount(); j++) {
                                View childView = ((LinearLayout) parentView).getChildAt(j);
                                scrollToColumn(originalPosition, true);
                                scaleView(childView, parentView, 1f, GLOBAL_SCALE);
                            }
                        }

                        scrollToColumn(originalPosition, false);
                        mCellIsMobile = true;
                        mobileView = (View) (parent_layout);
                        mHoverCell = getAndAddHoverView(mobileView, GLOBAL_SCALE);
                        mobileView.setVisibility(INVISIBLE);
                        return false;
                    }
                });
            }
        }
        parent_layout.addView(scroll_view);
        scroll_view.addView(layout_children);
        for(int i = 0;i < items.size();i++){
            final View view = items.get(i);
            removeParent(view);
            layout_children.addView(view);
            addBoardItem(view, column_pos);

        }
        if(footer != null) {
            removeParent(footer);
            final LinearLayout footer_layout = new LinearLayout(getContext());
            footer_layout.setOrientation(LinearLayout.VERTICAL);
            final LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.setMargins(0,200*-1,0,0);
            layout.addView(footer);
            footer_layout.setLayoutParams(params);
            parent_layout.addView(footer_layout);
            footer.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    int column_pos = mParentLayout.indexOfChild(parent_layout);
                    footerClickListener.onClick(v,column_pos);
                }
            });
            footer.post(new Runnable() {
                @Override
                public void run() {
                    scroll_view.setPadding(0,0,0,footer.getHeight());
                    final LinearLayout.LayoutParams new_params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    new_params.setMargins(0,footer.getHeight()*-1,0,0);
                    removeParent(footer);
                    footer_layout.setLayoutParams(new_params);
                    footer_layout.addView(footer);
                }
            });
        }
        mParentLayout.addView(parent_layout,column_pos);
    }

    public void addBoardItem(final View view, final int column_pos){
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout)view.getParent();
                BoardItem parent_layout = (BoardItem)layout.getParent().getParent();
                int pos = mParentLayout.indexOfChild(parent_layout);
                int i = layout.indexOfChild(view);
                itemClickListener.onClick(view,pos, i);
            }
        });
        if(!boardAdapter.columns.get(column_pos).items_locked) {
            view.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mDragItemStartCallback == null) {
                        return false;
                    }
                    originalPosition = mParentLayout.indexOfChild((LinearLayout) ((LinearLayout) view.getParent()).getParent().getParent());
                    originalItemPosition = ((LinearLayout) view.getParent()).indexOfChild(view);
                    mDragItemStartCallback.startDrag(view, originalPosition, originalItemPosition);
                    mCellSubIsMobile = true;
                    mobileView = (View) (view);
                    mHoverCell = getAndAddHoverView(mobileView, 1);
                    mobileView.setVisibility(INVISIBLE);
                    return false;
                }
            });
        }
    }

    private int calculatePixelFromDensity(float dp){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        float fpixels = metrics.density * dp;
        int pixels = (int) (fpixels + 0.5f);
        return pixels;
    }

    private BitmapDrawable getAndAddHoverView(View v, float scale){
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapWithBorder(v,scale);
        BitmapDrawable drawable = new BitmapDrawable(getResources(),b);
        mHoverCellOriginalBounds = new Rect(left,top,left+w,top+h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);
        drawable.setBounds(mHoverCellCurrentBounds);
        return drawable;
    }

    private Bitmap getBitmapWithBorder(View v, float scale) {
        Bitmap bitmap = getBitmapFromView(v,0);
        Bitmap b = getBitmapFromView(v,1);
        Canvas can = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAlpha(150);
        can.scale(scale,scale,mDownX,mDownY);
        can.rotate(3);
        can.drawBitmap(b,0,0,paint);
        return bitmap;
    }

    private Bitmap getBitmapFromView(View v, float scale){
        double radians = 0.0523599f;
        double s = Math.abs(Math.sin(radians));
        double c = Math.abs(Math.cos(radians));
        int width = (int)(v.getHeight()*s + v.getWidth()*c);
        int height = (int)(v.getWidth()*s + v.getHeight()*c);
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(scale,scale);
        v.draw(canvas);
        return bitmap;
    }

    private Rect rotatedBounds(Rect tmp,double radians){
        double s = Math.abs(Math.sin(radians));
        double c = Math.abs(Math.cos(radians));
        int width = (int)(tmp.height()*s + tmp.width()*c);
        int height = (int)(tmp.width()*s + tmp.height()*c);

        return new Rect(tmp.left,tmp.top,tmp.left+width,tmp.top+height);
    }

}
