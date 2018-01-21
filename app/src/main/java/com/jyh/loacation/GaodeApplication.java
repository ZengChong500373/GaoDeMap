package com.jyh.loacation;

import android.app.Application;
import android.content.Context;

import com.jyh.loacation.utils.CrashHandler;

/**
 * Created by Administrator on 2018/1/21.
 */

public class GaodeApplication extends Application {
    /**
     *
     *
     * */
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        CrashHandler.getInstance().init(mContext);
    }
    /**
     * 全局上下文
     */
    public static Context getContext() {
        return mContext;
    }
}
