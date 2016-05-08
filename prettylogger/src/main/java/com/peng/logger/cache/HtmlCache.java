package com.peng.logger.cache;

import android.text.Spanned;
import android.util.LruCache;

/**
 * Created by MartinPeng on 2016/5/3 0003 11:06.
 * Email:pengyuantao@21move.com
 */
public class HtmlCache {

    private LruCache<String, Spanned> lruCache;

    //单例对象
    private static HtmlCache htmlCache;

    /**
     * 获取该单例对象
     * @return
     */
    public static HtmlCache getInstance(){
        if (htmlCache == null) {
            synchronized (HtmlCache.class) {
                if (htmlCache == null) {
                    htmlCache = new HtmlCache();
                }
            }
        }

        return htmlCache;
    }

    public HtmlCache(){
        //获取系统分配给每个应用程序的最大内存，每个应用系统分配32M
        int maxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxMemory / 10;
        //给LruCache分配1/8 4M
        lruCache = new LruCache<String, Spanned>(cacheSize){

            //必须重写此方法，来测量Bitmap的大小
            @Override
            protected int sizeOf(String key, Spanned value) {
                return value.toString().getBytes().length;
            }
        };
    }

    /**
     * 开始存数据
     * @param key
     * @param value
     */
    public void put(String key, Spanned value) {
        lruCache.put(key, value);
    }

    /**
     * 开始取数据
     * @param key
     */
    public Spanned get(String key) {
        return lruCache.get(key);
    }

}
