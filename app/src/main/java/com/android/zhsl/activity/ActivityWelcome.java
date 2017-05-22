package com.android.zhsl.activity;

import android.content.Intent;
import android.os.Handler;

import com.android.zhsl.R;
import com.android.zhsl.utils.LoginUtils;
import com.ltf.mytoolslibrary.viewbase.base.ActivityTitleBase;
import com.ltf.mytoolslibrary.viewbase.utils.AppManager;

/**
 * 作者：李堂飞 on 2017/5/12 16:29
 * 邮箱：litangfei119@qq.com
 * 欢迎界面
 */

public class ActivityWelcome extends ActivityTitleBase{
    @Override
    protected void initTitle() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                gotoWhere();
            }
        }, 1000);
    }

    private void gotoWhere(){
        if(LoginUtils.getLoginUtils().isLogin(this)){//登录过便自动登录
            String[] str = LoginUtils.getLoginUtils().getEditTextContent(this);
            LoginUtils.getLoginUtils().login(null, str[0], str[1], str[2], str[3], new LoginUtils.onBackLoginResult() {
                @Override
                public void onBackLoginResultLisnner(boolean isLoginSuccessd) {
                    if(isLoginSuccessd){
                        LoginUtils.getLoginUtils().jumpToItemListActivity(ActivityWelcome.this);
                    }else{
                        gotoLogin();
                    }
                }
            });
        }else{
            gotoLogin();
        }
    }

    private void gotoLogin(){
        Intent intent = new Intent(this, ActivityLogin.class);
        startActivity(intent);
        AppManager.getAppManager().finishActivity(this);
    }


    @Override
    public void initisBack() {
        super.initisBack();
        setIsBackUp(false);
    }

    @Override
    public boolean setIsViewStaueColor() {
        return false;
    }

    @Override
    public String setStatusBarTintResource() {
        return "-1";
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_welcome;
    }
}
