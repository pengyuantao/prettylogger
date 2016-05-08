package com.peng.logger.listener;

import android.content.ComponentName;

/**
 * Service连接监听器
 * Created by MartinPeng on 2016/5/6 11:18.
 * Email:pengyuantao@21move.com
 */
public interface OnServiceConnectListener {
    //链接
    void onConnected(ComponentName name);
    //断开连接
    void onDisconnected(ComponentName name);

}
