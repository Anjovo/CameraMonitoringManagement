package com.android.zhsl.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.zhsl.MainActivity;
import com.android.zhsl.R;
import com.android.zhsl.app.MyAppLication;
import com.android.zhsl.views.RadiationView;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Login_Info_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.dpsdk_retval_e;
import com.ltf.mytoolslibrary.viewbase.andreabaccega.widget.FormEditText;
import com.ltf.mytoolslibrary.viewbase.base.ActivityTitleBase;
import com.ltf.mytoolslibrary.viewbase.isnull.IsNullUtils;
import com.ltf.mytoolslibrary.viewbase.permission.CheckPermissionUtils;
import com.ltf.mytoolslibrary.viewbase.utils.AnyIdCardCheckUtils;
import com.ltf.mytoolslibrary.viewbase.utils.AppManager;
import com.ltf.mytoolslibrary.viewbase.utils.SharedPreferencesHelper;
import com.ltf.mytoolslibrary.viewbase.utils.editext_format.FormEditTextCheckInputUtlis;
import com.ltf.mytoolslibrary.viewbase.utils.show.L;
import com.ltf.mytoolslibrary.viewbase.utils.show.T;
import com.ltf.mytoolslibrary.viewbase.views.CatLoadingView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;

/**
 * 作者：李堂飞 on 2017/5/15 14:59
 * 邮箱：litangfei119@qq.com
 */

public class ActivityLogin extends ActivityTitleBase {

    @Bind(R.id.bg)
    RadiationView rv;

    @Bind(R.id.activity_login_server)
    FormEditText ip;
    @Bind(R.id.activity_login_port)
    FormEditText port;
    @Bind(R.id.activity_login_user)
    FormEditText user;
    @Bind(R.id.activity_login_password)
    FormEditText password;

    //标记是否第一次登入
    private String isfirstLogin;
    @Override
    protected void initTitle() {
        rv.setMinRadius(40);// 辐射半径
        rv.startRadiate();// 开始辐射

        isfirstLogin = getSharedPreferences("LOGININFO", 0).getString("ISFIRSTLOGIN", "");
        if(isfirstLogin.equals("false")){
            setEditTextContent();
        }
    }

    /**
     * 取出 sharedpreference的登录信息并显示
     */
    private void setEditTextContent(){
        SharedPreferences sp = getSharedPreferences("LOGININFO", 0);
        String content = sp.getString("INFO", "");
        String[] loginInfo = content.split(",");
        if(loginInfo != null){
            ip.setText(loginInfo[0]);
            port.setText(loginInfo[1]);
            password.setText(loginInfo[2]);
            user.setText(loginInfo[3]);
        }
        Log.i("TestDpsdkCoreActivity", "setEditTextContent" + content);
    }

    @Override
    public void initisBack() {
        super.initisBack();
        setIsBackUp(false);
    }

    @Override
    public boolean setIsViewStaueColor() {
        return true;
    }

    @Override
    public String setStatusBarTintResource() {
        return "-1";
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_login;
    }

    public void onLoginClick(View v){

        //验证表单是否为空
        if(!FormEditTextCheckInputUtlis.gotoFormEditTextCheckInputUtlis(ip,port,user,password)){
            return;
        }
        if(!AnyIdCardCheckUtils.getInstance(this).isPassWord(password.getText().toString())){
            T.showShort(this,"密码输入不能为非法字符");
            return;
        }

        CheckPermissionUtils.getSelectPicUpdateUtils().checkPermission(-1, this, false, new CheckPermissionUtils.onBackPermissionResult() {
            @Override
            public void onBackPermissionResult() {
                showLoadingProgress(R.string.login);
                new LoginTask().execute();
            }
        },new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ,Manifest.permission.WRITE_EXTERNAL_STORAGE});

    }

    protected CatLoadingView mProgressDialog;
    protected void showLoadingProgress(int resId) {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
        } else {
            mProgressDialog = new CatLoadingView(this);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.shows();
    }

    private MyAppLication mAPP = MyAppLication.get();
    class LoginTask extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... arg0) {               //在此处处理UI会导致异常
//			if (mloginHandle != 0) {
//	    		IDpsdkCore.DPSDK_Logout(m_loginHandle, 30000);
//        		m_loginHandle = 0;
//	    	}
            Login_Info_t loginInfo = new Login_Info_t();
            Integer error = Integer.valueOf(0);
            loginInfo.szIp 	= ip.getText().toString().getBytes();
            String strPort 	= port.getText().toString().trim();
            loginInfo.nPort = Integer.parseInt(strPort);
            loginInfo.szUsername = user.getText().toString().getBytes();
            loginInfo.szPassword = password.getText().toString().getBytes();
            loginInfo.nProtocol = 2;
            saveLoginInfo();
            int nRet = IDpsdkCore.DPSDK_Login(mAPP.getDpsdkCreatHandle(), loginInfo, 30000);
            return nRet;
        }

        @Override
        protected void onPostExecute(Integer result) {

            super.onPostExecute(result);
            mProgressDialog.dismisss();
            if (result == 0) {

                //登录成功，开启GetGPSXMLTask线程
                new GetGPSXMLTask().execute();

                Log.d("DpsdkLogin success:",result+"");
                IDpsdkCore.DPSDK_SetCompressType(mAPP.getDpsdkCreatHandle(), 0);
                SharedPreferencesHelper.saveSharedPreferencestStringUtil(ActivityLogin.this,"m_pDLLHandle",0,mAPP.getDpsdkCreatHandle()+"");
                mAPP.setLoginHandler(1);
                //	m_loginHandle = 1;
                jumpToItemListActivity();
            } else {
                Log.d("DpsdkLogin failed:",result+"");
                if(1000424 == result){//密码无效
                    Toast.makeText(getApplicationContext(), "密码输入错误", Toast.LENGTH_SHORT).show();
                }else if(1000423 == result){
                    Toast.makeText(getApplicationContext(), "用户不存在", Toast.LENGTH_SHORT).show();
                }else if(3 == result){
                    Toast.makeText(getApplicationContext(), "服务器链接失败", Toast.LENGTH_SHORT).show();
                }else if(4 == result){
                    Toast.makeText(getApplicationContext(), "该账户已在别处授权登录", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "登录失败,请联系管理员(错误代码:" + result+")", Toast.LENGTH_SHORT).show();
                }
                mAPP.setLoginHandler(0);
                //m_loginHandle = 0;
                //jumpToContentListActivity();
            }
        }
    }

    public void jumpToItemListActivity()
    {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        //intent.setClass(this, ItemListActivity.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);
    }

    private void saveLoginInfo(){
        SharedPreferences sp = getSharedPreferences("LOGININFO", 0);
        SharedPreferences.Editor ed = sp.edit();
        StringBuilder sb = new StringBuilder();
        sb.append(ip.getText().toString()).append(",").append(port.getText().toString()).append(",")
                .append(password.getText().toString()).append(",").append(user.getText().toString());
        ed.putString("INFO", sb.toString());
        ed.putString("ISFIRSTLOGIN", "false");
        ed.commit();
        Log.i("TestDpsdkCoreActivity", "saveLoginInfo" + sb.toString());
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
