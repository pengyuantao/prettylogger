package com.peng.logger.ui;

import android.app.Activity;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Toast;

import com.peng.logger.LogCollector;
import com.peng.logger.adapter.LogListAdapter;
import com.peng.logger.listener.OnLogListener;
import com.peng.logger.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 显示Log的界面
 * Created by MartinPeng on 2016/5/3 0003 18:01.
 * Email:pengyuantao@21move.com
 */
public class LogCollectActivity extends ListActivity implements OnLogListener {

    private List<String> dataList = new ArrayList<>();
    private LogListAdapter listAdapter;
    private AbsListView.OnScrollListener onScrollListener;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private boolean isBottom = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        LogCollector.getInstance(this).registerLogListener(this);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogCollector.getInstance(this).unregisterLogListener();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        listAdapter = new LogListAdapter(getListView(), dataList);
        setListAdapter(listAdapter);
        //滚动监听器
        onScrollListener = new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                isBottom = visibleItemCount==1&&firstVisibleItem+visibleItemCount==totalItemCount?true:firstVisibleItem+visibleItemCount==totalItemCount;
            }
        };
        getListView().setOnScrollListener(onScrollListener);

    }


    /**
     * 创建一个PendingIntent
     *
     * @param context
     * @return
     */
    public static PendingIntent createPendingIntent(Context context) {
        Intent intent = new Intent(context, LogCollectActivity.class);
        intent.setFlags(335544320);
        return PendingIntent.getActivity(context, 1, intent, 0);
    }

    @Override
    public void onReceiverLogLine(String logs) {
        if (dataList.size() > 1000) {
            dataList.clear();
        }
        dataList.add(logs);
        listAdapter.refresh(dataList);
        final int position   = dataList.size() - 1;
        if (isBottom) {
            //滑动到底部
            getListView().smoothScrollToPositionFromTop(position,0,1500);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                        getListView().setSelection(position);
                }
            },1500);
        }
    }

}
