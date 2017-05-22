package com.android.zhsl.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.zhsl.MainActivity;
import com.android.zhsl.app.MyAppLication;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Login_Info_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.dpsdk_retval_e;
import com.ltf.mytoolslibrary.viewbase.utils.AppManager;
import com.ltf.mytoolslibrary.viewbase.utils.SharedPreferencesHelper;
import com.ltf.mytoolslibrary.viewbase.utils.show.L;
import com.ltf.mytoolslibrary.viewbase.views.CatLoadingView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 作者：李堂飞 on 2017/5/22 13:14
 * 邮箱：litangfei119@qq.com
 * 登录工具类
 */

public class LoginUtils {

    public static LoginUtils mLoginUtils;
    public static LoginUtils getLoginUtils(){
        if(mLoginUtils == null){
            mLoginUtils = new LoginUtils();
        }
        return mLoginUtils;
    }
    private LoginUtils(){

    }

    /**
     * 取出 sharedpreference的登录信息并显示
     */
    public String[] getEditTextContent(Context c){
        SharedPreferences sp = c.getSharedPreferences("LOGININFO", 0);
        String content = sp.getString("INFO", "");

        Log.i("TestDpsdkCoreActivity", "setEditTextContent" + content);
        return content.split(",");
    }

    /**保存登录信息**/
    public void saveLoginInfo(Context c,String ip,String port,String passWord,String user){
        SharedPreferences sp = c.getSharedPreferences("LOGININFO", 0);
        SharedPreferences.Editor ed = sp.edit();
        StringBuilder sb = new StringBuilder();
        sb.append(ip).append(",").append(port).append(",")
                .append(passWord).append(",").append(user);
        ed.putString("INFO", sb.toString());
        ed.putString("ISFIRSTLOGIN", "false");
        ed.commit();
        Log.i("TestDpsdkCoreActivity", "saveLoginInfo" + sb.toString());
    }

    /**标记是否第一次登入**/
    public boolean isLogin(Context c){
       return  (c.getSharedPreferences("LOGININFO", 0).getString("ISFIRSTLOGIN", "")).equals("false")?true:false;
    }

    public void jumpToItemListActivity(Activity activity)
    {
        Intent intent = new Intent();
        intent.setClass(activity, MainActivity.class);
        //intent.setClass(this, ItemListActivity.class);
        activity.startActivity(intent);
        AppManager.getAppManager().finishActivity(activity);
    }

