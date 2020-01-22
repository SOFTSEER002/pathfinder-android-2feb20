package com.fox.app.ApiService;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResponseModel {

    @SerializedName("barcode")
    @Expose
    private String barcode;
    @SerializedName("batchId")
    @Expose
    private String batchId;
    @SerializedName("batchDate")
    @Expose
    private String batchDate;
    @SerializedName("labelSequence")
    @Expose
    private String labelSequence;
    @SerializedName("scanTime")
    @Expose
    private String scanTime;
    @SerializedName("scanDate")
    @Expose
    private String scanDate;
    @SerializedName("zpCode")
    @Expose
    private String zpCode;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getBatchDate() {
        return batchDate;
    }

    public void setBatchDate(String batchDate) {
        this.batchDate = batchDate;
    }

    public String getLabelSequence() {
        return labelSequence;
    }

    public void setLabelSequence(String labelSequence) {
        this.labelSequence = labelSequence;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getScanDate() {
        return scanDate;
    }

    public void setScanDate(String scanDate) {
        this.scanDate = scanDate;
    }

    public String getZpCode() {
        return zpCode;
    }

    public void setZpCode(String zpCode) {
        this.zpCode = zpCode;
    }
}
