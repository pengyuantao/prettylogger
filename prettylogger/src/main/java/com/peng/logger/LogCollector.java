package com.peng.logger;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import com.peng.logger.listener.OnLogListener;
import com.peng.logger.listener.OnServiceConnectListener;
import com.peng.logger.service.LogCollectService;
import com.peng.logger.utils.LogUtils;

/**
 * Created by MartinPeng on 2016/5/3 0003 18:00.
 * Email:pengyuantao@21move.com
 */
public class LogCollector {

    //当前缓存的实例
    private static LogCollector logCollector;
    //是否已经被绑定
    private boolean isBind = false;
    //客户端的发送Log的标记位
    public static final int WHAT_CLIENT_LOG = 1;
    //从Budle中获取的
    public static final String TAG_LOG_DETAIL = "onLogReceive";
    //每个时间段需要打印的Log
    private OnLogListener logListener;
    //服务端的信使
    private Messenger serviceMessenger;
    //Context引用
    private Context context;
    //客户端的Handler，用于接收服务端发送过来的消息
    private Handler clientHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_CLIENT_LOG:
                    if (logListener != null) {
                        Bundle bundle = msg.getData();
                        String log = bundle.getString(TAG_LOG_DETAIL);
                        logListener.onReceiverLogLine(log);
                    }
                    break;
                default:
                    break;
            }
        }
    };
    //服务链接监听器
    private LogServiceConnection connection;

    public LogCollector(Context context) {
        this.context = context.getApplicationContext();
        connection = new LogServiceConnection();
    }


    /**
     * 单例模式获取对象
     *
     * @param context
     * @return
     */
    public static LogCollector getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context must not null");
        }

        if (logCollector == null) {
            synchronized (LogCollector.class) {
                if (logCollector == null) {
                    logCollector = new LogCollector(context);
                }
            }
        } else {
            logCollector.setContext(context);
        }
        return logCollector;
    }

    /**
     * 开启这个记录log的服务
     */
    public void start() {
        LogCollectService.startService(context, android.os.Process.myPid());
    }

    public void setContext(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * 停止记录服务(将所有的引用对象至null)
     */
    public void stop() {
        //取消绑定
        if (connection != null && isBind) {
            LogCollectService.unBindService(context, connection);
            LogUtils.e("开始取消绑定");
        }
        LogUtils.e("ServiceMessenger当前状态" + serviceMessenger);
        if (serviceMessenger != null) {
            //开始销毁记录服务
            Message message = Message.obtain();
            message.what = LogCollectService.WHAT_SERVICE_STOP;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException e) {
                isBind = false;
                e.printStackTrace();
            }
            serviceMessenger = null;
        } else {
            LogCollectService.stopService(context);
        }
        logCollector = null;
        context = null;
        logListener = null;
        clientHandler = null;
    }

    /**
     * 注册日志监听器
     */
    public void registerLogListener(OnLogListener logListener) {
        this.logListener = logListener;
        LogUtils.e("serviceMessenger  在  registerLogListener  状态 ：" + serviceMessenger);
        //注册Log监听日志
        if (serviceMessenger == null) {
            connection.connectListener = new OnServiceConnectListener() {
                @Override
                public void onConnected(ComponentName name) {
                    sendClientToService();
                }

                @Override
                public void onDisconnected(ComponentName name) {

                }
            };
            LogCollectService.bindService(context, connection);

        } else {
            sendClientToService();
        }
    }

    /**
     * 发送客户端的信使到服务端
     */
    private void sendClientToService() {
        Messenger clientMessenger = new Messenger(clientHandler);
        Message clientMessage = Message.obtain();
        clientMessage.replyTo = clientMessenger;
        clientMessage.what = LogCollectService.WHAT_REGISTER_LISTENER;
        try {
            serviceMessenger.send(clientMessage);
        } catch (RemoteException e) {
            isBind = false;
            e.printStackTrace();
        }
    }


    /**
     * 取消注册日志监听器
     */
    public void unregisterLogListener() {
        //注册Log监听日志

        if (connection != null && isBind) {
            LogCollectService.unBindService(context, connection);
        }

        logListener = null;
        connection = null;
    }




    /**
     * 连接监听器
     */
    public class LogServiceConnection implements ServiceConnection {
        //连接监听器
        public OnServiceConnectListener connectListener;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //找个房间照顾来自服务的使者
            serviceMessenger = new Messenger(service);
            isBind = true;
            //通知我方人员开始准备饭菜了
            if (connectListener != null) {
                connectListener.onConnected(name);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtils.e("使者已经走了。。。。。");
            isBind = false;
            if (connectListener != null) {
                connectListener.onDisconnected(name);
            }

        }
    }


}
