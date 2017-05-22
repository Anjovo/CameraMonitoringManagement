package com.android.zhsl;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.zhsl.activity.ActivityMap;
import com.android.zhsl.activity.ActivityVideoPay;
import com.android.zhsl.app.MyAppLication;
import com.android.zhsl.bean.ChannelInfoExt;
import com.android.zhsl.bean.TreeNode;
import com.android.zhsl.bean.VideoDataBean;
import com.android.zhsl.utils.GroupListManager;
import com.android.zhsl.views.CustomExpandableListView;
import com.dh.DpsdkCore.IDpsdkCore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ltf.mytoolslibrary.viewbase.base.ActivityTitleBase;
import com.ltf.mytoolslibrary.viewbase.isnull.IsNullUtils;
import com.ltf.mytoolslibrary.viewbase.utils.AppManager;
import com.ltf.mytoolslibrary.viewbase.utils.AutoUtils;
import com.ltf.mytoolslibrary.viewbase.utils.SharedPreferencesHelper;
import com.ltf.mytoolslibrary.viewbase.utils.StartToUrlUtils;
import com.ltf.mytoolslibrary.viewbase.utils.show.L;
import com.ltf.mytoolslibrary.viewbase.utils.show.T;
import com.ltf.mytoolslibrary.viewbase.views.CatLoadingView;
import com.ltf.mytoolslibrary.viewbase.views.UIAlertView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;

public class MainActivity extends ActivityTitleBase {
    // 打印标签
    private static final String TAG = "MainActivity";
    @Bind(R.id.listview12)
    ExpandableListView listview;
    private HashMap<String,String> map = new HashMap<>();
    @Override
    protected void initTitle() {
//        setUpTitleBack();
        setUpTitleCentreText("视屏线路选择");
        setUpTitleRightSearchBtn("地图");

        mVideoDataBean.clear();
        listview.setEmptyView(LayoutInflater.from(this).inflate(R.layout.common_xlistview_null,null));

        if(mShow == null){
            mShow = new CatLoadingView(this);
        }

        map.clear();
        String[] str = getResources().getStringArray(R.array.gps);
        for (int i=0;i<str.length;i++){
            map.put(str[i].split(",")[0],str[i].split(",")[1]+","+str[i].split(",")[2]);
        }
        mShow.shows();
        inits();

        L.d(TAG,"-----------------------initTitle()-----------------");
    }

    @Override
    public void onTitleRightSerchBtnClick() {
        super.onTitleRightSerchBtnClick();
        Bundle bun = new Bundle();
        bun.putString("list", new Gson().toJson(childLists));
        StartToUrlUtils.getStartToUrlUtils().startToActivity(this,ActivityMap.class,bun);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//			// 创建退出对话框
            logout();
        }

