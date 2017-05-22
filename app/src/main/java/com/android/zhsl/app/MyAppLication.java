package com.android.zhsl.app;

import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.fDPSDKStatusCallback;
import com.ltf.mytoolslibrary.viewbase.CacheFolder.CacheFolderUtils;
import com.ltf.mytoolslibrary.viewbase.app.ApplicationBase;
import com.ltf.mytoolslibrary.viewbase.swipebacklayout.app.SwipeBackActivity;
import com.ltf.mytoolslibrary.viewbase.utils.show.L;

/**
 * 作者：李堂飞 on 2017/5/16 13:44
 * 邮箱：litangfei119@qq.com
 */

public class MyAppLication extends ApplicationBase{
    private static final String TAG = "AppApplication";
    private static String path = "";
    private static final String LOG_PATH = path + "DPSDKlog.txt";
    public static final String LAST_GPS_PATH = path + "LastGPS.xml";

    private static MyAppLication _instance;
    private int m_loginHandle = 0; // 标记登录是否成功 1登录成功 0登录失败
    private int m_nLastError = 0;
    private Return_Value_Info_t m_ReValue = new Return_Value_Info_t();

    public static synchronized MyAppLication get() {
        return _instance;
    }

    @Override
    protected boolean setIsOpenCrashErrorMessage() {
        return true;
    }

    @Override
    public String setCrashErrorMessageName() {
        return "ZHSL";
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        SwipeBackActivity.setStupInit(this,1546792512135L);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        path = CacheFolderUtils.getCacheFolderUtils().setCropCacheFolderBackStr("ZHSL/configure");
        L.d(TAG,path);
        initApp();
    }

    /**
     * 全局初始化，在SplashActivity中调用
     */
    public void initApp() {

        // Creat DPSDK
        Log.d("initApp:", m_nLastError + "");
        int nType = 1;
        m_nLastError = IDpsdkCore.DPSDK_Create(nType, m_ReValue);
        Log.d("DpsdkCreate:", m_nLastError + "");

        // set logPath
        m_nLastError = IDpsdkCore.DPSDK_SetLog(m_ReValue.nReturnValue,
                LOG_PATH.getBytes());
        Log.d("DPSDK_SetLog:", m_nLastError + "");

        int ret = IDpsdkCore.DPSDK_SetDPSDKStatusCallback(
                m_ReValue.nReturnValue, new fDPSDKStatusCallback() {

                    @Override
                    public void invoke(int nPDLLHandle, int nStatus) {
                        Log.v("fDPSDKStatusCallback", "nStatus = " + nStatus);
                    }
                });
    }

    @Override
    public void onTerminate() {
        Logout();

        IDpsdkCore.DPSDK_Destroy(getDpsdkCreatHandle());
        super.onTerminate();
    }

    public void Logout() {
        if (getLoginHandler() == 0) {
            return;
        }
        int nRet = IDpsdkCore.DPSDK_Logout(getDpsdkCreatHandle(), 30000);

        if (0 == nRet) {
            // m_loginHandle = 0;
            setLoginHandler(0);
        }
    }

    public int getDpsdkHandle() {
        if (m_loginHandle == 1) // 登录成功，返回PDSDK_Creat时返回的 有效句柄
            return m_ReValue.nReturnValue;
        else
            return 0;
    }

    public int getDpsdkCreatHandle() { // 仅用于获取DPSDK_login的句柄
        return m_ReValue.nReturnValue;
    }

    public void setLoginHandler(int loginhandler) {
        this.m_loginHandle = loginhandler;
    }

    public int getLoginHandler() {
        return this.m_loginHandle;
    }

}
