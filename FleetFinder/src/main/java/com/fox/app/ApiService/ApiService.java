package com.fox.app.ApiService;



import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @POST("confirm-parcel")
    @FormUrlEncoded
    Call<ResponseModel> getBarcodeResult(@Field("accessId") String accessId,@Field("accessPassword") String accessPassword,@Field("barcode") String barcode);


}
