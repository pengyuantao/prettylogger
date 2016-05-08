package com.peng.logger.task;

import android.os.AsyncTask;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.TextView;

import com.move21.prettylogger.R;
import com.peng.logger.cache.HtmlCache;
import com.peng.logger.utils.MD5Utils;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

/**
 * 这个异步任务主要用于格式化Html使用的
 * Created by MartinPeng on 2016/5/3 0003 10:27.
 * Email:pengyuantao@21move.com
 */
public class HtmlFormatTask extends AsyncTask<String,Void,Spanned> {

    //弱引用的TextView
    private WeakReference<TextView> textView;

    private String md5 = null;

    public HtmlFormatTask(TextView textView) {
        this.textView = new WeakReference<TextView>(textView);
    }

    @Override
    protected Spanned doInBackground(String... params) {
        //判断参数的有效性
        if (params == null || params.length == 0) {
            return null;
        }  //取出第一个参数
        String htmlRes = params[0];
        Spanned cacheSpanned = null;
        if (!TextUtils.isEmpty(htmlRes)){
            //格式化MD5值
            md5 = MD5Utils.getmd5Code(htmlRes);
            cacheSpanned = HtmlCache.getInstance().get(md5);
            //判断当前的值是否为null
            if (cacheSpanned == null) {
                //开始进行格式化
                cacheSpanned = Html.fromHtml(htmlRes);
                HtmlCache.getInstance().put(md5, cacheSpanned);
            }
        }
        return cacheSpanned;
    }

    @Override
    protected void onPostExecute(Spanned spanned) {
        if (spanned == null) {
            return;
        }

        if (textView != null && textView.get() != null) {
            //如果任务没有取消
            if (!isCancelled()){
                String md5Cache = (String) textView.get().getTag(R.id.pretty_logger_md5);
                if (!md5.equals(md5Cache)) {
                    textView.get().setText(spanned);
                    textView.get().setTag(R.id.pretty_logger_md5,md5);
                }

            }
        }
    }

}
