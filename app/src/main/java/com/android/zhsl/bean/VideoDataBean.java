package com.android.zhsl.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 作者：李堂飞 on 2017/5/15 09:05
 * 邮箱：litangfei119@qq.com
 * 分组列表数据
 */

public class VideoDataBean implements Serializable{

    private String groupNmae;//分组名字
    private int groupPic;//分组图片
    private List<child> child;
    private String isClick;//是否点击
    private int jiantouPic;//上下拉箭头方向

    public int getJiantouPic() {
        return jiantouPic;
    }

    public void setJiantouPic(int jiantouPic) {
        this.jiantouPic = jiantouPic;
    }

    public String getIsClick() {
        return isClick;
    }

    public void setIsClick(String isClick) {
        this.isClick = isClick;
    }

    public List<VideoDataBean.child> getChild() {
        return child;
    }

    public void setChild(List<VideoDataBean.child> child) {
        this.child = child;
    }

    public String getGroupNmae() {
        return groupNmae;
    }

    public void setGroupNmae(String groupNmae) {
        this.groupNmae = groupNmae;
    }

    public int getGroupPic() {
        return groupPic;
    }

    public void setGroupPic(int groupPic) {
        this.groupPic = groupPic;
    }

    public VideoDataBean(String groupNmae, int groupPic, List<VideoDataBean.child> child, String isClick, int jiantouPic) {
        this.groupNmae = groupNmae;
        this.groupPic = groupPic;
        this.child = child;
        this.isClick = isClick;
        this.jiantouPic = jiantouPic;
    }

    public static class child implements Serializable{
        private String titleName;//子项名字
        private int childPic;//孩子图片
        private List<grandson> grandson;
        private String isClick;
        private int jiantouPic;//上下拉箭头方向
        private String lat = "0";
        private String lng = "0";

        public String getLat() {
            return lat;
        }

        public void setLat(String lat) {
            this.lat = lat;
        }

        public String getLng() {
            return lng;
        }

        public void setLng(String lng) {
            this.lng = lng;
        }

        public int getJiantouPic() {
            return jiantouPic;
        }

        public void setJiantouPic(int jiantouPic) {
            this.jiantouPic = jiantouPic;
        }

        public String getIsClick() {
            return isClick;
        }

        public void setIsClick(String isClick) {
            this.isClick = isClick;
        }

        public List<VideoDataBean.child.grandson> getGrandson() {
            return grandson;
        }

        public void setGrandson(List<VideoDataBean.child.grandson> grandson) {
            this.grandson = grandson;
        }

        public child(String titleName, int childPic, List<VideoDataBean.child.grandson> grandson, String isClick, int jiantouPic) {
            this.titleName = titleName;
            this.childPic = childPic;
            this.grandson = grandson;
            this.isClick = isClick;
            this.jiantouPic = jiantouPic;
        }

        public String getTitleName() {
            return titleName;
        }

        public void setTitleName(String titleName) {
            this.titleName = titleName;
        }

        public int getChildPic() {
            return childPic;
        }

        public void setChildPic(int childPic) {
            this.childPic = childPic;
        }

        public static class grandson implements Serializable{
            private String grandsonName;//孙子名字
            private int grandsonPic;//孙子图片
            private String grandsonVideoUrl;//孙子视屏链接
            private String channelName;
            private String channelId;
            private String lat = "0";
            private String lng = "0";
            private String isSelect = "0";

            public String getIsSelect() {
                return isSelect;
            }

            public void setIsSelect(String isSelect) {
                this.isSelect = isSelect;
            }

            public grandson(String grandsonName, int grandsonPic, String grandsonVideoUrl, String channelName, String channelId, String lat, String lng) {
                this.grandsonName = grandsonName;
                this.grandsonPic = grandsonPic;
                this.grandsonVideoUrl = grandsonVideoUrl;
                this.channelName = channelName;
                this.channelId = channelId;
                this.lat = lat;
                this.lng = lng;
            }

            public String getLat() {
                return lat;
            }

            public void setLat(String lat) {
                this.lat = lat;
            }

            public String getLng() {
                return lng;
            }

            public void setLng(String lng) {
                this.lng = lng;
            }

            public String getChannelName() {
                return channelName;
            }

            public void setChannelName(String channelName) {
                this.channelName = channelName;
            }

            public String getChannelId() {
                return channelId;
            }

            public void setChannelId(String channelId) {
                this.channelId = channelId;
            }

            public String getGrandsonName() {
                return grandsonName;
            }

            public void setGrandsonName(String grandsonName) {
                this.grandsonName = grandsonName;
            }

            public int getGrandsonPic() {
                return grandsonPic;
            }

            public void setGrandsonPic(int grandsonPic) {
                this.grandsonPic = grandsonPic;
            }

            public String getGrandsonVideoUrl() {
                return grandsonVideoUrl;
            }

            public void setGrandsonVideoUrl(String grandsonVideoUrl) {
                this.grandsonVideoUrl = grandsonVideoUrl;
            }
        }
    }
}
