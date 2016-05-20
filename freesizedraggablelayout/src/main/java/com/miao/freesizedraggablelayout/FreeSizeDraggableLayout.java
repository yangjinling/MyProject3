package com.miao.freesizedraggablelayout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miaoyunze on 2016/4/9.
 */
public class FreeSizeDraggableLayout extends ViewGroup {
    final String TAG = "FreeSizeDraggableLayout";
    /**
     * click position out of child view of viewgroup
     */
    final int INVALID_POSITION = -1;
    /**
     * unit size of viewgroup, width and height is default 4
     */
    private int mUnitWidthNum = 4;
    private int mUnitHeightNum = 4;
    /**
     * calculate by UnitSize and real size of device
     */
    private int mUnitWidth;
    private int mUnitHeight;
    /**
     * save the data of child views
     */
    private List<DetailView> listViews;
    /**
     * position of clicked, used to calculate the view pressed
     */
    private int mClickX;
    private int mClickY;
    /**
     * a imageview created to move with fingure when a subview in
     * freesizedraggablelayout pressed
     */
    private ImageView mDragImageView;
    /**
     * some params aux to get the real position of ImageView which need to draw
     */
    private int mPoint2ItemLeft;
    private int mPoint2ItemTop;
    private int mOffset2Left;
    private int mOffset2Top;
    private int mStatusHeight;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    /**
     * padding of each child vie
     */
    private int mViewPadding = 5;
    /**
     * index of pressed DetailView in listView
     */
    private int mPressedItem = INVALID_POSITION;
    /**
     * group change flag
     */
    private boolean mGroupChangeEnable = true;
    private long mResponseTime = 300;
    private Handler mHandler = new Handler();
    private boolean mPress = false;
    private Vibrator mVibrator;
    private Runnable mPressHandler = new Runnable() {
        @Override
        public void run() {
            Log.e(TAG, "in runnable");
            mPress = true;
            mVibrator.vibrate(50);
            createPressImageView(listViews.get(mPressedItem).getView(), mClickX, mClickY);
            View viewPress = getChildAt(mPressedItem);
            viewPress.setVisibility(View.INVISIBLE);
        }
    };

    public FreeSizeDraggableLayout(Context context) {
        this(context, null);
    }

    public FreeSizeDraggableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FreeSizeDraggableLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mStatusHeight = getStatusHeight(getContext());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressedItem = getClickedItem(new Point((int) ev.getX(), (int) ev.getY()));
                if (mPressedItem == INVALID_POSITION)
                    return super.dispatchTouchEvent(ev);
                mHandler.postDelayed(mPressHandler, mResponseTime);

                mClickX = (int) ev.getX();
                mClickY = (int) ev.getY();

                mPoint2ItemTop = mClickY - listViews.get(mPressedItem).getView().getTop();
                mPoint2ItemLeft = mClickX - listViews.get(mPressedItem).getView().getLeft();

