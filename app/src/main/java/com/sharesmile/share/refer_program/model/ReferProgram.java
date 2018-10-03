package com.sharesmile.share.refer_program.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.sharesmile.share.core.Constants;
import com.sharesmile.share.core.SharedPrefsManager;
import com.sharesmile.share.core.base.ExpoBackoffTask;
import com.sharesmile.share.core.base.UnObfuscable;
import com.sharesmile.share.core.config.Urls;
import com.sharesmile.share.core.event.UpdateEvent;
import com.sharesmile.share.core.timekeeping.ServerTimeKeeper;
import com.sharesmile.share.network.NetworkDataProvider;
import com.sharesmile.share.network.NetworkException;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sharesmile.share.core.Constants.PREF_SMC_PROGRAMS;

public class ReferProgram implements UnObfuscable {
    @SerializedName("id")
    private int id;
    @SerializedName("program_name")
    private String programName;
    @SerializedName("start_date")
    private String startDate;
    @SerializedName("end_date")
    private String endDate;
    @SerializedName("is_active")
    private boolean isActive;
    @SerializedName("banner_image")
    private String bannerImage;
    @SerializedName("referal_category")
    private String referalCategory;
    @SerializedName("incentive")
    private String incentive;
    @SerializedName("share_message")
    private String shareMessage;
    @SerializedName("share_message_2")
    private String shareMessage2;
    @SerializedName("sponsored_by")
    private String sponsoredBy;
    @SerializedName("referal_cause_category")
    private String referalCauseCategory;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getBannerImage() {
        return bannerImage;
    }

    public void setBannerImage(String bannerImage) {
        this.bannerImage = bannerImage;
    }

    public String getReferalCategory() {
        return referalCategory;
    }

    public void setReferalCategory(String referalCategory) {
        this.referalCategory = referalCategory;
    }

    public String getIncentive() {
        return incentive;
    }

    public void setIncentive(String incentive) {
        this.incentive = incentive;
    }

    public String getShareMessage() {
        return shareMessage;
    }

    public void setShareMessage(String shareMessage) {
        this.shareMessage = shareMessage;
    }

    public String getShareMessage2() {
        return shareMessage2;
    }

    public void setShareMessage2(String shareMessage2) {
        this.shareMessage2 = shareMessage2;
    }

    public String getSponsoredBy() {
        return sponsoredBy;
    }

    public void setSponsoredBy(String sponsoredBy) {
        this.sponsoredBy = sponsoredBy;
    }

    public String getReferalCauseCategory() {
        return referalCauseCategory;
    }

    public void setReferalCauseCategory(String referalCauseCategory) {
        this.referalCauseCategory = referalCauseCategory;
    }

    public static void syncDetails() {
        new ExpoBackoffTask() {
            @Override
            public int performtask() {

                return syncReferProgramDetails();
            }
        }.run();
    }

    private static int syncReferProgramDetails() {
        try {
            List<ReferProgram> referPrograms = NetworkDataProvider.doGetCall(Urls.getReferProgramsUrl(),
                    new TypeToken<List<ReferProgram>>() {
                    }.getType());
            int pos = -1;
            for (ReferProgram referProgram : referPrograms) {
                if (referProgram.getIsActive()) {
                    SharedPrefsManager.getInstance().setString(Constants.PREF_SMC_PROGRAMS, new Gson().toJson(referProgram, ReferProgram.class));
                    break;
                } else {
                    SharedPrefsManager.getInstance().setString(Constants.PREF_SMC_PROGRAMS, null);
                }
            }
            EventBus.getDefault().post(new UpdateEvent.OnGetReferProgramDetails(ExpoBackoffTask.RESULT_SUCCESS));
        } catch (NetworkException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static ReferProgram getReferProgramDetails() {
        return SharedPrefsManager.getInstance().getObject(PREF_SMC_PROGRAMS, ReferProgram.class);
    }

    public static boolean isReferProgramActive() {
        ReferProgram referProgram = getReferProgramDetails();
        if (referProgram == null)
            return false;
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(ServerTimeKeeper.getInstance().getServerTimeAtSystemTime(Calendar.getInstance().getTimeInMillis()));
        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startCalendar.setTimeInMillis(simpleDateFormat.parse(referProgram.getStartDate()).getTime());
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);

            endCalendar.setTimeInMillis(simpleDateFormat.parse(referProgram.getEndDate()).getTime());
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);

            if (currentCalendar.after(startCalendar) && currentCalendar.before(endCalendar)) {
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static long noOfDaysPassed() {
        ReferProgram referProgram = getReferProgramDetails();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(ServerTimeKeeper.getInstance().getServerTimeAtSystemTime(Calendar.getInstance().getTimeInMillis()));
        Calendar startCalendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            startCalendar.setTimeInMillis(simpleDateFormat.parse(referProgram.getStartDate()).getTime());
            startCalendar.set(Calendar.HOUR_OF_DAY, 0);
            startCalendar.set(Calendar.MINUTE, 0);
            startCalendar.set(Calendar.SECOND, 0);
            long end = currentCalendar.getTimeInMillis();
            long start = startCalendar.getTimeInMillis();
            return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long noOfDaysPending() {
        ReferProgram referProgram = getReferProgramDetails();
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTimeInMillis(ServerTimeKeeper.getInstance().getServerTimeAtSystemTime(Calendar.getInstance().getTimeInMillis()));
        Calendar endCalendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            endCalendar.setTimeInMillis(simpleDateFormat.parse(referProgram.getEndDate()).getTime());
            endCalendar.set(Calendar.HOUR_OF_DAY, 23);
            endCalendar.set(Calendar.MINUTE, 59);
            endCalendar.set(Calendar.SECOND, 59);
            long end = endCalendar.getTimeInMillis();
            long start = currentCalendar.getTimeInMillis();
            return TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
