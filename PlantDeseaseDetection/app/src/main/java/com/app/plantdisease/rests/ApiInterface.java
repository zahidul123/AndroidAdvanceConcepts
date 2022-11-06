package com.app.plantdisease.rests;

import com.app.plantdisease.callbacks.CallbackAds;
import com.app.plantdisease.callbacks.CallbackCategories;
import com.app.plantdisease.callbacks.CallbackCategoryDetails;
import com.app.plantdisease.callbacks.CallbackComments;
import com.app.plantdisease.callbacks.CallbackPostDetail;
import com.app.plantdisease.callbacks.CallbackRecent;
import com.app.plantdisease.callbacks.CallbackSettings;
import com.app.plantdisease.callbacks.CallbackUser;
import com.app.plantdisease.models.User;
import com.app.plantdisease.models.Value;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Android News App";

    @Headers({CACHE, AGENT})
    @GET("api.php?get_news_detail")
    Call<CallbackPostDetail> getNewsDetail(
            @Query("id") long id
    );

    @Headers({CACHE, AGENT})
   // @GET("api.php?get_recent_posts")
    @GET("manage-news.php")
    Call<Object> getRecentPost(
            @Query("api_key") String api_key,
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_video_posts")
    Call<CallbackRecent> getVideoPost(
            @Query("api_key") String api_key,
            @Query("page") int page,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_index")
    Call<CallbackCategories> getAllCategories(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_category_posts")
    Call<CallbackCategoryDetails> getCategoryDetailsByPage(
            @Query("id") long id,
            @Query("api_key") String api_key,
            @Query("page") long page,
            @Query("count") long count
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search_results")
    Call<CallbackRecent> getSearchPosts(
            @Query("api_key") String api_key,
            @Query("search") String search,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_search_results_rtl")
    Call<CallbackRecent> getSearchPostsRTL(
            @Query("api_key") String api_key,
            @Query("search") String search,
            @Query("count") int count
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_comments")
    Call<CallbackComments> getComments(@Query("nid") Long nid
    );

    @FormUrlEncoded
    @POST("api.php?post_comment")
    Call<Value> sendComment(@Field("nid") Long nid,
                            @Field("user_id") String user_id,
                            @Field("content") String content,
                            @Field("date_time") String date_time);

    @FormUrlEncoded
    @POST("api.php?update_comment")
    Call<Value> updateComment(@Field("comment_id") String comment_id,
                              @Field("date_time") String date_time,
                              @Field("content") String content);

    @FormUrlEncoded
    @POST("api.php?delete_comment")
    Call<Value> deleteComment(@Field("comment_id") String comment_id);

    @FormUrlEncoded
    @POST("api.php?update_user_data")
    Call<User> updateUserData(
            @Field("id") String id,
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("api.php?update_photo_profile")
    Call<User> updatePhotoProfile(
            @Field("id") String id,
            @Field("name") String name,
            @Field("email") String email,
            @Field("password") String password,
            @Field("old_image") String old_image,
            @Field("image") String image
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_settings")
    Call<CallbackSettings> getSettings();

    @Headers({CACHE, AGENT})
    @GET("api.php?get_user_data")
    Call<CallbackUser> getUser(
            @Query("id") String id
    );

    @FormUrlEncoded
    @POST("api.php?check_email")
    Call<Value> checkEmail(
            @Field("email") String email
    );

    @FormUrlEncoded
    @POST("php-mailer.php")
    Call<Value> forgotPassword(
            @Field("email") String email,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("api.php?update_view")
    Call<Value> updateView(
            @Field("nid") long nid
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?get_ads")
    Call<CallbackAds> getAds(
            @Query("api_key") String api_key
    );

}
