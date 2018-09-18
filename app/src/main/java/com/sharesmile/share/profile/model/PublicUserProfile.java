package com.sharesmile.share.profile.model;

import com.google.gson.annotations.SerializedName;
import com.sharesmile.share.AchievedBadge;
import com.sharesmile.share.core.base.UnObfuscable;

import java.util.ArrayList;

public class PublicUserProfile implements UnObfuscable {
    @SerializedName("user_profile")
    UserProfile userProfile;

    @SerializedName("user_achievement")
    ArrayList<UserAchievement> userAchievements;

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public ArrayList<AchievedBadge> getUserAchievements() {
        ArrayList<AchievedBadge> achievedBadges = new ArrayList<>();
        if (userAchievements != null) {
            for (UserAchievement userAchievement :
                    userAchievements) {
                AchievedBadge achievedBadge = new AchievedBadge();
                achievedBadge.setUserId(userAchievement.getUserId());

                achievedBadge.setCauseId(userAchievement.getCauseId());
                achievedBadge.setBadgeIdInProgress(userAchievement.getBadgeIdInProgress());
                achievedBadge.setBadgeIdAchieved(userAchievement.getBadgeIdAchieved());
                achievedBadge.setCategory(userAchievement.getCategoryId());
                achievedBadge.setBadgeType(userAchievement.getBadgeType());
                achievedBadges.add(achievedBadge);
            }
        }
        return achievedBadges;


    }

    public class UserProfile {

        @SerializedName("user_id")
        long userId;
        @SerializedName("first_name")
        String firstName;
        @SerializedName("last_name")
        String lastName;
        @SerializedName("gender_user")
        String genderUser;
        @SerializedName("email")
        String email;
        @SerializedName("profile_picture")
        String profilePicture;
        @SerializedName("phone_number")
        String phoneNumber;
        @SerializedName("total_workouts")
        double totalWorkouts;
        @SerializedName("total_distance")
        double totalDistance;
        @SerializedName("total_amount")
        double totalAmount;
        @SerializedName("social_thumb")
        String socialThumb;
        @SerializedName("birthday")
        String birthday;
        @SerializedName("max_streak")
        long maxStreak;
        @SerializedName("current_streak")
        long currentStreak;
        @SerializedName("achieved_title_1")
        long achievedTitle1;
        @SerializedName("achieved_title_2")
        long achievedTitle2;
        @SerializedName("user_bio")
        String userBio;

        public long getUserId() {
            return userId;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getGenderUser() {
            return genderUser;
        }

        public String getEmail() {
            return email;
        }

        public String getProfilePicture() {
            return profilePicture;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public double getTotalDistance() {
            return totalDistance;
        }

        public double getTotalAmount() {
            return totalAmount;
        }

        public String getSocialThumb() {
            return socialThumb;
        }

        public String getBirthday() {
            return birthday;
        }

        public long getMaxStreak() {
            return maxStreak;
        }

        public long getCurrentStreak() {
            return currentStreak;
        }

        public long getAchievedTitle1() {
            return achievedTitle1;
        }

        public long getAchievedTitle2() {
            return achievedTitle2;
        }

        public String getUserBio() {
            return userBio;
        }

        public double getTotalWorkouts() {
            return totalWorkouts;
        }
    }

    public class UserAchievement {
        @SerializedName("user_id")
        long userId;
        @SerializedName("cause_id")
        int causeId;
        @SerializedName("badge_id_in_progress")
        long badgeIdInProgress;
        @SerializedName("badge_id_achieved")
        long badgeIdAchieved;
        @SerializedName("category_id")
        int categoryId;
        @SerializedName("parameter_completed")
        String parameterCompleted;
        @SerializedName("achievement_time")
        String achievementTime;
        @SerializedName("badge_type")
        String badgeType;
        @SerializedName("client_achievement_id")
        int clientAchievementId;
        @SerializedName("badge_is_completed")
        boolean badgeIsCompleted;
        @SerializedName("server_achievement_id")
        int serverAchievementId;
        @SerializedName("cause_title")
        String causeTitle;

        public long getUserId() {
            return userId;
        }

        public int getCauseId() {
            return causeId;
        }

        public long getBadgeIdInProgress() {
            return badgeIdInProgress;
        }

        public long getBadgeIdAchieved() {
            return badgeIdAchieved;
        }

        public int getCategoryId() {
            return categoryId;
        }

        public String getParameterCompleted() {
            return parameterCompleted;
        }

        public String getAchievementTime() {
            return achievementTime;
        }

        public String getBadgeType() {
            return badgeType;
        }

        public int getClientAchievementId() {
            return clientAchievementId;
        }

        public boolean isBadgeIsCompleted() {
            return badgeIsCompleted;
        }

        public int getServerAchievementId() {
            return serverAchievementId;
        }

        public String getCauseTitle() {
            return causeTitle;
        }
    }
}
