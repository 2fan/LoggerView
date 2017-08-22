package cn.leo.loggerview.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Leo on 2017/8/21.
 * 初始化方法：在application的onCreate()事件中写下行代码：
 * registerActivityLifecycleCallbacks(Logger.getLogger(getApplicationContext()));
 * 在APP上唤起debug日志方法，点击事件，快速点击3下，然后慢速点击3下，关闭也是
 */

public class Logger extends FrameLayout implements Application.ActivityLifecycleCallbacks {
    private static boolean debuggable = true; //正式环境(false)不打印日志，也不能唤起app的debug界面
    private static Logger me;
    private static String tag;
    private long timestamp = 0;
    private TextView mTvLog;
    private View mSrcView;
    private int mLongClick;
    private int mShortClick;

    public static void setTag(String tag) {
        Logger.tag = tag;
    }

    private Logger(Context context) {
        super(context);
        tag = context.getApplicationInfo().packageName; //可以自定义
        if (!debuggable) {
            return;
        }
        float v = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 5, getResources().getDisplayMetrics());
        mTvLog = new TextView(context);
        mTvLog.setTextSize(v);
        mTvLog.setBackgroundColor(Color.argb(0x55, 0X00, 0x00, 0x00));
        mTvLog.setTextColor(Color.WHITE);
        mTvLog.setShadowLayer(1, 1, 1, Color.BLACK);
        mTvLog.setVisibility(GONE);
    }

    public static Logger getLogger(Context context) {
        if (me == null) {
            synchronized (Logger.class) {
                if (me == null) {
                    me = new Logger(context);
                }
            }
        }
        return me;
    }

    public static void v(String msg) {
        v(tag, msg);
    }

    public static void d(String msg) {
        d(tag, msg);
    }

    public static void i(String msg) {
        i(tag, msg);
    }

    public static void w(String msg) {
        w(tag, msg);
    }

    public static void e(String msg) {
        e(tag, msg);
    }

    public static void s(String msg) {
        s(tag, msg);
    }

    public static void v(String tag, String msg) {
        me.print(Log.VERBOSE, tag, msg);
    }

    public static void d(String tag, String msg) {
        me.print(Log.DEBUG, tag, msg);
    }

    public static void i(String tag, String msg) {
        me.print(Log.INFO, tag, msg);
    }

    public static void w(String tag, String msg) {
        me.print(Log.WARN, tag, msg);
    }

    public static void e(String tag, String msg) {
        me.print(Log.ERROR, tag, msg);
    }

    public static void s(String tag, String msg) {
        me.print(0, tag, msg);
    }

    private void print(int type, String tag, String msg) {
        if (!debuggable || me == null) return;
        String str = "[" + getTime() + "]" + getLevel(type) + "/" + tag + ":" + msg;
        handler.obtainMessage(type, str).sendToTarget();
        switch (type) {
            case Log.VERBOSE:
                Log.v(tag, msg);
                break;
            case Log.DEBUG:
                Log.d(tag, msg);
                break;
            case Log.INFO:
                Log.i(tag, msg);
                break;
            case Log.WARN:
                Log.w(tag, msg);
                break;
            case Log.ERROR:
                Log.e(tag, msg);
                break;
            case 0:
                System.out.println(tag + ":" + msg);
                break;
        }
    }

    private String getLevel(int type) {
        String[] level = new String[]{"S", "", "V", "D", "I", "W", "E"};
        return level[type];
    }

    private void addText(int type, String text) {
        String[] level = new String[]{"#ffffff", "", "#ffffff", "#ffffff", "#00ff00", "#ffff00", "#ff0000"};
        String str = String.format("<br> <font color=\"" + level[type] + "\">%s \r\n", text);
        mTvLog.append(Html.fromHtml(str));
    }

    private String getTime() {
        String time = new SimpleDateFormat("MM-dd HH:mm:ss.SSS").format(new Date());
        return time;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (debuggable) {
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            mSrcView = decorView.getChildAt(0);
            decorView.removeView(mSrcView);
            me.addView(mSrcView);
            me.addView(mTvLog);
            decorView.addView(me);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        me.removeView(mSrcView);
        me.removeView(mTvLog);
        decorView.removeView(me);
        if (mSrcView != null) {
            decorView.addView(mSrcView);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            long l = SystemClock.uptimeMillis();
            long dis = l - timestamp;
            if (dis < 300 && mShortClick < 2) {
                mShortClick++;
            } else if (dis > 300 && dis < 2000 && mShortClick == 2) {
                mLongClick++;
                if (mLongClick == 3 && mShortClick == 2) {
                    if (mTvLog.getVisibility() == GONE) {
                        mTvLog.setVisibility(VISIBLE);
                    } else {
                        mTvLog.setVisibility(GONE);
                    }
                    clearClick();
                }
            } else {
                clearClick();
            }
            timestamp = SystemClock.uptimeMillis();
        }
        return super.dispatchTouchEvent(ev);

    }

    private void clearClick() {
        mLongClick = 0;
        mShortClick = 0;
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String text = (String) msg.obj;
            addText(msg.what, text);
            int offset = (mTvLog.getLineCount() + 1) * mTvLog.getLineHeight();
            if (offset > mTvLog.getHeight()) {
                mTvLog.setText("");
                addText(msg.what, text);
            }
        }
    };
}
