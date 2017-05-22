package com.android.zhsl.activity;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.zhsl.R;
import com.company.PlaySDK.IPlaySDK;
import com.dh.DpsdkCore.Enc_Channel_Info_Ex_t;
import com.dh.DpsdkCore.Get_RealStream_Info_t;
import com.dh.DpsdkCore.IDpsdkCore;
import com.dh.DpsdkCore.Ptz_Direct_Info_t;
import com.dh.DpsdkCore.Ptz_Operation_Info_t;
import com.dh.DpsdkCore.Return_Value_Info_t;
import com.dh.DpsdkCore.fMediaDataCallback;
import com.ltf.mytoolslibrary.viewbase.CacheFolder.CacheFolderUtils;
import com.ltf.mytoolslibrary.viewbase.base.ActivityTitleBase;
import com.ltf.mytoolslibrary.viewbase.isnull.IsNullUtils;
import com.ltf.mytoolslibrary.viewbase.utils.AppManager;
import com.ltf.mytoolslibrary.viewbase.utils.SharedPreferencesHelper;
import com.ltf.mytoolslibrary.viewbase.utils.show.T;

import butterknife.Bind;

import static com.company.PlaySDK.Constants.PicFormat_JPEG;

/**
 * 作者：李堂飞 on 2017/5/15 13:05
 * 邮箱：litangfei119@qq.com
 * 摄像头资源播放
 */

public class ActivityVideoPay extends ActivityTitleBase{

    private String VideoUrl = "";
    private String channelName = "";
    private String channelId = "";
    @Bind(R.id.sv_player)
    SurfaceView m_svPlayer = null;
    private int m_nPort = 0;

    private Button btRight;
    private Button btTop;
    private Button btBottom;
    private Button btLeft;
    private Button btAddZoom;
    private Button btReduceZoom;
    private Button btAddFocus;
    private Button btReduceFocus;
    private Button btAddAperture;
    private Button btReduceAperture;
    private TextView tongdao;
    private ImageView back;

    private byte[] m_szCameraId = null;
    private int m_pDLLHandle = 0;
    private int m_nSeq = 0;
    private int mTimeOut = 30*1000;
    @Override
    protected void initTitle() {
//        setUpTitleBack();
//        setUpTitleRightSearchBtn("抓图");
//        setUpTitleCentreText(IsNullUtils.isNullBackStr(getIntent().getStringExtra("VideoName"),"视屏线路预览"));
        channelId = IsNullUtils.isNullBackStr(getIntent().getStringExtra("channelId"),"0");
        channelName = IsNullUtils.isNullBackStr(getIntent().getStringExtra("channelName"),"");

        com.ltf.mytoolslibrary.viewbase.utils.show.L.e("视屏预览","VideoName="+channelName+"channelId="+channelId);
        m_pDLLHandle = Integer.parseInt(SharedPreferencesHelper.getSharedPreferencestStringUtil(this,"m_pDLLHandle",0,"1"));
//        m_pDLLHandle = new Return_Value_Info_t().nReturnValue;
        m_szCameraId = channelId.getBytes();
        // 查找控件
        findViews();
        // 设置监听器
        setListener();

        tongdao.setText(IsNullUtils.isNullBackStr(getIntent().getStringExtra("VideoName"),"视屏线路预览"));

        initd();
    }