    protected CatLoadingView mProgressDialog;
    public void showLoadingProgress(Activity c) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
        } else {
            mProgressDialog = new CatLoadingView(c);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.shows();
    }

    private String ip;
    private String port;
    private String passWord;
    private String user;
    private onBackLoginResult monBackLoginResult;
    /**登录**/
    public void login(Activity activity,String ip,String port,String passWord,String user,onBackLoginResult monBackLoginResult){
        this.ip = ip;
        this.port = port;
        this.passWord = passWord;
        this.user = user;
        this.monBackLoginResult = monBackLoginResult;

        if(activity != null){
            showLoadingProgress(activity);
        }
        new LoginTask().execute();
    }

    private MyAppLication mAPP = MyAppLication.get();
    class LoginTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... arg0) {               //在此处处理UI会导致异常
//			if (mloginHandle != 0) {
//	    		IDpsdkCore.DPSDK_Logout(m_loginHandle, 30000);
//        		mloginHandle = 0;
//	    	}

            Login_Info_t loginInfo = new Login_Info_t();
            Integer error = Integer.valueOf(0);
            loginInfo.szIp 	= ip.getBytes();
            String strPort 	= port.trim();
            loginInfo.nPort = Integer.parseInt(strPort);
            loginInfo.szUsername = user.getBytes();
            loginInfo.szPassword = passWord.getBytes();
            loginInfo.nProtocol = 2;
            saveLoginInfo(MyAppLication.get().getApplicationContext(),ip,port,passWord,user);
            int nRet = IDpsdkCore.DPSDK_Login(mAPP.getDpsdkCreatHandle(), loginInfo, 30000);
            return nRet;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);
            if(mProgressDialog != null){
                mProgressDialog.dismisss();
            }
            if (result == 0) {

                //登录成功，开启GetGPSXMLTask线程
                new GetGPSXMLTask().execute();

                Log.d("DpsdkLogin success:",result+"");
                IDpsdkCore.DPSDK_SetCompressType(mAPP.getDpsdkCreatHandle(), 0);
                SharedPreferencesHelper.saveSharedPreferencestStringUtil(MyAppLication.get().getApplicationContext(),"m_pDLLHandle",0,mAPP.getDpsdkCreatHandle()+"");
                mAPP.setLoginHandler(1);
                //	m_loginHandle = 1;
                if(monBackLoginResult != null){
                    monBackLoginResult.onBackLoginResultLisnner(true);
                }
            } else {
                Log.d("DpsdkLogin failed:",result+"");
                if(1000424 == result){//密码无效
                    Toast.makeText(MyAppLication.get().getApplicationContext(), "密码输入错误", Toast.LENGTH_SHORT).show();
                }else if(1000423 == result){
                    Toast.makeText(MyAppLication.get().getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
                }else if(3 == result){
                    Toast.makeText(MyAppLication.get().getApplicationContext(), "服务器链接失败", Toast.LENGTH_SHORT).show();
                }else if(4 == result){
                    Toast.makeText(MyAppLication.get().getApplicationContext(), "该账户已在别处授权登录", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MyAppLication.get().getApplicationContext(), "登录失败,请联系管理员(错误代码:" + result+")", Toast.LENGTH_SHORT).show();
                }
                mAPP.setLoginHandler(0);
                if(monBackLoginResult != null){
                    monBackLoginResult.onBackLoginResultLisnner(false);
                }
                //m_loginHandle = 0;
                //jumpToContentListActivity();
            }
        }
    }

    public interface onBackLoginResult{
        void onBackLoginResultLisnner(boolean isLoginSuccessd);
    }

    //读取GPSXMl 模块
    class GetGPSXMLTask extends AsyncTask<Void, Integer, Integer>{

        @Override
        protected Integer doInBackground(Void... params) {
            int nRet = GetGPSXML();
            return nRet;
        }


        @Override
        protected void onPostExecute(Integer result) {
//            Toast.makeText(ActivityLogin.this, "GetGPSXML nRet"+result, 0).show();
            L.d("GetGPSXML nRet"+result);
            super.onPostExecute(result);
        }

    }

    public int GetGPSXML(){
        int res = -1;
        Return_Value_Info_t nGpsXMLLen = new Return_Value_Info_t();
        int nRet = IDpsdkCore.DPSDK_AskForLastGpsStatusXMLStrCount(mAPP.getDpsdkCreatHandle(), nGpsXMLLen, 10*1000);
        if(nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS && nGpsXMLLen.nReturnValue > 1)
        {
            byte[] LastGpsIStatus = new byte[nGpsXMLLen.nReturnValue - 1];
            nRet = IDpsdkCore.DPSDK_AskForLastGpsStatusXMLStr(mAPP.getDpsdkCreatHandle(), LastGpsIStatus, nGpsXMLLen.nReturnValue);

            if(nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS){

                //System.out.printf("获取GPS XML成功，nRet = %d， LastGpsIStatus = [%s]", nRet, new String(LastGpsIStatus));
                Log.d("GetGPSXML", String.format("获取GPS XML成功，nRet = %d， LastGpsIStatus = [%s]", nRet, new String(LastGpsIStatus)));
                try {
                    File file = new File(MyAppLication.LAST_GPS_PATH); // 路径  sdcard/LastGPS.xml
                    FileOutputStream out = new FileOutputStream(file);
                    out.write(LastGpsIStatus);
                    out.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    L.d("MainActivity",e1.toString()+"");
                }
            }else
            {
                //System.out.printf("获取GPS XML失败，nRet = %d", nRet);
                Log.d("GetGPSXML", String.format("获取GPS XML失败，nRet = %d", nRet));
            }
        }else if(nRet == dpsdk_retval_e.DPSDK_RET_SUCCESS && nGpsXMLLen.nReturnValue == 0)
        {
            //System.out.printf("获取GPS XML  XMLlength = 0");
            Log.d("GetGPSXML", "获取GPS XML  XMLlength = 0");
        }
        else
        {
            //System.out.printf("获取GPS XML失败，nRet = %d", nRet);
            Log.d("GetGPSXML", String.format("获取GPS XML失败，nRet = %d", nRet));
        }
        //System.out.println();
        res = nRet;
        return res;
    }
}
