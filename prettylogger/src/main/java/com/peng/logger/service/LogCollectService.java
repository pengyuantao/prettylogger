package com.peng.logger.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.move21.prettylogger.R;
import com.peng.logger.LogCollector;
import com.peng.logger.core.LogOperator;
import com.peng.logger.listener.OnLogListener;
import com.peng.logger.ui.LogCollectActivity;
import com.peng.logger.utils.LogUtils;
import com.peng.logger.utils.NotifyHelper;

import java.lang.reflect.Method;

/**
 * Created by MartinPeng on 2016/5/3 0003 17:55.
 * Email:pengyuantao@21move.com
 * 日志收集的服务（处于另外一个进程中）
 */
public class LogCollectService extends Service {

    public static final String TAG = LogCollector.class.getSimpleName();

    //类型为客户端的信使对象
    public static final int WHAT_REGISTER_LISTENER = 0;//用于接收客户端的Messenger对象，注册监听器使用
    public static final int WHAT_SERVICE_STOP = 1;//用于听这个Service
    //服务端的Handler(主线程)
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            onHandlerMessage(msg);
        }
    };
    //服务端信使
    private Messenger serviceMessenger;
    //客户端信使
    private Messenger clientMessenger;
    //log回调监听器
    private OnLogListener onLogListener;

    private void onHandlerMessage(final Message msg) {
        switch (msg.what) {
            case WHAT_REGISTER_LISTENER:
                LogUtils.e("有位国家和我们----建交---了");
                clientMessenger = msg.replyTo;
                onLogListener = new OnLogListener() {
                    @Override
                    public void onReceiverLogLine(String logs) {
                        if (clientMessenger != null) {
                            //TODO  这里需要去发送消息
                            Message message = Message.obtain();
                            Bundle bundle = new Bundle();
                            bundle.putString(LogCollector.TAG_LOG_DETAIL, logs);
                            message.setData(bundle);
                            message.what = LogCollector.WHAT_CLIENT_LOG;
                            try {
                                clientMessenger.send(message);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                LogOperator.getInstance(LogCollectService.this).registerLogListener(onLogListener);
                break;
            case WHAT_SERVICE_STOP:
                //停止当前的这个类
                stopForeground(true);
                LogCollectService.this.stopSelf();
                LogUtils.e("收到销毁的指令");
                break;
            default:
                break;
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i("Service onCreate");
        //将信使和Handler进行关联
        serviceMessenger = new Messenger(handler);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获取进程id
        if (intent != null) {
            int proid = intent.getIntExtra("proid", -1);
            if (proid != -1) {
                LogOperator.getInstance(this).stop();
                LogOperator.getInstance(this).start();
                LogOperator.getInstance(this).setProid(proid);
            }
            LogUtils.e("onStartCommand   被调用" + proid);
        }
        showNotification(this, "prettyLogger by MartinPeng", "点击查看，正在记录日志中", LogCollectActivity.createPendingIntent(this));
        //设置进程id
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    /**
     * 启动Service
     *
     * @param context
     * @param proid
     */
    public static void startService(Context context, int proid) {
        Intent intent = new Intent(context, LogCollectService.class);
        intent.putExtra("proid", proid);
        context.startService(intent);
    }

    public static void stopService(Context context) {
        Intent intent = new Intent(context, LogCollectService.class);
        context.stopService(intent);
    }

    /**
     * 绑定服务
     *
     * @param context
     * @param serviceConnection
     */
    public static void bindService(Context context, ServiceConnection serviceConnection) {
        Intent intent = new Intent(context, LogCollectService.class);
        context.bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    public static void unBindService(Context context, ServiceConnection serviceConnection) {
        if (serviceConnection != null) {
            context.unbindService(serviceConnection);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.i("停止记录日志");
        LogUtils.i("日志路径：" + LogOperator.getInstance(this).getLogSavePath());
        Log.i(TAG, "停止记录日志");
        Log.i(TAG, "日志路径：" + LogOperator.getInstance(this).getLogSavePath());

        LogOperator.getInstance(this).stop();
        LogOperator.getInstance(this).unregisterLogListener();

    }

    @TargetApi(11)
    public void showNotification(Context context, String contentTitle, String contentText, PendingIntent pendingIntent) {
//        Notification notification = NotifyHelper.with(this).vibrate(new long[0]).autoCancel(false).title(contentTitle).message(contentText).largeIcon(R.drawable.leak_canary_notification).smallIcon(R.drawable.leak_canary_notification).click(pendingIntent).build();


        Notification notification = null;
        if (Build.VERSION.SDK_INT < 11) {
            notification = new Notification();
            notification.icon = R.drawable.notification_log_icon_small;
            notification.when = System.currentTimeMillis();
            notification.flags |= 16;

            try {
                Method builder = Notification.class.getMethod("setLatestEventInfo", new Class[]{Context.class, CharSequence.class, CharSequence.class, PendingIntent.class});
                builder.invoke(notification, new Object[]{context, contentTitle, contentText, pendingIntent});
            } catch (Exception var7) {
                throw new RuntimeException(var7);
            }
        } else {
            Notification.Builder builder1 = (new Notification.Builder(context)).setSmallIcon(R.drawable.notification_log_icon_small).setWhen(System.currentTimeMillis()).setContentTitle(contentTitle).setContentText(contentText).setAutoCancel(false).setContentIntent(pendingIntent);

            if (Build.VERSION.SDK_INT < 16) {
                notification = builder1.getNotification();
            } else {
                notification = builder1.build();
            }
            notification.flags = Notification.FLAG_NO_CLEAR;
        }
        //设置为前台服务
        startForeground(-558907665, notification);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        clientMessenger = null;
        onLogListener = null;
        return super.onUnbind(intent);

    }
}
