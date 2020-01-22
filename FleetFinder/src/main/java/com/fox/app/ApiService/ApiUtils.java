package com.fox.app.ApiService;

public class ApiUtils {
    //      api service for webservices
    private ApiUtils() {}
    public static final String BASE_URL = "http://foxfleet.biz/foxapi/public/api/";

    public static ApiService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(ApiService.class);
    }
}
