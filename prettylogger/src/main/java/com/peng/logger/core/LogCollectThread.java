package com.peng.logger.core;

/**
 * Created by MartinPeng on 2016/5/3 0003 14:14.
 * Email:pengyuantao@21move.com
 */

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.peng.logger.listener.OnLogListener;
import com.peng.logger.utils.LogDateUtils;
import com.peng.logger.utils.LogFileUtils;
import com.peng.logger.utils.LogUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;

/**
 * 日志收集线程
 */
public class LogCollectThread extends Thread {
    /**
     * 日志等级：*:v , *:d ,*:i , *:w , *:e , *:f , *:s
     * <p/>
     * 显示当前mPID程序的 E和W等级的日志.
     */
    // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";
    // cmds = "logcat  | grep \"(" + mPID + ")\"";//打印所有日志信息
    // cmds = "logcat -s way";//打印标签过滤信息
    //进程id
    private String proid;
    //缓存的文件
    private File logFile;
    //handler 对象
    private Handler handler;
    //进程类
    private Process process;
    //是否在运行的标记
    private boolean isRunning = true;
    //连接字符(用于连接时间和具体的Log日志)
    private StringBuilder stringBuilder;
    //连接自负（用于连接时发送消息连接使用）
    private StringBuilder cacheBuilder;

    //指定输出的四种
    public static final String cmd = "logcat *:e *:w *:i *:d *:v | grep \"(%s)\"";

    public static final String V = "V";//Verbose
    public static final String D = "D";//Debug
    public static final String I = "I";//Info
    public static final String W = "W";//Warming
    public static final String E = "E";//Error

    //对应的颜色值(默认)
    public static final String COLOR_V = "#BBBBBB";
    public static final String COLOR_D = "#0070BB";
    public static final String COLOR_I = "#48BB31";
    public static final String COLOR_W = "#BBBB23";
    public static final String COLOR_E = "#FF0006";

    public String colorV = COLOR_V;
    public String colorD = COLOR_D;
    public String colorI = COLOR_I;
    public String colorW = COLOR_W;
    public String colorE = COLOR_E;

    public static final String colorFont = "<font size=\'12px\' color=\'%s\' >%s</font>";

    //默认的刷新时间(默认时间2秒)
    public static final int REFRESH_DEFAULT_SECOND = 1;
    //刷新时间
    public long refreshSecond = REFRESH_DEFAULT_SECOND*1000;
    //上一次记录的时间
    private long preTime = 0;
    //是否得到有颜色的文字
    private boolean isColorFont = true;
    //IO流
    private BufferedReader bufferedReader;//读
    private BufferedWriter bufferedWriter;//写

    //pid的格式化
    private DecimalFormat decimalFormat = new DecimalFormat("#0000");
    //默认的保存目录
    public static final String CACHE_DIRECTORY_NAME = "LogCollector";
    //Log信息回调监听器
    private OnLogListener onLogListener;
    //默认Log的类型
    public static final int WHAT_LOG = 1;
    public static final int WHAT_CLEAR = 2;


    public LogCollectThread(String proid, File logFile, Handler handler) {
        this.proid = proid;
        this.logFile = logFile;
        this.handler = handler;
        stringBuilder = new StringBuilder();
        cacheBuilder = new StringBuilder();
    }


    public LogCollectThread(Context context) {
        //获取进程id
        proid = decimalFormat.format(android.os.Process.myPid());
        //初始化缓存路径
        initCacheFile(context.getApplicationContext());
        //初始化字符
        stringBuilder = new StringBuilder();
        cacheBuilder = new StringBuilder();
    }

    public void initCacheFile(Context context) {
        String cacheLogFilePath = null;
        //初始化保存Log的路径
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            cacheLogFilePath= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + CACHE_DIRECTORY_NAME;
            LogFileUtils.creatFileByPath(cacheLogFilePath);
        } else {
            cacheLogFilePath = context.getFilesDir().getAbsolutePath() + File.separator + CACHE_DIRECTORY_NAME;
            LogFileUtils.creatFileByPath(cacheLogFilePath);
        }
        //当前应用包名
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        String packageName = null;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
            packageName = packageInfo.packageName;
            String appLable = packageManager.getApplicationLabel(packageInfo.applicationInfo).toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String parentPath = cacheLogFilePath + File.separator + packageName;
        File parentFile = LogFileUtils.creatFileByPath(parentPath);
        this.logFile = new File(parentFile, LogDateUtils.getDateFormat() + ".log");
    }

    public File getLogFile() {
        return logFile;
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
                initHandler();
            }

            this.onLogListener = onLogListener;
        }
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
     * 初始化Handler
     */
    private void initHandler() {
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


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void stopCollect() {
        isRunning = false;
    }

    public void sendMessage(char cTag, String line) {
        if (handler != null) {
            if (isColorFont) {
                String tag = String.valueOf(cTag);
                if (V.equals(tag)) {
                    line = String.format(colorFont, colorV, line);
                } else if (D.equals(tag)) {
                    line = String.format(colorFont, colorD, line);
                } else if (I.equals(tag)) {
                    line = String.format(colorFont, colorI, line);
                } else if (W.equals(tag)) {
                    line = String.format(colorFont, colorW, line);
                } else if (E.equals(tag)) {
                    line = String.format(colorFont, colorE, line);
                } else {
                    line = String.format(colorFont, "#000000", line);
                }
            }
            //连接字符串
            cacheBuilder.append(line).append("<br/>");
            //对缓存时间进行判断
            if (System.currentTimeMillis()-preTime>refreshSecond){
                Message message = Message.obtain();
                message.what = WHAT_LOG;
                message.obj = cacheBuilder.toString();
                handler.sendMessage(message);
                cacheBuilder.setLength(0);
                preTime = System.currentTimeMillis();
            }

        }
    }

    @Override
    public void run() {
        try {
            process = Runtime.getRuntime().exec(cmd);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()), 1024);
            bufferedWriter = new BufferedWriter(new FileWriter(logFile));
            String line = null;
            while (isRunning && (line = bufferedReader.readLine()) != null) {
                if (!isRunning) {
                    break;
                }

                if (line.length() == 0) {
                    continue;
                }

                if (line.contains(proid)) {
                    //清空
                    stringBuilder.setLength(0);
                    stringBuilder.append(LogDateUtils.getDateFormat()).append("  ").append(line);
                    if (handler != null) {
                        sendMessage(line.charAt(0),stringBuilder.toString());
                    }
                    bufferedWriter.write(stringBuilder.toString());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }

            }

            LogUtils.e("日志---------->执行完毕");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }

            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                    bufferedReader = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (bufferedWriter != null) {
                try {
                    bufferedWriter.close();
                    bufferedWriter = null;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public void setProid(int progressid) {
        this.proid = decimalFormat.format(progressid);
    }
}
