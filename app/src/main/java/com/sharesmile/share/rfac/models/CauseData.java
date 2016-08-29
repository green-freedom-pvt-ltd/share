package com.sharesmile.share.rfac.models;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.*;
import com.sharesmile.share.core.UnObfuscable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sharesmile.share.Cause;

/**
 * Created by Shine on 01/05/16..
 */
public class CauseData implements UnObfuscable, Serializable {

    private static final String TAG = "Cause";

    @SerializedName("pk")
    private long id;
    @SerializedName("cause_title")
    private String title;

    @SerializedName("cause_category")
    private String category;

    @SerializedName("partners")
    private List<Partner> executors;

    private List<Sponsor> sponsors;

    @SerializedName("cause_description")
    private String causeDescription;

    @SerializedName("cause_brief")
    private String detailText;

    @SerializedName("cause_image")
    private String imageUrl;

    @SerializedName("conversion_rate")
    private float conversionRate;

    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("cause_thank_you_image")
    private String causeThankYouImage;

    @SerializedName("cause_share_message_template")
    private String causeShareMessageTemplate;

    @SerializedName("min_distance")
    private int minDistance;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public List<Partner> getExecutors() {
        return executors;
    }

    public Partner getExecutor() {
        if (executors != null && !executors.isEmpty()) {
            return executors.get(0);
        }
        return null;
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }

    public Sponsor getSponsor() {
        if (sponsors != null && !sponsors.isEmpty()) {
            return sponsors.get(0);
        }
        return null;
    }

    public float getConversionRate() {
        return conversionRate;
    }

    public String getDetailText() {
        return detailText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getCauseShareMessageTemplate() {
        return causeShareMessageTemplate;
    }

    public String getCauseDescription() {
        return causeDescription;
    }

    public String getCauseThankYouImage() {
        return causeThankYouImage;
    }

    public void setCauseShareMessageTemplate(String causeShareMessageTemplate) {
        this.causeShareMessageTemplate = causeShareMessageTemplate;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setExecutors(List<Partner> executors) {
        this.executors = executors;
    }

    public void setSponsors(List<Sponsor> sponsors) {
        this.sponsors = sponsors;
    }

    public void setCauseDescription(String causeDescription) {
        this.causeDescription = causeDescription;
    }

    public void setDetailText(String detailText) {
        this.detailText = detailText;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setConversionRate(float conversionRate) {
        this.conversionRate = conversionRate;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setCauseThankYouImage(String causeThankYouImage) {
        this.causeThankYouImage = causeThankYouImage;
    }

    public int getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(int minDistance) {
        this.minDistance = minDistance;
    }

    public Cause getCauseDbObject() {

        Cause cause = new Cause((long) getId(), getTitle(), getCauseDescription(), getConversionRate(), getMinDistance(), getCategory(), getDetailText(),
                getImageUrl(), getCauseThankYouImage(), getCauseShareMessageTemplate(),
                isActive(), getSponsor().getId(), getSponsor().getName(), getSponsor().getSponsorNgo(), getSponsor().getLogoUrl(), getExecutor().getId(), getExecutor().getPartnerCompany(),
                getExecutor().getPartnerNgo(), getExecutor().getType());
        return cause;

    }

    public CauseData(Cause cause) {

        setId(cause.getId());
        setTitle(cause.getCauseTitle());
        setCauseDescription(cause.getCauseDescription());
        setConversionRate(cause.getConversionRate());
        setMinDistance(cause.getMinDistance());
        setCategory(cause.getCauseCategory());
        setDetailText(cause.getCauseBrief());
        setImageUrl(cause.getCauseImage());
        setCauseThankYouImage(cause.getCauseThankyouImage());
        setCauseShareMessageTemplate(cause.getShare_template());
        setActive(cause.getIsActive());

        //Sponsors
        Sponsor sponsor = new Sponsor();
        sponsor.setId(cause.getSponsorId());
        sponsor.setName(cause.getSponsorCompany());
        sponsor.setSponsorNgo(cause.getSponsorNgo());
        sponsor.setLogoUrl(cause.getSponsorLogo());
        List<Sponsor> sponsors = new ArrayList<>();
        sponsors.add(sponsor);
        setSponsors(sponsors);

        //Partners
        Partner partner = new Partner();
        partner.setId(cause.getPartnerId());
        partner.setType(cause.getPartnerType());
        partner.setPartnerNgo(cause.getPartnerNgo());
        partner.setPartnerCompany(cause.getPartnerCompany());

        List<Partner> partners = new ArrayList<>();
        partners.add(partner);
        setExecutors(partners);
    }
}
