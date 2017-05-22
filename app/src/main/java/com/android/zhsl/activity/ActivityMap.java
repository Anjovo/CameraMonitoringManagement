package com.android.zhsl.activity;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.zhsl.R;
import com.android.zhsl.bean.VideoDataBean;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ltf.mytoolslibrary.viewbase.adapter.CommonAdapter1;
import com.ltf.mytoolslibrary.viewbase.base.ActivityTitleBase;
import com.ltf.mytoolslibrary.viewbase.isnull.IsNullUtils;
import com.ltf.mytoolslibrary.viewbase.utils.AppManager;
import com.ltf.mytoolslibrary.viewbase.utils.AutoUtils;
import com.ltf.mytoolslibrary.viewbase.utils.StartToUrlUtils;
import com.ltf.mytoolslibrary.viewbase.utils.show.L;
import com.ltf.mytoolslibrary.viewbase.utils.show.T;
import com.ltf.mytoolslibrary.viewbase.views.CatLoadingView;

import java.util.List;

import static com.android.zhsl.R.id.queren;
import static com.android.zhsl.R.id.quxiao;

/**
 * 作者：李堂飞 on 2017/5/18 13:20
 * 邮箱：litangfei119@qq.com
 */

public class ActivityMap extends ActivityTitleBase{

    MapView mMapView = null;
    BaiduMap mBaiduMap = null;
    private List<VideoDataBean.child> list;

    public LocationClient mLocationClient = null;
    private TextView dingwei;
    private CatLoadingView mShow;
    private LinearLayout viegroup;
    @Override
    protected void initTitle() {
        setUpTitleCentreText("视屏站点地图");
        setUpTitleBack();
        setUpTitleRightSearchBtn("定位");
        list = new Gson().fromJson(getIntent().getStringExtra("list"),new TypeToken<List<VideoDataBean.child>>(){}.getType());

        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        dingwei = (TextView) findViewById(R.id.dingwei);
        viegroup = (LinearLayout) findViewById(R.id.viegroup);

        if(mShow == null){
            mShow = new CatLoadingView(this);
        }

        mBaiduMap = mMapView.getMap();
        //卫星地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);

        mLocationClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );

        initLocation();

        mBaiduMap.setOnMapStatusChangeListener(new BaiduMap.OnMapStatusChangeListener() {
            @Override
            public void onMapStatusChangeStart(MapStatus mapStatus) {

            }

            @Override
            public void onMapStatusChange(MapStatus mapStatus) {
            }

            @Override
            public void onMapStatusChangeFinish(MapStatus mapStatus) {
                updateMap(mapStatus);
            }
        });

        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener()
        {

            @Override
            public boolean onMapPoiClick(MapPoi arg0)
            {
                return false;
            }

            @Override
            public void onMapClick(LatLng arg0)
            {
                viegroup.setVisibility(View.GONE);
                mBaiduMap.hideInfoWindow();

            }
        });
    }

    /***
     * 更新地图上的显示
     * @param mapStatus
     */
    private void updateMap(MapStatus mapStatus) {
        LatLng mCenterLatLng = mapStatus.target;

        double lat = mCenterLatLng.latitude;
        double lng = mCenterLatLng.longitude;
        Log.i("中心点坐标", lat+","+lng);

        InitMap initMap = null;
        if(initMap == null){
            initMap = new InitMap().invoke();
        }
        LatLng ll_right = initMap.getLl_right();
        LatLng ll_left = initMap.getLl_left();

        if(list != null && list.size() > 0){
            for (int i=0;i<list.size();i++){
                Bundle bun = new Bundle();
                bun.putSerializable("bun",list.get(i));
                L.d("经纬度:",list.get(i).getLat()+"--"+list.get(i).getLng());
                if(!IsNullUtils.isNulls(list.get(i).getLat()) && !IsNullUtils.isNulls(list.get(i).getLng())
                        && !"0".equals(list.get(i).getLat())
                        &&!"0".equals(list.get(i).getLng())){

                    //定义Maker坐标点
                    LatLng point = new LatLng(Double.parseDouble(list.get(i).getLat()), Double.parseDouble(list.get(i).getLng()));
//构建Marker图标
                    BitmapDescriptor bitmapd  = BitmapDescriptorFactory
                            .fromResource(R.mipmap.map_body_previewpoint_h);
//构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmapd);

                    if(ll_right.latitude<lat&&lat<ll_left.latitude&&ll_left.longitude<lng&&lng<ll_right.longitude){
                        Marker mMarker = (Marker) mBaiduMap.addOverlay(option);
                        mMarker.setExtraInfo(bun);
                    }
                }
            }
        }

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getExtraInfo() == null){
                    infoWindow(marker.getPosition().latitude,marker.getPosition().longitude,null);
                    return false;
                }
                VideoDataBean.child  b = (VideoDataBean.child) marker.getExtraInfo().getSerializable("bun");
                showPopupWindow(viegroup,b);
                infoWindow(marker.getPosition().latitude,marker.getPosition().longitude,marker.getExtraInfo());
                return false;
            }
        });
    }

    @Override
    public void onTitleRightSerchBtnClick() {
        super.onTitleRightSerchBtnClick();
        if(mShow != null && !mShow.isShowing()){
            mShow.shows();
        }
        mLocationClient.start();
    }

    /**把GPS坐标转换为百度坐标**/
    private LatLng gpsToBaiDu(LatLng sourceLatLng){
        // 将GPS设备采集的原始GPS坐标转换成百度坐标
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.GPS);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        return converter.convert();
    }

    private String message="贵阳市";
    private Handler mhander = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BDLocation location = (BDLocation) msg.obj;
            message = location.getCity()+location.getStreet()+location.getDistrict();
            dingwei.setText("当前定位地址:"+location.getAddrStr());

            //定义Maker坐标点
            LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