    private void initd() {
        m_nPort = IPlaySDK.PLAYGetFreePort();
        SurfaceHolder holder = m_svPlayer.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder holder)
            {
                Log.d("xss", "surfaceCreated");
                IPlaySDK.InitSurface(m_nPort, m_svPlayer);
            }

            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height)
            {
                Log.d("xss", "surfaceChanged");
            }

            public void surfaceDestroyed(SurfaceHolder holder)
            {
                Log.d("xss", "surfaceDestroyed");
            }
        });

        final fMediaDataCallback fm = new fMediaDataCallback() {

            @Override
            public void invoke(int nPDLLHandle, int nSeq, int nMediaType,
                               byte[] szNodeId, int nParamVal, byte[] szData, int nDataLen) {

                int ret = IPlaySDK.PLAYInputData(m_nPort, szData, nDataLen);
                if(ret == 1){
                    Log.e("xss","playing success=" + nSeq + " package size=" + nDataLen);
                }else{
                    Log.e("xss","playing failed=" + nSeq + " package size=" + nDataLen);
                }
            }
        };
        startPaly(fm);
    }

    private void startPaly(fMediaDataCallback fm){
        if(!StartRealPlay()){
            Log.e("xss", "StartRealPlay failed!");
            Toast.makeText(getApplicationContext(), "视频预览失败!", Toast.LENGTH_SHORT).show();
            return;
        }

        try{
            Return_Value_Info_t retVal = new Return_Value_Info_t();

            Get_RealStream_Info_t getRealStreamInfo = new Get_RealStream_Info_t();
            //m_szCameraId = etCam.getText().toString().getBytes();

            System.arraycopy(m_szCameraId, 0, getRealStreamInfo.szCameraId, 0, m_szCameraId.length);
            //getRealStreamInfo.szCameraId = "1000096$1$0$0".getBytes();
            getRealStreamInfo.nMediaType = 1;
            getRealStreamInfo.nRight = 0;
            getRealStreamInfo.nStreamType = 1;
            getRealStreamInfo.nTransType = 1;
            Enc_Channel_Info_Ex_t ChannelInfo = new Enc_Channel_Info_Ex_t();
            IDpsdkCore.DPSDK_GetChannelInfoById(m_pDLLHandle, m_szCameraId, ChannelInfo);
            int ret = IDpsdkCore.DPSDK_GetRealStream(m_pDLLHandle, retVal, getRealStreamInfo, fm, mTimeOut);
            if(ret == 0){
                m_nSeq = retVal.nReturnValue;
                Log.e("xss DPSDK_success!",ret+"");
                Toast.makeText(getApplicationContext(), "视频预览成功,请稍后!", Toast.LENGTH_SHORT).show();
            }else{
                StopRealPlay();
                Log.e("xss DPSDK_failed!",ret+"");
                Toast.makeText(getApplicationContext(), "视频预览失败!", Toast.LENGTH_SHORT).show();
            }
        }catch(Exception e){
            Log.e("xss", e.toString());
        }
    }

    private void findViews(){
        btLeft = (Button)findViewById(R.id.button_ptz_left);
        btRight = (Button)findViewById(R.id.button_right);
        btTop = (Button)findViewById(R.id.button_top);
        btBottom = (Button)findViewById(R.id.button_bottom);
        btAddZoom = (Button)findViewById(R.id.button_add_zoom);
        btReduceZoom = (Button)findViewById(R.id.button_reduce_zoom);
        btAddFocus = (Button)findViewById(R.id.button_add_focus);
        btReduceFocus = (Button)findViewById(R.id.button_reduce_focus);
        btAddAperture = (Button)findViewById(R.id.button_add_aperture);
        btReduceAperture = (Button)findViewById(R.id.button_reduce_aperture);
        m_svPlayer = (SurfaceView)findViewById(R.id.sv_player);
        tongdao = (TextView) findViewById(R.id.tongdao);
        back = (ImageView) findViewById(R.id.back);
    }

    @Override
    public void onTitleRightSerchBtnClick() {
        super.onTitleRightSerchBtnClick();
        captureBitmap();
    }

    @Override
    public void onBackPressed() {
        back();
        super.onBackPressed();
    }

    private void back(){
        int ret = IDpsdkCore.DPSDK_CloseRealStreamBySeq(m_pDLLHandle, m_nSeq, mTimeOut);
        if(ret == 0){
            Log.e("xss","DPSDK_CloseRealStreamByCameraId success!");
            Toast.makeText(getApplicationContext(), "Close video success!", Toast.LENGTH_SHORT).show();
        }else{
            Log.e("xss","DPSDK_CloseRealStreamByCameraId failed! ret = " + ret);
            Toast.makeText(getApplicationContext(), "Close video failed!", Toast.LENGTH_SHORT).show();
        }
        StopRealPlay();
        AppManager.getAppManager().finishActivity(ActivityVideoPay.this);
    }

    private void setListener(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                back();
            }
        });

        btLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = false;
                    ptzDirectInfo.nDirect = 3;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = true;
                    ptzDirectInfo.nDirect = 3;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }

                return false;
            }
        });

        btRight.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = false;
                    ptzDirectInfo.nDirect = 4;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = true;
                    ptzDirectInfo.nDirect = 4;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }

                return false;
            }
        });

        btTop.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = false;
                    ptzDirectInfo.nDirect = 1;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = true;
                    ptzDirectInfo.nDirect = 1;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }

                return false;
            }
        });

        btBottom.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = false;
                    ptzDirectInfo.nDirect = 2;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Direct_Info_t ptzDirectInfo = new Ptz_Direct_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzDirectInfo.szCameraId, 0, m_szCameraId.length);
                    ptzDirectInfo.bStop = true;
                    ptzDirectInfo.nDirect = 2;
                    ptzDirectInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzDirection(m_pDLLHandle, ptzDirectInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzDirection success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzDirection failed!");
                    }
                }

                return false;
            }
        });

        btAddZoom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = false;
                    ptzOperationInfo.nOperation = 0;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = true;
                    ptzOperationInfo.nOperation = 0;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }

                return false;
            }
        });

        btReduceZoom.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = false;
                    ptzOperationInfo.nOperation = 3;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = true;
                    ptzOperationInfo.nOperation = 3;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }

                return false;
            }
        });

        btAddFocus.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = false;
                    ptzOperationInfo.nOperation = 1;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = true;
                    ptzOperationInfo.nOperation = 1;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }

                return false;
            }
        });

        btReduceFocus.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = false;
                    ptzOperationInfo.nOperation = 4;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = true;
                    ptzOperationInfo.nOperation = 4;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }

                return false;
            }
        });

        btAddAperture.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = false;
                    ptzOperationInfo.nOperation = 2;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = true;
                    ptzOperationInfo.nOperation = 2;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }

                return false;
            }
        });

        btReduceAperture.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                if(arg1.getAction() == MotionEvent.ACTION_DOWN)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = false;
                    ptzOperationInfo.nOperation = 5;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP)
                {
                    Ptz_Operation_Info_t ptzOperationInfo = new Ptz_Operation_Info_t();
                    System.arraycopy(m_szCameraId, 0, ptzOperationInfo.szCameraId, 0, m_szCameraId.length);
                    ptzOperationInfo.bStop = true;
                    ptzOperationInfo.nOperation = 5;
                    ptzOperationInfo.nStep = 4;

                    int ret = IDpsdkCore.DPSDK_PtzCameraOperation(m_pDLLHandle, ptzOperationInfo, mTimeOut);
                    if(ret == 0)
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation success!");
                    }
                    else
                    {
                        Log.e("xss","DPSDK_PtzCameraOperation failed!");
                    }
                }

                return false;
            }
        });
    }

    private String path = CacheFolderUtils.getCacheFolderUtils().setCropCacheFolderBackStr("ZHSL/IMGSTR");
    /**
     * 创建文件夹  保存截图 图片
     */
    private void captureBitmap() {
        int result = IPlaySDK.PLAYCatchPicEx(m_nPort, path, PicFormat_JPEG);
        Log.i("PLAYCatchPicEx", String.valueOf(result));
        if (result > 0) {
            T.showShort(this,"保存成功!");
            saveIntoMediaCore();
        } else {
            T.showShort(this,"保存失败!");
        }
    }

    private void saveIntoMediaCore(){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        //intent.setAction(MEDIA_ROUTER_SERVICE);
        Uri uri = Uri.parse(path);
        intent.setData(uri);
        this.setIntent(intent);
    }

    public void StopRealPlay(){
        try {
            IPlaySDK.PLAYStopSoundShare(m_nPort);
            IPlaySDK.PLAYStop(m_nPort);
            IPlaySDK.PLAYCloseStream(m_nPort);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean StartRealPlay()
    {
        if(m_svPlayer == null)
            return false;

        boolean bOpenRet = IPlaySDK.PLAYOpenStream(m_nPort,null,0,1500*1024) == 0? false : true;
        IPlaySDK.PLAYSetDecodeThreadNum(m_nPort, 4);
        if(bOpenRet)
        {
            boolean bPlayRet = IPlaySDK.PLAYPlay(m_nPort, m_svPlayer) == 0 ? false : true;
            Log.i("StartRealPlay", "StartRealPlay1");
            if(bPlayRet)
            {
                boolean bSuccess = IPlaySDK.PLAYPlaySoundShare(m_nPort) == 0 ? false : true;

                Log.i("StartRealPlay", "StartRealPlay2");
                if(!bSuccess)
                {
                    IPlaySDK.PLAYStop(m_nPort);
                    IPlaySDK.PLAYCloseStream(m_nPort);
                    Log.i("StartRealPlay", "StartRealPlay3");
                    return false;
                }
            }
            else
            {
                IPlaySDK.PLAYCloseStream(m_nPort);
                Log.i("StartRealPlay", "StartRealPlay4");
                return false;
            }
        }
        else
        {
            Log.i("StartRealPlay", "StartRealPlay5");
            return false;
        }

        return true;
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
        return R.layout.activity_real_play;
    }
}
