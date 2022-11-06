package com.app.plantdisease.config;

public class AppConfig {

    //put your admin panel url here
   // public static final String ADMIN_PANEL_URL = "http://10.0.2.2/android_news_app";
    public static final String ADMIN_PANEL_URL = "https://plant.levinzo.com";

    //your api key which obtained from admin panel
    public static final String API_KEY = "cda11v2OkqSI1rhQm37PBXKnpisMtlaDzoc4w0U6uNATgZRbJG";

    //if login register feature disabled, comment feature will be disabled
    public static final boolean DISABLE_LOGIN_REGISTER = false;

    //show one latest news as header view
    public static final boolean DISPLAY_HEADER_VIEW = true;

    //show short description in the news list
    public static final boolean ENABLE_EXCERPT_IN_POST_LIST = true;

    //fixed bottom navigation
    public static final boolean ENABLE_FIXED_BOTTOM_NAVIGATION = true;

    //show total news in each category
    public static final boolean ENABLE_POST_COUNT_IN_CATEGORY = false;

    //video player orientation
    public static final boolean FORCE_VIDEO_PLAYER_TO_LANDSCAPE = false;

    //date display configuration
    public static final boolean ENABLE_DATE_DISPLAY = true;
    public static final boolean DATE_DISPLAY_AS_TIME_AGO = false;

    //display alert dialog when user want to close the app
    public static final boolean ENABLE_EXIT_DIALOG = false;

    //enable view count in the news description
    public static final boolean ENABLE_VIEW_COUNT = true;

    //set false to disable copy text in the news description
    public static final boolean ENABLE_TEXT_SELECTION = false;

    //open link in the news description using external web browser
    public static final boolean OPEN_LINK_INSIDE_APP = false;

    //GDPR EU Consent
    public static final boolean USE_LEGACY_GDPR_EU_CONSENT = true;

    //load more for next news list
    public static final int LOAD_MORE = 15;

    //if you use RTL Language e.g : Arabic Language or other, set true
    public static final boolean ENABLE_RTL_MODE = false;

}
