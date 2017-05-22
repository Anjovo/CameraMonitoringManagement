package com.android.zhsl.activity;

import android.Manifest;
import android.view.View;

import com.android.zhsl.R;
import com.android.zhsl.utils.LoginUtils;
import com.android.zhsl.views.RadiationView;
import com.ltf.mytoolslibrary.viewbase.andreabaccega.widget.FormEditText;
import com.ltf.mytoolslibrary.viewbase.base.ActivityTitleBase;
import com.ltf.mytoolslibrary.viewbase.permission.CheckPermissionUtils;
import com.ltf.mytoolslibrary.viewbase.utils.AnyIdCardCheckUtils;
import com.ltf.mytoolslibrary.viewbase.utils.editext_format.FormEditTextCheckInputUtlis;
import com.ltf.mytoolslibrary.viewbase.utils.show.T;

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

    @Override
    protected void initTitle() {
        rv.setMinRadius(40);// 辐射半径
        rv.startRadiate();// 开始辐射

        if(LoginUtils.getLoginUtils().isLogin(this)){
            setEditTextContent();
        }
    }

    /**
     * 取出 sharedpreference的登录信息并显示
     */
    private void setEditTextContent(){
        String[] loginInfo = LoginUtils.getLoginUtils().getEditTextContent(this);
        if(loginInfo != null){
            ip.setText(loginInfo[0]);
            port.setText(loginInfo[1]);
            password.setText(loginInfo[2]);
            user.setText(loginInfo[3]);
        }
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
                LoginUtils.getLoginUtils().login(ActivityLogin.this, ip.getText().toString(),
                        port.getText().toString().trim(), password.getText().toString(),
                        user.getText().toString(), new LoginUtils.onBackLoginResult() {
                            @Override
                            public void onBackLoginResultLisnner(boolean isLoginSuccessd) {
                                if(isLoginSuccessd){
                                    LoginUtils.getLoginUtils().jumpToItemListActivity(ActivityLogin.this);
                                }
                            }
                        });
            }
        },new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        ,Manifest.permission.WRITE_EXTERNAL_STORAGE});

    }
}
