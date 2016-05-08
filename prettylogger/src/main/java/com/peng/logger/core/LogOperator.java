package com.peng.logger.core;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.peng.logger.listener.OnLogListener;

/**
 * 日志主要操作者
 * Created by administrator on 16/5/1.
 */
public class LogOperator {

    //日志收集类的单例
    public static LogOperator logCollector;
    //收集日志的线程
    private LogCollectThread logCollectThread;
    //引用的Context
    private Context context;

    public LogOperator(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 单例模式返回当前唯一
     *
     * @return
     */
    public static LogOperator getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }
        if (logCollector == null) {
            synchronized (LogOperator.class) {
                if (logCollector == null) {
                    logCollector = new LogOperator(context);
                }
            }
        }
        return logCollector;
    }

    /**
     * 开始统计Log
     */
    public void start() {
        Log.i("TAG", "start" + logCollectThread);
        if (logCollectThread == null) {
            logCollectThread = new LogCollectThread(context);
            logCollectThread.start();
        }
    }

    /**
     * 停止统计Log
     */
    public void stop() {
        Log.i("TAG", "stop" + logCollectThread);
        if (logCollectThread != null) {
            logCollectThread.stopCollect();
            logCollectThread = null;
        }
    }

    /**
     * 注册监听器
     */
    public void registerLogListener(OnLogListener onLogListener) {
        if (onLogListener != null) {
            if (logCollectThread != null) {
                logCollectThread.registerLogListener(onLogListener);
            }
        }
    }

    public String getLogSavePath(){
        if (logCollectThread != null) {
            return logCollectThread.getLogFile().toString();
        }
        return null;
    }


    /**
     * 取消日志监听器
     */
    public void unregisterLogListener() {
        if (logCollectThread != null) {
            logCollectThread.unregisterLogListener();
        }
    }


    /**
     * 设置Log.v颜色值
     *
     * @param colorV
     */
    public void setColorV(String colorV) {
        try {
            //校验当前的颜色值
            Color.parseColor(colorV);
            if (logCollectThread != null) {
                logCollectThread.colorV = colorV;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Log.d颜色值
     *
     * @param colorD
     */
    public void setColorD(String colorD) {
        try {
            //校验当前的颜色值
            Color.parseColor(colorD);
            if (logCollectThread != null) {
                logCollectThread.colorD = colorD;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Log.i颜色值
     *
     * @param colorI
     */
    public void setColorI(String colorI) {
        try {
            //校验当前的颜色值
            Color.parseColor(colorI);
            if (logCollectThread != null) {
                logCollectThread.colorI = colorI;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Log.w颜色值
     *
     * @param colorW
     */
    public void setColorW(String colorW) {
        try {
            //校验当前的颜色值
            Color.parseColor(colorW);
            if (logCollectThread != null) {
                logCollectThread.colorW = colorW;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置Log.e颜色值
     *
     * @param colorE
     */
    public void setColorE(String colorE) {
        try {
            //校验当前的颜色值
            Color.parseColor(colorE);
            if (logCollectThread != null) {
                logCollectThread.colorE = colorE;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置刷时间
     *
     * @param refreshSecond
     */
    public void setRefreshSecond(long refreshSecond) {
        if (refreshSecond > 1000) {
            logCollectThread.refreshSecond = refreshSecond;
        }
    }

    public void setProid(int proid) {
        if (logCollectThread != null) {
            logCollectThread.setProid(proid);
        }
    }

}
