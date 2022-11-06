package com.app.plantdisease.utils;

import static com.app.plantdisease.config.AppConfig.ADMIN_PANEL_URL;

public class Constant {

    public static final String REGISTER_URL = ADMIN_PANEL_URL + "/api/user_register/?user_type=normal&name=";
    public static final String NORMAL_LOGIN_URL = ADMIN_PANEL_URL + "/api/get_user_login/?email=";
    public static final String CATEGORY_ARRAY_NAME = "result";
    public static int GET_SUCCESS_MSG;
    public static final String MSG = "msg";
    public static final String SUCCESS = "success";
    public static final String USER_NAME = "name";
    public static final String USER_ID = "user_id";
    public static final long DELAY_REFRESH = 1000;
    public static final int DELAY_PROGRESS_DIALOG = 2000;

    public static final long DELAY_TIME = 1000;
    public static final long DELAY_RIPPLE = 300;
    public static final String YOUTUBE_IMG_FRONT = "https://img.youtube.com/vi/";
    public static final String YOUTUBE_IMG_BACK = "/mqdefault.jpg";
    public static final int MAX_SEARCH_RESULT = 100;

    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";

    public static final String AD_STATUS_ON = "on";
    public static final String ADMOB = "admob";
    public static final String FAN = "fan";
    public static final String STARTAPP = "startapp";
    public static final String UNITY = "unity";
    public static final String APPLOVIN = "applovin";

    //startapp native ad image parameters
    public static final int STARTAPP_IMAGE_XSMALL = 1; //for image size 100px X 100px
    public static final int STARTAPP_IMAGE_SMALL = 2; //for image size 150px X 150px
    public static final int STARTAPP_IMAGE_MEDIUM = 3; //for image size 340px X 340px
    public static final int STARTAPP_IMAGE_LARGE = 4; //for image size 1200px X 628px

    //unity banner ad size
    public static final int UNITY_ADS_BANNER_WIDTH = 320;
    public static final int UNITY_ADS_BANNER_HEIGHT = 50;

    public static final int MAX_NUMBER_OF_NATIVE_AD_DISPLAYED = 25;
    public static final int BANNER_HOME = 1;
    public static final int BANNER_POST_DETAIL = 1;
    public static final int BANNER_CATEGORY_DETAIL = 1;
    public static final int BANNER_SEARCH = 1;
    public static final int BANNER_COMMENT = 1;
    public static final int INTERSTITIAL_POST_LIST = 1;
    public static final int INTERSTITIAL_POST_DETAIL = 1;
    public static final int NATIVE_AD_POST_DETAIL = 1;

    public static final int FONT_SIZE_XSMALL = 12;
    public static final int FONT_SIZE_SMALL = 14;
    public static final int FONT_SIZE_MEDIUM = 16;
    public static final int FONT_SIZE_LARGE = 18;
    public static final int FONT_SIZE_XLARGE = 20;

}