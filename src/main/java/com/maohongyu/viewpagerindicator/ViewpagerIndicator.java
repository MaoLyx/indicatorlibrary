package com.maohongyu.viewpagerindicator;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hongyu on 2017/2/6 下午9:38.
 * 作用：
 */

public class ViewpagerIndicator extends LinearLayout {

    private static final String TAG = "ViewpagerIndicator";

    private int mTriangleWidth;

    private int mTriangleHeight;

    private int mVisibleCount;

    private final int DEFAULT_VISIBLE_COUNT = 4;

    private final float RADIO_TRIANGLE_WITH = 1/6f;

    private int mInitTrianslationX;

    private int mTrianslationX;

    private Paint mPaint;

    private Path mPath;

    private final int DEFAULT_HIGH_LIGHT = Color.WHITE;

    private final int DEFAULT_NORMAL     = Color.DKGRAY;

    private int mTabHighLightColor ;

    private int mTabNormalColor    ;

    public ViewpagerIndicator(Context context) {
        this(context,null);
    }

    public ViewpagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
//        获取自定义属性
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ViewpagerIndicator);
        mVisibleCount = array.getInt(R.styleable.ViewpagerIndicator_visible_count,DEFAULT_VISIBLE_COUNT);
        if (mVisibleCount<0) {
            mVisibleCount = DEFAULT_VISIBLE_COUNT;
        }
        mTabHighLightColor = array.getColor(R.styleable.ViewpagerIndicator_tabHighLightColor,DEFAULT_HIGH_LIGHT);
        mTabNormalColor = array.getColor(R.styleable.ViewpagerIndicator_tabNormalColor,DEFAULT_NORMAL);
        array.recycle();

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(2));
    }



    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mInitTrianslationX+mTrianslationX,getHeight());
        canvas.drawPath(mPath,mPaint);
        canvas.restore();
        super.dispatchDraw(canvas);
    }

    /**
     * 得到屏幕的宽度
     * @return
     */
    public int getScreenWidth() {
        WindowManager systemService = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = systemService.getDefaultDisplay().getWidth();
        return width;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTriangleWidth      = (int) ((w/mVisibleCount)*RADIO_TRIANGLE_WITH);
        mTriangleHeight =  mTriangleWidth/2-3;
        mInitTrianslationX  =  w/mVisibleCount/2 - mTriangleWidth/2;
        initTriangle();

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount==0){
            return;
        }
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            LinearLayout.LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            layoutParams.weight = 0;
            layoutParams.width = getTabWidth();
            child.setLayoutParams(layoutParams);
        }
    }

    /**
     * 初始化三角形
     */
    private void initTriangle() {
        mPath = new Path();
        mPath.moveTo(0,0);
        mPath.lineTo(mTriangleWidth,0);
        mPath.lineTo(mTriangleWidth/2,-mTriangleHeight);
        mPath.close();
    }

    /**
     * 移动操作
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {
        int tabWidth = getWidth()/mVisibleCount;
        mTrianslationX = (int) (tabWidth*(position+offset));
        if (position >= (mVisibleCount-2) && offset > 0 && getChildCount()>mVisibleCount)
        {
            if (mVisibleCount!=1){
                if (position==getChildCount()-2)
                {
                    invalidate();
                    return;
                }
                scrollTo((position-(mVisibleCount-2))*tabWidth+(int)(tabWidth*offset),0);
            } else {
                scrollTo((int) (position * tabWidth + offset * mTrianslationX), 0);
            }
        }
        invalidate();
    }

    /**
     *
     * @return Tab的宽度
     */
    private int getTabWidth() {
        return getScreenWidth()/mVisibleCount;
    }

    /**
     * 动态添加tab
     * @param titleItem Tab的名称
     */
    public void setTitleItem(List<String> titleItem)
    {
        removeAllViews();
        if (titleItem != null && titleItem.size()>0 )
        {
            for (int i = 0;i < titleItem.size();i++) {
                final int j=i;
                View child = generateTabView(titleItem.get(i));
                child.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mViewPager.setCurrentItem(j);
                    }
                });
                addView(child);
            }
        }

    }

    /**
     * 创建Tab Item
     * @param title Tab内容
     * @return
     */
    private View generateTabView(String title) {
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.width = getScreenWidth()/mVisibleCount;
        textView.setText(title);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        textView.setTextColor(Color.DKGRAY);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(lp);
        return textView;
    }

    private ViewPager mViewPager;

    private OnPageChangeListener listener;

    public void setListener(OnPageChangeListener listener) {
        this.listener = listener;
    }

    public interface OnPageChangeListener{

        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);


        void onPageSelected(int position);


        void onPageScrollStateChanged(int state);

    }

    /**
     * 设置联动的Viewpager 在设置
     * @param viewPager
     * @param position
     */
    public void setViewPeger(ViewPager viewPager,int position)
    {
        if (getChildCount()== 0)
        {
            throw new UnsupportedOperationException("请先设置ViewpagerIndicator的Tab");
        }
        this.mViewPager = viewPager;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                scroll(position,positionOffset);
                if (listener != null) {
                    listener.onPageScrolled(position,positionOffset,positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                if (listener != null) {
                    listener.onPageSelected(position);
                }
                setHighLightItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (listener != null) {
                    listener.onPageScrollStateChanged(state);
                }
            }
        });
        mViewPager.setCurrentItem(position);
        setHighLightItem(position);
    }

    /**
     * 设置Tab字体高亮
     * @param position
     */
    private void setHighLightItem(int position) {
        reSetItem();
        View child = getChildAt(position);
        setItemTextColor(child,mTabHighLightColor);
    }

    /**
     * 重置颜色
     */
    private void reSetItem() {
        for (int i = 0; i < getChildCount(); i++) {
            setItemTextColor(getChildAt(i),mTabNormalColor);
        }
    }

    /**
     * 设置Tab字体颜色
     * @param child
     * @param color
     */
    private void setItemTextColor(View child,int color) {
        if (child instanceof TextView) {
            ((TextView) child).setTextColor(color);
        }
    }
}
