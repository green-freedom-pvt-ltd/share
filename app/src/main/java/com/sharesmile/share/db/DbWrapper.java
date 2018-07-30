package com.sharesmile.share.db;

import android.database.sqlite.SQLiteDatabase;

import com.sharesmile.share.AchievedBadgeDao;
import com.sharesmile.share.AchievedTitleDao;
import com.sharesmile.share.BadgeDao;
import com.sharesmile.share.CategoryDao;
import com.sharesmile.share.CauseDao;
import com.sharesmile.share.DaoMaster;
import com.sharesmile.share.DaoSession;
import com.sharesmile.share.LeaderBoardDao;
import com.sharesmile.share.TitleDao;
import com.sharesmile.share.core.application.MainApplication;
import com.sharesmile.share.UserDao;
import com.sharesmile.share.WorkoutDao;

import io.smooch.core.c.m;

/**
 * Created by Shine on 07/05/16.
 */
public class DbWrapper {

    private final static String DB_NAME = "impact-db";

    MainApplication application;

    private SQLiteDatabase db;
    //private DaoMaster.DevOpenHelper mDbHelper;

    private DaoSession mDaoSession;
    private WorkoutDao mWorkoutDao;
    private UserDao mUserdao;
    private CauseDao mCauseDao;
    private UpgradeHelper helper;

    private LeaderBoardDao mLeaderBoardDao;

    private CategoryDao mCategoryDao;
    private BadgeDao mBadgeDao;
    private AchievedBadgeDao mAchievedBadgeDao;
    private TitleDao mTitleDao;
    private AchievedTitleDao mAchievedTitleDao;

    public DbWrapper(MainApplication app) {
        application = app;
        generateMembers();
    }

 /*   private void generateMembers() {

        mDbHelper = new DaoMaster.DevOpenHelper(application, DB_NAME, null);

        if (db != null) {
            db.close();
        }

        db = mDbHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
        mWorkoutDao = mDaoSession.getWorkoutDao();
        mUserdao = mDaoSession.getUserDao();
        mCauseDao = mDaoSession.getCauseDao();

    }*/

    public void generateMembers() {

        if (helper != null) {
            //need to refresh everything
            helper.close();
        } else {
            helper = new UpgradeHelper(application, DB_NAME, null);
        }
        if (db != null) {
            db.close();
        }
        db = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(db);
        mDaoSession = daoMaster.newSession();
        mWorkoutDao = mDaoSession.getWorkoutDao();
        mUserdao = mDaoSession.getUserDao();
        mCauseDao = mDaoSession.getCauseDao();
        mLeaderBoardDao = mDaoSession.getLeaderBoardDao();

        mBadgeDao = mDaoSession.getBadgeDao();
        mAchievedBadgeDao = mDaoSession.getAchievedBadgeDao();
        mTitleDao = mDaoSession.getTitleDao();
        mAchievedTitleDao = mDaoSession.getAchievedTitleDao();
        mCategoryDao = mDaoSession.getCategoryDao();
    }

    /*public DaoMaster.DevOpenHelper getDbHelper() {
        return mDbHelper;
    }*/



    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public WorkoutDao getWorkoutDao() {
        return mWorkoutDao;
    }

    public UserDao getUserdao() {
        return mUserdao;
    }

    public CauseDao getCauseDao() {
        return mCauseDao;
    }

    public LeaderBoardDao getLeaderBoardDao(){ return mLeaderBoardDao;}

    public BadgeDao getBadgeDao(){ return mBadgeDao;}
    public TitleDao getTitleDao(){ return mTitleDao;}
    public AchievedTitleDao getAchievedTitleDao(){ return mAchievedTitleDao;}
    public AchievedBadgeDao getAchievedBadgeDao(){ return mAchievedBadgeDao;}
    public CategoryDao getCategoryDao(){ return mCategoryDao;}

    public void clearAll() {
        getWorkoutDao().deleteAll();
        getUserdao().deleteAll();
        mDaoSession.clear();
    }
}
