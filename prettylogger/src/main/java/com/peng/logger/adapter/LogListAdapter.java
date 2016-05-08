package com.peng.logger.adapter;

import android.os.AsyncTask;
import android.widget.AbsListView;
import android.widget.TextView;

import com.move21.prettylogger.R;
import com.peng.logger.task.HtmlFormatTask;

import java.util.Collection;

/**
 * Created by MartinPeng on 2016/5/3 0003 11:45.
 * Email:pengyuantao@21move.com
 */
public class LogListAdapter extends BaseListAdapter<String> {

    public LogListAdapter(AbsListView view, Collection<String> mDatas) {
        this(view, mDatas, 0);
    }

    public LogListAdapter(AbsListView view, Collection<String> mDatas, int itemLayoutId) {
        super(view, mDatas, R.layout.pretty_log_item);
    }

    @Override
    public void convert(AdapterHolder helper, String item, boolean isScrolling, int position) {
        TextView textView = helper.getView(R.id.pretty_logger_item_list);
        //判断是否处于滑动状态
        if (!isScrolling) {
            HtmlFormatTask task = new HtmlFormatTask(textView);
            //并发执行
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
            textView.setTag(R.id.pretty_logger_item_list, task);
        } else {
            HtmlFormatTask task = (HtmlFormatTask) textView.getTag(R.id.pretty_logger_item_list);
            if (task != null && task.getStatus() == AsyncTask.Status.RUNNING) {
                task.cancel(true);
            }
        }
    }
}