//构建Marker图标
            BitmapDescriptor bitmapd  = BitmapDescriptorFactory
                    .fromResource(R.mipmap.dingwei_center);
//构建MarkerOption，用于在地图上添加Marker
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .extraInfo(null)
                    .icon(bitmapd);

            move(location.getLatitude(),location.getLongitude(),option);
        }
    };
    public BDLocationListener myListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if(mShow != null){
                mShow.dismisss();
            }
            if (location.getLocType() == BDLocation.TypeGpsLocation) {
                // GPS定位结果
                sendMessage(location);
            }else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
                // 网络定位结果
                sendMessage(location);
            }else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                T.showShort(getApplicationContext(),"网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                T.showShort(getApplicationContext(),"无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }else{
                T.showShort(getApplicationContext(),"定位失败,错误代码:"+location.getLocType());
            }
            mLocationClient.stop();
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            if(mShow != null){
                mShow.dismisss();
            }
        }
    };

    private void sendMessage(BDLocation location){
        mhander.sendMessage(mhander.obtainMessage(1,location));
    }

    /***移动到指定位置**/
    private void move(double lat,double lng,OverlayOptions option){
        //设定中心点坐标
        LatLng cenpt =  new LatLng(lat,lng);
//定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
                //要移动的点
                .target(cenpt)
                //放大地图到12倍
                .zoom(8)
                .build();
//定义MapStatusUpdate对象，以便描述地图状态将要发生的变化

        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
//改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
//        updateMap(mMapStatus);

        Marker mMarker = (Marker) mBaiduMap.addOverlay(option);
    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        //可选，默认高精度，设置定位模式，高精度，低功耗，仅设备

        option.setCoorType("bd09ll");
        //可选，默认gcj02，设置返回的定位结果坐标系

        int span=1000;
        option.setScanSpan(span);
        //可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的

        option.setIsNeedAddress(true);
        //可选，设置是否需要地址信息，默认不需要

        option.setOpenGps(true);
        //可选，默认false,设置是否使用gps

        option.setLocationNotify(true);
        //可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果

        option.setIsNeedLocationDescribe(true);
        //可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”

        option.setIsNeedLocationPoiList(true);
        //可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到

        option.setIgnoreKillProcess(false);
        //可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        option.SetIgnoreCacheException(false);
        //可选，默认false，设置是否收集CRASH信息，默认收集

        option.setEnableSimulateGps(false);
        //可选，默认false，设置是否需要过滤GPS仿真结果，默认需要

        mLocationClient.setLocOption(option);

        mLocationClient.start();
        if(mShow != null && !mShow.isShowing()){
            mShow.shows();
        }
    }

    @Override
    public void onTitleBackClick() {
        super.onTitleBackClick();
        AppManager.getAppManager().finishActivity(this);
    }

    @Override
    public boolean setIsViewStaueColor() {
        return true;
    }

    @Override
    public String setStatusBarTintResource() {
        return R.color.colortitle+"";
    }

    @Override
    protected int setLayoutId() {
        return R.layout.activity_map;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    class ViewHolder{
        TextView name;
        ListView ListViews;
        Button quxiao;
        Button queren;
    }

    private int nowPosition = -1;//当前选择的位置
    private VideoDataBean.child views;
    private void showPopupWindow(final LinearLayout viegroup, VideoDataBean.child viewss) {
        this.views = viewss;
        ViewHolder viewHolder = null;
        viegroup.setVisibility(View.VISIBLE);
        if (viegroup.getTag() == null)
        {
            viewHolder = new ViewHolder();
            viewHolder.name =(TextView)viegroup.findViewById(R.id.name);
            viewHolder.ListViews=(ListView)viegroup.findViewById(R.id.listview12);
            viewHolder.quxiao =(Button)viegroup.findViewById(quxiao);
            viewHolder.queren =(Button)viegroup.findViewById(queren);

            viegroup.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) viegroup.getTag();
        viewHolder.name.setText(views.getTitleName());


        if(views.getGrandson().size() == 0){
            viewHolder.ListViews.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0));
        }else{
            viewHolder.ListViews.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    AutoUtils.getDisplayHeightValue(250)));
        }

        CommonAdapter1 adapter1 = new CommonAdapter1<VideoDataBean.child.grandson>(this,views.getGrandson(),R.layout.item_video_check_list) {
            @Override
            public void convert(com.ltf.mytoolslibrary.viewbase.adapter.ViewHolder viewHolder, VideoDataBean.child.grandson grandson, int i) {
                viewHolder.setText(R.id.name,grandson.getGrandsonName());
                ImageView isCheck = (ImageView) viewHolder.getConvertView().findViewById(R.id.isCheck);
                if("0".equals(grandson.getIsSelect())){
                    isCheck.setImageResource(R.mipmap.car_select_n);
                }else{
                    isCheck.setImageResource(R.mipmap.car_select_y);
                }

                isCheck.setTag(i);
                isCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int pos = (int) view.getTag();
                        nowPosition = pos;
                        for (int i=0;i<views.getGrandson().size();i++){
                            if(i==pos){
                                if("0".equals(views.getGrandson().get(pos).getIsSelect())){
                                    views.getGrandson().get(pos).setIsSelect("1");
                                }else{
                                    views.getGrandson().get(pos).setIsSelect("0");
                                }
                            }else{
                                views.getGrandson().get(i).setIsSelect("0");
                            }
                        }
                        notifyDataSetChanged();
                    }
                });
            }
        };
        viewHolder.ListViews.setAdapter(adapter1);

        viewHolder.quxiao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viegroup.setVisibility(View.GONE);
            }
        });
        viewHolder.queren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nowPosition == -1){
                    T.showShort(ActivityMap.this,"你还没有选择任何视屏通道");
                    return;
                }
                viegroup.setVisibility(View.GONE);
                if("1".equals(views.getGrandson().get(nowPosition).getIsSelect())){

                    Bundle bun = new Bundle();
                    bun.putString("channelId",views.getGrandson().get(nowPosition).getChannelId());
                    bun.putString("channelName",views.getGrandson().get(nowPosition).getChannelName());
                    bun.putString("VideoName",views.getGrandson().get(nowPosition).getGrandsonName());
                    StartToUrlUtils.getStartToUrlUtils().startToActivity(ActivityMap.this, ActivityVideoPay.class,bun);
                }else{
                    T.showShort(ActivityMap.this,"你还没有选择任何视屏通道");
                }
            }
        });
    }

    /**弹出窗覆盖物**/
    private void infoWindow(double lat,double log,Bundle bun){
        VideoDataBean.child  b = null;
        if(bun != null){
            b = (VideoDataBean.child) bun.getSerializable("bun");
        }
//创建InfoWindow展示的view
        Button button = new Button(getApplicationContext());
        button.setGravity(Gravity.CENTER);
        if(b != null)
            button.setText(b.getTitleName());
        else
            button.setText(message);
        button.setBackgroundResource(R.mipmap.popup);
//定义用于显示该InfoWindow的坐标点
        LatLng pt = new LatLng(lat, log);
//创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, pt, -87);//-47
//显示InfoWindow
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    private class InitMap {
        private LatLng ll_left;
        private LatLng ll_right;

        public LatLng getLl_left() {
            return ll_left;
        }

        public LatLng getLl_right() {
            return ll_right;
        }

        public InitMap invoke() {
            WindowManager wm = ActivityMap.this.getWindowManager();
//      int width = wm.getDefaultDisplay().getWidth();
//      int height = wm.getDefaultDisplay().getHeight();

            DisplayMetrics outMetrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(outMetrics);
            int width = outMetrics.widthPixels;
            int height = outMetrics.heightPixels;

            Log.i("屏幕宽度和高度", width+","+height);

            Point pt = new Point();
            pt.x = 0;
            pt.y = 0;
            ll_left = mBaiduMap.getProjection().fromScreenLocation(pt);
            Log.i("左上角经纬度", ll_left.latitude+","+ll_left.longitude);

            Point ptr = new Point();
            ptr.x = width;
            ptr.y = height;
            ll_right = mBaiduMap.getProjection().fromScreenLocation(ptr);
            Log.i("右下角经纬度", ll_right.latitude+","+ll_right.longitude);
            return this;
        }
    }
}
