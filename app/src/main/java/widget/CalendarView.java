package widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.arisaid.calendarview.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CalendarView extends View {

    // 列的数量
    private static final int NUM_COLUMNS = 7;
    // 行的数量
    private static final int NUM_ROWS = 6;
    /**
     * 可选日期数据
     */
    private List<String> mOptionalDates = new ArrayList<>();

    /**
     * 以选日期数据
     */
    private List<String> mSelectedDates = new ArrayList<>();

    // 背景颜色
    private int mBgColor = Color.parseColor("#ffffff");
    // 天数默认颜色
    private int mDayNormalColor = Color.parseColor("#999999");
    // 天数不可选颜色
    private int mDayNotOptColor = Color.parseColor("#999999");
    // 天数选择后颜色
    private int mDayPressedColor = Color.WHITE;

    private int mDayTodayNormal = Color.parseColor("#FFD7DB");
    private int mDayCanOptColor = Color.parseColor("#f54548");

    private int mDayNextOrLastOptColor = Color.parseColor("#cccccc");

    // 天数字体大小
    private int mDayTextSize = 14;

    // 介绍字体大小
    private int mDayDescTextSize = 10;

    // 是否可以被点击状态
    private boolean mClickable = true;

    private DisplayMetrics mMetrics;
    private Paint mPaint, mBgPaint, mDescPaint;
    private int mCurYear;
    private int mCurMonth;
    private int mCurDate;

    private int mSelYear;
    private int mSelMonth;
    private int mSelDate;
    private int mColumnSize;
    private int mRowSize;
    private int[][] mDays;

    // 当月一共有多少天
    private int mMonthDays;
    // 当月第一天位于周几
    private int mWeekNumber;

    // 保存行列坐标实现不同类别日期的点击事件
    Map<String, String> lastMonthRC;// 上个月日期的行列数
    Map<String, String> nowMonthRC; // 本月日期的行列数
    Map<String, String> nextMonthRC; // 下个月日期的行列数

    public CalendarView(Context context) {
        super(context);
        init();
    }

    public CalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 获取手机屏幕参数
        mMetrics = getResources().getDisplayMetrics();
        // 创建画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDescPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.parseColor("#f54548"));
        mBgPaint.setStyle(Paint.Style.FILL);
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        mCurYear = calendar.get(Calendar.YEAR);
        mCurMonth = calendar.get(Calendar.MONTH);
        mCurDate = calendar.get(Calendar.DATE);
        setSelYTD(mCurYear, mCurMonth, mCurDate);

        lastMonthRC = new HashMap<>();
        nowMonthRC = new HashMap<>();
        nextMonthRC = new HashMap<>();
    }

    private String dexc = "可预约";

    @Override
    protected void onDraw(Canvas canvas) {
        initSize();
        int maxRaw = -1;

        // 绘制背景
        mPaint.setColor(mBgColor);
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), mPaint);

        mDays = new int[6][7];
        // 设置绘制字体大小
        mPaint.setTextSize(mDayTextSize * mMetrics.scaledDensity);
        Paint.FontMetrics fm = mPaint.getFontMetrics();
        int normalHeight = (int) Math.ceil(fm.descent - fm.ascent);
        // 设置绘制字体颜色
        mDescPaint.setTextSize(mDayDescTextSize * mMetrics.scaledDensity);
        Paint.FontMetrics fontMetrics = mDescPaint.getFontMetrics();
        int descHeight = (int) Math.ceil(fontMetrics.descent - fm.ascent);
        String dayStr;
        // 获取当月一共有多少天
        mMonthDays = DateUtils.getMonthDays(mSelYear, mSelMonth);
        // 获取当月第一天位于周几
        mWeekNumber = DateUtils.getFirstDayWeek(mSelYear, mSelMonth);


        for (int day = 0; day < mMonthDays; day++) {
            dayStr = String.valueOf(day + 1);
            int column = (day + mWeekNumber - 1) % 7;
            int row = (day + mWeekNumber - 1) / 7;
            maxRaw = row;
            mDays[row][column] = day + 1;
            int startX = (int) (mColumnSize * column + (mColumnSize - mPaint.measureText(dayStr)) / 2);
            int startY = (int) (mRowSize * row + mRowSize / 3 + normalHeight / 3);
            int startDescX = (int) (mColumnSize * column + (mColumnSize - mDescPaint.measureText(dexc)) / 2);
            int startDescY = (int) (mRowSize * row + mRowSize * 2 / 3 + descHeight / 5);
            // 判断当前天数是否可选
            if (mOptionalDates.contains(getSelData(mSelYear, mSelMonth, mDays[row][column]))) {
                nowMonthRC.put(row + "-" + column, mSelYear + "-" + (mSelMonth + 1) + "-" + dayStr);
                // 可选，继续判断是否是点击过的
                if (!mSelectedDates.contains(getSelData(mSelYear, mSelMonth, mDays[row][column]))) {
                    // 没有点击过，绘制默认背景
                    mBgPaint.setColor(mBgColor);
                    canvas.drawCircle(mColumnSize * column + mColumnSize / 2, mRowSize * row + mRowSize / 2, mColumnSize * 5 / 12, mBgPaint);
                    mPaint.setColor(mDayCanOptColor);
                    canvas.drawText(dayStr, startX, startY, mPaint);
                    mDescPaint.setColor(mDayNormalColor);
                    canvas.drawText(dexc, startDescX, startDescY, mDescPaint);

                } else {
                    // 点击过，绘制点击过的背景
                    mBgPaint.setColor(mDayCanOptColor);
                    canvas.drawCircle(mColumnSize * column + mColumnSize / 2, mRowSize * row + mRowSize / 2, mColumnSize * 5 / 12, mBgPaint);
                    mPaint.setColor(mDayPressedColor);
                    canvas.drawText(dayStr, startX, startY, mPaint);
                    mDescPaint.setColor(mDayPressedColor);
                    canvas.drawText(dexc, startDescX, startDescY, mDescPaint);
                }
                // 绘制天数
            } else {
                if (Integer.parseInt(dayStr) < DateUtils.getDayOfMonth() && DateUtils.getMonthOfYear() == mSelMonth && DateUtils.getYear() == mSelYear) {
                    mPaint.setColor(mDayNextOrLastOptColor);
                } else {
                    mPaint.setColor(mDayNotOptColor);
                }
                canvas.drawText(dayStr, startX, startY, mPaint);
            }
            if (DateUtils.getCurDate().equals(mSelYear + "-" + (mSelMonth + 1) + "-" + dayStr)) {
                if (mSelectedDates.size() > 0) {
                    mBgPaint.setColor(mDayTodayNormal);
                    mPaint.setColor(mDayCanOptColor);
                    mDescPaint.setColor(mDayCanOptColor);
                } else {
                    mBgPaint.setColor(mDayCanOptColor);
                    mPaint.setColor(mDayPressedColor);
                    mDescPaint.setColor(mDayPressedColor);
                }
                canvas.drawCircle(mColumnSize * column + mColumnSize / 2, mRowSize * row + mRowSize / 2, mColumnSize * 5 / 12, mBgPaint);
                canvas.drawText(dayStr, startX, startY, mPaint);
                startDescX = (int) (mColumnSize * column + (mColumnSize - mDescPaint.measureText("今天")) / 2);
                canvas.drawText("今天", startDescX, startDescY, mDescPaint);
            }
        }

        // 上个月
        // 获取显示上个月的日期
        int lastLeastDays = mWeekNumber - 1;
        int lastDays;
        if (mSelMonth == 0) {
            lastDays = DateUtils.getMonthDays(mSelYear - 1, 11);
        } else {
            lastDays = DateUtils.getMonthDays(mSelYear, mSelMonth - 1);
        }
        if (lastLeastDays > 0) {
            for (int i = lastLeastDays; i > 0; i--) {
                dayStr = String.valueOf(lastDays - i + 1);
                int column = (lastLeastDays - i) % 7;
                int row = 0;
                if (mSelMonth == 0) {
                    lastMonthRC.put(row + "-" + column, (mSelYear - 1) + "-" + 12 + "-" + dayStr);
                } else {
                    lastMonthRC.put(row + "-" + column, mSelYear + "-" + (mSelMonth + 1) + "-" + dayStr);
                }
                int startX = (int) (mColumnSize * column + (mColumnSize - mPaint.measureText(dayStr)) / 2);
                int startY = (int) (mRowSize * row + mRowSize / 3 + normalHeight / 3);
                mPaint.setColor(mDayNextOrLastOptColor);
                canvas.drawText(dayStr, startX, startY, mPaint);
            }
        }


        // 下个月
        // 现在的内容数
        int nowDays = lastLeastDays + DateUtils.getMonthDays(mSelYear, mSelMonth);
        int j = 1;
        for (int i = nowDays; i < 43; i++) {
            dayStr = String.valueOf(j);
            int lastRaw = i / 7;
            int column = i % 7;
            if (mSelMonth == 11) {
                nextMonthRC.put(lastRaw + "-" + column, (mSelYear + 1) + "-" + "1" + dayStr);
            } else {
                nextMonthRC.put(lastRaw + "-" + column, mSelYear + "-" + mSelMonth + 1 + dayStr);
            }
            nextMonthRC.put(lastRaw + "-" + column, "");
            int startX = (int) (mColumnSize * column + (mColumnSize - mPaint.measureText(dayStr)) / 2);
            int startY = (mRowSize * lastRaw + mRowSize / 3 + normalHeight / 3);
            mPaint.setColor(mDayNextOrLastOptColor);
            canvas.drawText(dayStr, startX, startY, mPaint);
            j++;
        }
    }

    private int downX = 0, downY = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventCode = event.getAction();
        switch (eventCode) {
            case MotionEvent.ACTION_DOWN:
                downX = (int) event.getX();
                downY = (int) event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                if (!mClickable) {
                    return true;
                }
                int upX = (int) event.getX();
                int upY = (int) event.getY();
                if (Math.abs(upX - downX) < 10 && Math.abs(upY - downY) < 10) {
                    performClick();
                    onClick((upX + downX) / 2, (upY + downY) / 2);
                }
                break;
            default:
        }
        return true;
    }

    /**
     * 点击事件
     */
    private void onClick(int x, int y) {
        int row = y / mRowSize;
        int column = x / mColumnSize;
        if (nowMonthRC.containsKey(row + "-" + column)) {
            if (mListener != null) {
                mListener.onClickDateListener(mSelYear, (mSelMonth + 1), mSelDate);
            }
        } else if (lastMonthRC.containsKey(row + "-" + column)) {
            if (mListener != null) {
                mListener.onClickLastMonthListener();
            }
        } else if (nextMonthRC.containsKey(row + "-" + column)) {
            if (mListener != null) {
                mListener.onClickNextMonthListener();
            }
        }
        setSelYTD(mSelYear, mSelMonth, mDays[row][column]);
        // 判断是否点击过
        boolean isSelected = mSelectedDates.contains(getSelData(mSelYear, mSelMonth, mSelDate));
        // 判断是否可以添加
        boolean isCanAdd = mOptionalDates.contains(getSelData(mSelYear, mSelMonth, mSelDate));
        if (isCanAdd) {
            mSelectedDates.clear();
            mSelectedDates.add(getSelData(mSelYear, mSelMonth, mSelDate));
        }

        invalidate();
    }

    /**
     * 初始化列宽和高
     */
    private void initSize() {
        // 初始化每列的大小
        mColumnSize = getWidth() / NUM_COLUMNS;
        // 初始化每行的大小
        mRowSize = getHeight() / NUM_ROWS;
    }

    /**
     * 设置可选择日期
     *
     * @param dates 日期数据
     */
    public void setOptionalDate(List<String> dates) {
        this.mOptionalDates = dates;
        invalidate();
    }

    /**
     * 设置已选日期数据
     */
    public void setSelectedDates(List<String> dates) {
        this.mSelectedDates = dates;
    }

    /**
     * 获取已选日期数据
     */
    public List<String> getSelectedDates() {
        return mSelectedDates;
    }

    /**
     * 设置日历是否可以点击
     */
    @Override
    public void setClickable(boolean clickable) {
        this.mClickable = clickable;
    }

    /**
     * 设置年月日
     *
     * @param year  年
     * @param month 月
     * @param date  日
     */
    private void setSelYTD(int year, int month, int date) {
        this.mSelYear = year;
        this.mSelMonth = month;
        this.mSelDate = date;
    }

    public void setDate(int year, int month, int day) {
        this.mSelYear = year;
        this.mSelMonth = month;
        this.mSelDate = day;
        invalidate();
    }

    /**
     * 设置上一个月日历
     */
    public void setLastMonth() {
        int year = mSelYear;
        int month = mSelMonth;
        int day = mSelDate;
        // 如果是1月份，则变成12月份
        if (month == 0) {
            year = mSelYear - 1;
            month = 11;
        } else if (DateUtils.getMonthDays(year, month) == day) {
            //　如果当前日期为该月最后一点，当向前推的时候，就需要改变选中的日期
            month = month - 1;
            day = DateUtils.getMonthDays(year, month);
        } else {
            month = month - 1;
        }
        setSelYTD(year, month, day);
        invalidate();
    }

    /**
     * 设置下一个日历
     */
    public void setNextMonth() {
        int year = mSelYear;
        int month = mSelMonth;
        int day = mSelDate;
        // 如果是12月份，则变成1月份
        if (month == 11) {
            year = mSelYear + 1;
            month = 0;
        } else if (DateUtils.getMonthDays(year, month) == day) {
            //　如果当前日期为该月最后一点，当向前推的时候，就需要改变选中的日期
            month = month + 1;
            day = DateUtils.getMonthDays(year, month);
        } else {
            month = month + 1;
        }
        setSelYTD(year, month, day);
        invalidate();
    }

    /**
     * 获取当前展示的年和月份
     *
     * @return 格式：2016-06
     */
    public String getDate() {
        String data;
        if ((mSelMonth + 1) < 10) {
            data = mSelYear + "-0" + (mSelMonth + 1);
        } else {
            data = mSelYear + "-" + (mSelMonth + 1);
        }
        return data;
    }

    public void setMonthOnly() {

        invalidate();
    }

    public void setMonthLast() {

        invalidate();
    }

    public void setMonthNext() {

        invalidate();
    }

    /**
     * 获取当前展示的日期
     *
     * @return 格式：20160606
     */
    private String getSelData(int year, int month, int date) {
        String monty, day;
        month = (month + 1);

        // 判断月份是否有非0情况
        if ((month) < 10) {
            monty = "0" + month;
        } else {
            monty = String.valueOf(month);
        }

        // 判断天数是否有非0情况
        if ((date) < 10) {
            day = "0" + (date);
        } else {
            day = String.valueOf(date);
        }
        return year + monty + day;
    }

    private OnClickListener mListener;

    public interface OnClickListener {
        void onClickDateListener(int year, int month, int day);

        void onClickNextMonthListener();

        void onClickLastMonthListener();
    }

    /**
     * 设置点击回调
     */
    public void setOnClickDate(OnClickListener listener) {
        this.mListener = listener;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

}
