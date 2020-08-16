package com.boring.chatNow.fragments;

import com.boring.chatNow.notifications.Response;
import com.boring.chatNow.notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface API_Service {
    @Headers({
            "Content-type: application/json",
            "Authorizations: key = AAAAVdrbqHs:APA91bH9sKKb9qjR6CnAItfercRAk4IageRrAkoVDJVrt2nGYDXhXHp3g2CV-c4OFHzHkug40maUFwUyY6fflA6lCh-JdIFg22NC0sQhgSEy0qL6XeSJPfy4JeDBsMT3NBnbG7Rm3Bwi"
    })

    @POST("fcm/send")
    Call<Response>sendNotification(@Body Sender body);
}