        return super.onKeyDown(keyCode, event);
    }

    private MyAppLication mAPP = MyAppLication.get();
    private CatLoadingView mShow;
    private void logout() {
        final UIAlertView delDialog = new UIAlertView(
                this,
                "温馨提示",
                "确认要退出登录么?",
                "取消", "确定");
        delDialog.show();
        delDialog.setClicklistener(new UIAlertView.ClickListenerInterface() {
                                       @Override
                                       public void doLeft() {
                                           delDialog.dismiss();
                                       }

                                       @Override
                                       public void doRight() {
                                           delDialog.dismiss();
                                           mShow.shows();
                                           int in = IDpsdkCore.DPSDK_Logout(mAPP.getDpsdkCreatHandle(),30000);
                                           mShow.dismisss();
                                           if (in == 0){
                                               T.showShort(mAPP.getApplicationContext(),"注销成功");
                                               AppManager.getAppManager().finishAllActivity();
                                               AppManager.getAppManager().AppExit(MainActivity.this);
                                           }else{
                                               T.showShort(mAPP.getApplicationContext(),"注销失败,错误代码:"+in);
                                           }
                                       }
                                   }
        );
    }


    // 获取的树信息
    private TreeNode root = null;
    // 获取实例
    private GroupListManager mGroupListManager = null;
    // 通道列表
    private List<ChannelInfoExt> channelInfoExtList = null;
    private void inits() {
        mGroupListManager = GroupListManager.getInstance();
        channelInfoExtList = mGroupListManager.getChannelList();
        getGroupList();
    }

    // 组织树的头信息
    private byte[] szCoding = null;
    /**
     * <p>
     * 获取组织列表
     * </p>
     *
     * @author fangzhihua 2014-5-12 上午9:56:14
     */
    private void getGroupList() {
        root = mGroupListManager.getRootNode();
        if (root == null) {
            try{
                szCoding = mGroupListManager.loadDGroupInfoLayered();
                root = mGroupListManager.getGroupList(szCoding, mGroupListManager.getRootNode());
            }catch (Exception e){
                L.e(TAG,e.toString());
                return;
            }
        }

        initData(root);
    }

    @Override
    public void onTitleBackClick() {
        super.onTitleBackClick();
        AppManager.getAppManager().finishActivity(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        return R.layout.activity_main;
    }

    private void initData(TreeNode mTreeNode){
        mVideoDataBean.clear();
        childLists.clear();

        if(mTreeNode == null){
            if(IsNullUtils.isNulls(SharedPreferencesHelper.getSharedPreferencestStringUtil(this,"groupList",0,""))){
                T.showShort(this,"数据获取异常");
                return;
            }
            mVideoDataBean = new Gson().fromJson(SharedPreferencesHelper.getSharedPreferencestStringUtil(this,"groupList",0,""),new TypeToken<List<VideoDataBean>>(){}.getType());
        }else{
            for (int k = 0; k < mTreeNode.getChildren().size(); k++) {
                List<VideoDataBean.child> childs = new ArrayList<>();
                for (int i = 0; i < mTreeNode.getChildren().get(k).getChildren().size(); i++) {
                    List<VideoDataBean.child.grandson> grandsons = new ArrayList<>();
                    for (int j = 0; j < mTreeNode.getChildren().get(k).getChildren().get(i).getChildren().size(); j++)
                    {
                        VideoDataBean.child.grandson mgrandson = new VideoDataBean.child.grandson
                                (mTreeNode.getChildren().get(k).getChildren().get(i).getChildren().get(j).getText(), R.mipmap.video, "",
                                        mTreeNode.getChildren().get(k).getChildren().get(i).getChildren().get(j).getChannelInfo().getSzName(),
                                        mTreeNode.getChildren().get(k).getChildren().get(i).getChildren().get(j).getChannelInfo().getSzId(),
                                        mTreeNode.getChildren().get(k).getChildren().get(i).getChildren().get(j).getChannelInfo().getLat(),
                                        mTreeNode.getChildren().get(k).getChildren().get(i).getChildren().get(j).getChannelInfo().getLng());
                        grandsons.add(mgrandson);
                    }

                    VideoDataBean.child ch = new VideoDataBean.child(mTreeNode.getChildren().get(k).getChildren().get
                            (i).getText(), R.mipmap.zu, grandsons, "0", R.mipmap.xiala);
                    ch.setLat((IsNullUtils.isNullBackStr(map.get(mTreeNode.getChildren().get(k).getText()),"0,0")).split(",")[0]);
                    ch.setLng((IsNullUtils.isNullBackStr(map.get(mTreeNode.getChildren().get(k).getText()),"0,0")).split(",")[1]);

                    childs.add(ch);
                    childLists.add(ch);
                }
                VideoDataBean mVideoDataBeans = new VideoDataBean(mTreeNode.getChildren().get(k).getText(),
                        R.mipmap.zuzhi, childs, "0", R.mipmap.xiala);
                mVideoDataBean.add(mVideoDataBeans);
            }
            SharedPreferencesHelper.saveSharedPreferencestStringUtil(this,"groupList",0,new Gson().toJson(mVideoDataBean));
        }

        setUpTitleCentreText(mTreeNode.getText());

        if(adapter == null){
            adapter = new MyExpandableListAdapter();
            listview.setAdapter(adapter);
        }

        listview.setGroupIndicator(null);
        listview.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                if("0".equals(mVideoDataBean.get(i).getIsClick())){
                    mVideoDataBean.get(i).setIsClick("1");
                    mVideoDataBean.get(i).setJiantouPic(R.mipmap.shangla);
                    L.d("MainActivity",mVideoDataBean.get(i).getGroupNmae()+"父亲被点击了");
                }else{
                    L.d("MainActivity",mVideoDataBean.get(i).getGroupNmae()+"父亲又被点击了");
                    mVideoDataBean.get(i).setIsClick("0");
                    mVideoDataBean.get(i).setJiantouPic(R.mipmap.xiala);
                }
                adapter.refresh();
                return false;
            }
        });
        mShow.dismisss();
    }

    private MyExpandableListAdapter adapter;
    private List<VideoDataBean> mVideoDataBean = new ArrayList<>();
    private List<VideoDataBean.child> childLists = new ArrayList<>();

    class MyExpandableListAdapter extends BaseExpandableListAdapter {

        private Handler handler;
        public MyExpandableListAdapter(){
            handler = new Handler(){

                @Override
                public void handleMessage(Message msg) {
                    notifyDataSetChanged();
                    super.handleMessage(msg);
                }
            };
        }

        public void refresh() {
            handler.sendMessage(new Message());
        }

        @Override
        public void registerDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {

        }

        @Override
        public int getGroupCount() {
            return mVideoDataBean.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return 1;//// 很关键，，一定要返回  1  三级
        }

        @Override
        public VideoDataBean getGroup(int i) {
            return mVideoDataBean.get(i);
        }

        @Override
        public VideoDataBean.child getChild(int i, int i1) {
            return mVideoDataBean.get(i).getChild().get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean b, View convertView, ViewGroup viewGroup) {
            View view=null;
            GroupHolder groupHolder = null;
            if(convertView!=null){
                view = convertView;
                groupHolder = (GroupHolder) view.getTag();
            }else{
                view = View.inflate(MainActivity.this,R.layout.item_video_list, null);
                groupHolder = new GroupHolder();
                groupHolder.name = (TextView) view.findViewById(R.id.name);
                groupHolder.pic = (ImageView) view.findViewById(R.id.pic);
                groupHolder.xiala = (ImageView) view.findViewById(R.id.xiala);

                AutoUtils.auto(view);
                view.setTag(groupHolder);
            }

            groupHolder.name.setText(mVideoDataBean.get(groupPosition).getGroupNmae());
            groupHolder.pic.setImageResource(mVideoDataBean.get(groupPosition).getGroupPic());
            groupHolder.xiala.setVisibility(View.VISIBLE);
            groupHolder.xiala.setImageResource(mVideoDataBean.get(groupPosition).getJiantouPic());

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup
                viewGroup) {
            // 返回子ExpandableListView 的对象
            return getGenericExpandableListView(mVideoDataBean.get(groupPosition),childPosition);
        }

        public ExpandableListView getGenericExpandableListView(final VideoDataBean college,int childPosition){
//            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);

            CustomExpandableListView view = new CustomExpandableListView(MainActivity.this);
            view.setGroupIndicator(null);
            view.setDivider(null);
            view.setChildDivider(null);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            view.setCacheColorHint(0);
            view.setBackgroundResource(R.color.transparent);
            view.setDividerHeight(0);
            // 加载班级的适配器
            adapter1 = new GrandsonExpandableListViewAdapter(college);
            view.setAdapter(adapter1);

//            view.setPadding(10,0,0,0);
            view.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long
                        l) {
                    Bundle bun = new Bundle();
                    bun.putString("channelId",college.getChild().get(i).getGrandson().get(i1).getChannelId());
                    bun.putString("channelName",college.getChild().get(i).getGrandson().get(i1).getChannelName());
                    bun.putString("VideoName",college.getChild().get(i).getGrandson().get(i1).getGrandsonName());
                    StartToUrlUtils.getStartToUrlUtils().startToActivity(MainActivity.this, ActivityVideoPay.class,bun);
                    return true;
                }
            });
            view.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView expandableListView, View view, int position, long l) {
                    if("0".equals(college.getChild().get(position).getIsClick())){
                        L.d("MainActivity",college.getChild().get(position).getTitleName()+"孩子的孩子被点击了");
                        college.getChild().get(position).setIsClick("1");
                        college.getChild().get(position).setJiantouPic(R.mipmap.shangla);
                    }else{
                        L.d("MainActivity",college.getChild().get(position).getTitleName()+"孩子的孩子又被点击了");
                        college.getChild().get(position).setIsClick("0");
                        college.getChild().get(position).setJiantouPic(R.mipmap.xiala);
                    }
                    adapter1.refresh();
                    return false;
                }
            });

            if("1".equals(college.getChild().get(childPosition).getIsClick())){
                view.expandGroup(childPosition);
            }else{
                view. collapseGroup(childPosition);
            }

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int i) {

        }

        @Override
        public void onGroupCollapsed(int i) {

        }

        @Override
        public long getCombinedChildId(long l, long l1) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long l) {
            return 0;
        }

        class GroupHolder{
            ImageView pic;
            ImageView xiala;
            TextView name;
        }
    }

    private GrandsonExpandableListViewAdapter adapter1;
    class GrandsonExpandableListViewAdapter extends BaseExpandableListAdapter{
        private VideoDataBean college;
        private Handler handler;
        public GrandsonExpandableListViewAdapter(VideoDataBean college){
            this.college = college;
            handler = new Handler(){

                @Override
                public void handleMessage(Message msg) {
                    notifyDataSetChanged();
                    super.handleMessage(msg);
                }
            };
        }

        public void refresh() {
            handler.sendMessage(new Message());
        }

        @Override
        public int getGroupCount() {
            return college.getChild().size();
        }

        @Override
        public int getChildrenCount(int i) {
            return college.getChild().get(i).getGrandson().size();
        }

        @Override
        public VideoDataBean.child getGroup(int i) {
            return college.getChild().get(i);
        }

        @Override
        public VideoDataBean.child.grandson getChild(int i, int i1) {
            return college.getChild().get(i).getGrandson().get(i1);
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean b, View view1, ViewGroup viewGroup) {
            View view=null;
            GrandsonExpandableListViewAdapter.GroupHolder groupHolder = null;
            if(view1!=null){
                view = view1;
                groupHolder = (GrandsonExpandableListViewAdapter.GroupHolder) view.getTag();
            }else{
                view = View.inflate(MainActivity.this,R.layout.item_video_list, null);
                groupHolder = new GrandsonExpandableListViewAdapter.GroupHolder();
                groupHolder.name = (TextView) view.findViewById(R.id.name);
                groupHolder.pic = (ImageView) view.findViewById(R.id.pic);
                groupHolder.xiala = (ImageView) view.findViewById(R.id.xiala);

                AutoUtils.auto(view);
                view.setTag(groupHolder);
            }

            groupHolder.name.setText(college.getChild().get(groupPosition).getTitleName());
            groupHolder.pic.setImageResource(college.getChild().get(groupPosition).getChildPic());
            groupHolder.xiala.setImageResource(college.getChild().get(groupPosition).getJiantouPic());
            groupHolder.xiala.setVisibility(View.VISIBLE);

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean b, View convertView, ViewGroup
                viewGroup) {
            View view=null;
            ChildHolder childholder = null;
            if(convertView!=null){
                view = convertView;
                childholder = (ChildHolder) view.getTag();
            }else{
                view = View.inflate(MainActivity.this,R.layout.item_video_list, null);
                childholder = new ChildHolder();
                childholder.name = (TextView) view.findViewById(R.id.name);
                childholder.pic = (ImageView) view.findViewById(R.id.pic);
                childholder.xiala = (ImageView) view.findViewById(R.id.xiala);

                AutoUtils.auto(view);
                view.setTag(childholder);
            }

            childholder.name.setText(college.getChild().get(groupPosition).getGrandson().get
                    (childPosition).getGrandsonName());
            childholder.pic.setImageResource(college.getChild().get(groupPosition).getGrandson().get
                    (childPosition).getGrandsonPic());
            childholder.xiala.setVisibility(View.GONE);

//            view.setPadding(10,0,0,0);

            return view;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }

        class ChildHolder{
            ImageView pic;
            ImageView xiala;
            TextView name;
        }
        class GroupHolder{
            ImageView pic;
            ImageView xiala;
            TextView name;
        }
    }
}
