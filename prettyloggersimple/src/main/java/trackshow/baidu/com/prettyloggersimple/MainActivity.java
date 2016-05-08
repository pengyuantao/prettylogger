package trackshow.baidu.com.prettyloggersimple;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import com.peng.logger.LogCollector;

public class MainActivity extends AppCompatActivity{

    public static final String TAG = MainActivity.class.getSimpleName();
    private boolean isStart = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new LogThread().start();
        LogCollector.getInstance(this).start();
    }

    /**
     * start Log Thread
     */
    class LogThread extends Thread{
        int i= 0;
        @Override
        public void run() {
            while (true){
                if (isStart) {
                    Log.v(TAG, "输出日志---VVVVVVVVVVV----->  " + i);
                    Log.d(TAG, "输出日志----DD---->  " + i);
                    Log.i(TAG, "输出日志---IIIIII----->  " + i);
                    Log.w(TAG, "输出日志---WWWWWWWWWWWWW----->  " + i);
                    Log.e(TAG, "输出日志----EEEEEEEEEEEEEEEEEEEEEEE---->  " + i);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    i++;
                } else {
                    return;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogCollector.getInstance(this).stop();
        isStart = false;

    }
}
