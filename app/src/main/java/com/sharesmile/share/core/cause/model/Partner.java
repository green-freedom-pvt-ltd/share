package com.sharesmile.share.core.cause.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.core.base.UnObfuscable;

import java.io.Serializable;

/**
 * Created by ankitmaheshwari1 on 07/03/16.
 */
public class Partner implements UnObfuscable,Serializable {


    private static final String TAG = "Partner";

    @SerializedName("partner_type")
    private String type;

    @SerializedName("partner_id")
    private int id;

    @SerializedName("partner_company")
    private String partnerCompany;

    @SerializedName("partner_ngo")
    private String partnerNgo;

    public String getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public String getPartnerCompany() {
        return partnerCompany;
    }

    public void setPartnerCompany(String partnerCompany) {
        this.partnerCompany = partnerCompany;
    }

    public String getPartnerNgo() {
        return partnerNgo;
    }

    public void setPartnerNgo(String partnerNgo) {
        this.partnerNgo = partnerNgo;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(int id) {
        this.id = id;
    }
}