                mOffset2Top = (int) (ev.getRawY() - mClickY);
                mOffset2Left = (int) (ev.getRawX() - mClickX);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mPress) {
                    //update imageview's position, it will move with finger
                    mWindowLayoutParams.x = (int) ev.getX() - mPoint2ItemLeft + mOffset2Left;
                    mWindowLayoutParams.y = (int) ev.getY() - mPoint2ItemTop + mOffset2Top - mStatusHeight;
                    mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams);

                    int iPrepareChangePosition = getClickedItem(new Point((int) ev.getX(), (int) ev.getY()));
                    //check if the position of finger moved in a child view, if it's true
                    //and child view has same size with pressed view, change their position
                    if (iPrepareChangePosition != INVALID_POSITION) {
                        if (listViews.get(iPrepareChangePosition).bSameSize(listViews.get(mPressedItem))) {
                            changePositionInList(iPrepareChangePosition, mPressedItem);
                        } else if (mGroupChangeEnable) {
                            List<Integer> ilistChange = ChangeableViewGroupExist(iPrepareChangePosition, mPressedItem);
                            if (ilistChange.size() != 0) {
                                changeGroupPos(mPressedItem, iPrepareChangePosition, ilistChange);
                            }
                        }
                        DrawViewsAtList();
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mPressHandler);
                if (mPress) {
                    mWindowManager.removeView(mDragImageView);
                    View viewPress = listViews.get(mPressedItem).getView();
                    viewPress.setVisibility(View.VISIBLE);
                    viewPress.invalidate();
                    mPress = false;
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * if the group make of views has same size with dragged item, all views
     * point params in this group should be reset according the size and position
     * of dragged item
     *
     * @param iPress
     * @param iPrepare
     * @param iTotal
     */
    private void changeGroupPos(int iPress, int iPrepare, List<Integer> iTotal) {
        Point pPress = listViews.get(iPress).getPoint();
        Point pPrepare = listViews.get(iPrepare).getPoint();

        int ixPress = pPress.x;
        int iyPress = pPress.y;

        int iWidth = listViews.get(iPress).getWidthNum();
        int iHeight = listViews.get(iPress).getHeightNum();

        int ixPrepare = pPrepare.x;
        int iyPrepare = pPrepare.y;

        DetailView detailView = listViews.get(iPress);
        detailView.setPoint(new Point(ixPrepare, iyPrepare));
        listViews.set(iPress, detailView);
        //update Point of group
        for (int i = 0; i < iTotal.size(); ++i) {
            int iViewWaitChange = iTotal.get(i);
            detailView = listViews.get(iViewWaitChange);
            Point pTemp = detailView.getPoint();
            if (pTemp.x < ixPress || pTemp.x >= ixPress + iWidth) {
                if (pTemp.x < ixPress) {
                    pTemp.x += iWidth;
                } else
                    pTemp.x -= iWidth;
            }
            if (pTemp.y < iyPress || pTemp.y >= iyPress + iHeight) {
                if (pTemp.y < iyPress)
                    pTemp.y += iHeight;
                else
                    pTemp.y -= iHeight;
            }
            detailView.setPoint(pTemp);
        }
    }

    /**
     * draw child views according to detailView's Position in listViews
     */
    private void DrawViewsAtList() {
        for (int i = 0; i < listViews.size(); ++i) {
            //set child view's layout with padding
            DetailView dvView = listViews.get(i);
            View vChild = getChildAt(i);
            int iL = dvView.getPoint().x * mUnitWidth;
            int iT = dvView.getPoint().y * mUnitHeight;
            int iR = iL + dvView.getWidthNum() * mUnitWidth;
            int iB = iT + dvView.getHeightNum() * mUnitHeight;

            vChild.setLeft(iL + mViewPadding);
            vChild.setTop(iT + mViewPadding);
            vChild.setRight(iR - mViewPadding);
            vChild.setBottom(iB - mViewPadding);
        }
    }

    /**
     * check if there has the viewgroup has same size with dragged item
     * @param iCheckingItem
     * @param iDraggingItem
     * @return
     */
    private List<Integer> ChangeableViewGroupExist(int iCheckingItem, int iDraggingItem) {
        List<Integer> list = new ArrayList<>();
        int xOri = listViews.get(iCheckingItem).getPoint().x;
        int yOri = listViews.get(iCheckingItem).getPoint().y;

        int xDes = xOri + listViews.get(iDraggingItem).getWidthNum();
        int yDes = yOri + listViews.get(iDraggingItem).getHeightNum();

        int iArea = 0;
        for (int x = 0; x < listViews.size(); ++x) {
            DetailView detailView = listViews.get(x);
            Point pBegin = detailView.getPoint();
            Point pEnd = new Point(pBegin.x + detailView.getWidthNum(), pBegin.y + detailView.getHeightNum());
            //if there has any view in the rect(pBegin, pEnd), we store it in list
            if (pBegin.x >= xOri && pBegin.y >= yOri && pEnd.x <= xDes && pEnd.y <= yDes) {
                list.add(x);
                iArea += detailView.getHeightNum() * detailView.getWidthNum();
            }
        }
        //if the area of all views in list is equal to dragged item, it means that we can change them
        if (iArea != listViews.get(iDraggingItem).getHeightNum() * listViews.get(iDraggingItem).getWidthNum()) {
            list.clear();
        }
        return list;
    }

    /**
     * create a image for the pressed view
     *
     * @param v
     * @param x
     * @param y
     */
    private void createPressImageView(View v, int x, int y) {
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;

        mWindowLayoutParams.x = x - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = y - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowLayoutParams.alpha = 0.5f;
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        //create the bitmap of view
        mDragImageView = new ImageView(getContext());
        v.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(v.getDrawingCache());
        mDragImageView.setImageBitmap(bitmap);
        v.destroyDrawingCache();

        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    /**
     * set the list of view in freesizedraggablelayout
     *
     * @param list
     */
    public void setList(List<DetailView> list) {
        listViews = list;
        removeAllViews();
        for (DetailView v : listViews) {
            addView(v.getView());
        }
    }

    /**
     * set the width of viewgroup
     *
     * @param i
     */
    public void setUnitWidthNum(int i) {
        mUnitWidthNum = i;
    }

    /**
     * set the height of viewgroup
     *
     * @param i
     */
    public void setUnitHeightNum(int i) {
        mUnitHeightNum = i;
    }


    /**
     * change position_data of DetailView. It's very important
     * cause if a onLayout is called then freeseizedraggablelayout
     * will redraw all items according to their point member.
     *
     * @param i
     * @param j
     */
    private void changePositionInList(int i, int j) {
        Point p = listViews.get(i).getPoint();
        listViews.get(i).setPoint(listViews.get(j).getPoint());
        listViews.get(j).setPoint(new Point(p.x, p.y));
    }

    /**
     * get the clicked view's index in listView
     *
     * @param p
     * @return
     */
    private int getClickedItem(Point p) {
        int i = INVALID_POSITION;
        for (DetailView view : listViews) {
            View v = view.getView();
            Rect rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
            if (rect.contains(p.x, p.y)) {
                i = listViews.indexOf(view);
                break;
            }
        }
        return i;
    }

    /**
     * set the padding of subviews in layout, default is 5
     *
     * @param i
     */
    public void setsubViewPadding(int i) {
        mViewPadding = i;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //set the unit size
        mUnitWidth = MeasureSpec.getSize(widthMeasureSpec) / mUnitWidthNum;
        mUnitHeight = MeasureSpec.getSize(heightMeasureSpec) / mUnitHeightNum;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int iChildCount = getChildCount();
        for (int i = 0; i < iChildCount; ++i) {
            //set child view's layout with padding
            DetailView dvView = listViews.get(i);
            View vChild = getChildAt(i);
            int iL = dvView.getPoint().x * mUnitWidth;
            int iT = dvView.getPoint().y * mUnitHeight;
            int iR = iL + dvView.getWidthNum() * mUnitWidth;
            int iB = iT + dvView.getHeightNum() * mUnitHeight;
            vChild.layout(iL + mViewPadding, iT + mViewPadding, iR - mViewPadding, iB - mViewPadding);
        }
    }

    /**
     * get height of statusbar
     *
     * @param context
     * @return
     */
    private static int getStatusHeight(Context context) {
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight) {
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }

    /**
     * set if group change is allowed, it's allow default
     * @param b
     */
    private void setGroupChangeEnable(Boolean b) {
        mGroupChangeEnable = b;
    }
}
