package com.zin.ra.nsgam.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.zin.ra.base.AppManager;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            //初始化
            AppManager.getInstance().init(this);

            Timer tmpTimer = new Timer();
            tmpTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    try {
                        //获取当前运行包名
                        final String tmpString = AppManager.getInstance().getCurrentPkgName(MainActivity.this);

                        if (tmpString != null) {
                            //当前取得的包名就是栈顶应用包名
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @SuppressLint("NewApi")
                                @Override
                                public void run() {
                                }
                            });
                        }

                    } catch (Throwable e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            }, 5 * 1000, 3 * 1000);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
