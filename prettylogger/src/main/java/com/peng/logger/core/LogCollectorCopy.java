package com.peng.logger.core;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.peng.logger.listener.OnLogListener;
import com.peng.logger.utils.LogDateUtils;
import com.peng.logger.utils.LogFileUtils;
import com.peng.logger.utils.LogUtils;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 日志收集者(废弃，只是不想删除而已)
 * Created by administrator on 16/5/1.
 */
public class LogCollectorCopy {

    //默认的保存目录
    public static final String CACHE_DIRECTORY_NAME = "LogCollector";
    //默认Log的类型
    public static final int WHAT_LOG = 1;
    public static final int WHAT_CLEAR = 2;
    //日志收集类的单例
    public static LogCollectorCopy logCollector;
    //当前进程的pid
    private String proid;
    //应用程序包名
    private String packageName;
    //当前应用的名称
    private String appLable;
    //缓存Log的目录
    private String cacheLogFilePath;
    //Log信息回调监听器
    private OnLogListener onLogListener;
    //用于向主线程发送消息的Handler
    private Handler handler;
    //采集Log的线程
    private LogCollectThread logCollectThread;
    //pid的格式化
    private DecimalFormat decimalFormat = new DecimalFormat("#0000");


    /**
     * 单例模式返回当前唯一
     *
     * @return
     */
    public static LogCollectorCopy getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }

        if (logCollector == null) {
            synchronized (LogCollectorCopy.class) {
                if (logCollector == null) {
                    logCollector = new LogCollectorCopy(context);
                }
            }
        }
        return logCollector;
    }

    private LogCollectorCopy(Context context) {
        //获取进程id
        proid = decimalFormat.format(android.os.Process.myPid());
        //初始化缓存路径
        initCacheFile(context.getApplicationContext());
        //当前应用包名
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            packageName = packageInfo.packageName;
            appLable = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void initCacheFile(Context context) {
        //初始化保存Log的路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLogFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CACHE_DIRECTORY_NAME;
            LogFileUtils.creatFileByPath(cacheLogFilePath);
        } else {
            cacheLogFilePath = context.getFilesDir().getAbsolutePath() + File.separator + CACHE_DIRECTORY_NAME;
            LogFileUtils.creatFileByPath(cacheLogFilePath);
        }
    }


    /**
     * 开始统计Log
     */
    public void start() {
        Log.i("TAG", "start" + logCollectThread);
        if (logCollectThread == null) {
            String parentPath = cacheLogFilePath + File.separator + packageName;
            File parentFile = LogFileUtils.creatFileByPath(parentPath);
            File childFile = new File(parentFile, LogDateUtils.getDateFormat() + ".log");
            logCollectThread = new LogCollectThread(proid, childFile, handler);
            logCollectThread.start();
        }
    }

    /**
     * 停止统计Log
     */
    public void stop() {
        LogUtils.i("stop" + logCollectThread);
        if (logCollectThread != null) {
            logCollectThread.stopCollect();
            LogUtils.e("Thread是否存活"+logCollectThread.isAlive());
            logCollectThread = null;
        }
    }

    /**
     * 注册监听器
     */
    public void registerLogListener(OnLogListener onLogListener) {
        if (onLogListener != null) {
            if (handler == null) {
                if (Looper.getMainLooper() != Looper.myLooper()) {
                    throw new IllegalArgumentException("use registerLogListener method must in main Thread");
                }
                initHanlder();
            }
            if (logCollectThread != null) {
                logCollectThread.setHandler(handler);
            }
            this.onLogListener = onLogListener;
        }
    }

    /**
     * 初始化Handler
     */
    private void initHanlder() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what != WHAT_LOG || onLogListener == null) {
                    //取消所有的消息
                    removeMessages(msg.what);
                    removeMessages(WHAT_LOG);
                    handler = null;
                    onLogListener = null;
                    return;
                }
                onLogListener.onReceiverLogLine(msg.obj.toString());
            }
        };
    }

    /**
     * 取消日志监听器
     */
    public void unregisterLogListener() {
        if (onLogListener != null) {
            onLogListener = null;
        }

        if (handler != null) {
            handler.sendEmptyMessage(WHAT_CLEAR);
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

}
