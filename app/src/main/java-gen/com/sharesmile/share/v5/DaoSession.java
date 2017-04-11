package com.sharesmile.share.v5;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

import com.sharesmile.share.v5.Workout;
import com.sharesmile.share.v5.Cause;
import com.sharesmile.share.v5.User;
import com.sharesmile.share.v5.Message;
import com.sharesmile.share.v5.LeaderBoard;

import com.sharesmile.share.v5.WorkoutDao;
import com.sharesmile.share.v5.CauseDao;
import com.sharesmile.share.v5.UserDao;
import com.sharesmile.share.v5.MessageDao;
import com.sharesmile.share.v5.LeaderBoardDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig workoutDaoConfig;
    private final DaoConfig causeDaoConfig;
    private final DaoConfig userDaoConfig;
    private final DaoConfig messageDaoConfig;
    private final DaoConfig leaderBoardDaoConfig;

    private final WorkoutDao workoutDao;
    private final CauseDao causeDao;
    private final UserDao userDao;
    private final MessageDao messageDao;
    private final LeaderBoardDao leaderBoardDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        workoutDaoConfig = daoConfigMap.get(WorkoutDao.class).clone();
        workoutDaoConfig.initIdentityScope(type);

        causeDaoConfig = daoConfigMap.get(CauseDao.class).clone();
        causeDaoConfig.initIdentityScope(type);

        userDaoConfig = daoConfigMap.get(UserDao.class).clone();
        userDaoConfig.initIdentityScope(type);

        messageDaoConfig = daoConfigMap.get(MessageDao.class).clone();
        messageDaoConfig.initIdentityScope(type);

        leaderBoardDaoConfig = daoConfigMap.get(LeaderBoardDao.class).clone();
        leaderBoardDaoConfig.initIdentityScope(type);

        workoutDao = new WorkoutDao(workoutDaoConfig, this);
        causeDao = new CauseDao(causeDaoConfig, this);
        userDao = new UserDao(userDaoConfig, this);
        messageDao = new MessageDao(messageDaoConfig, this);
        leaderBoardDao = new LeaderBoardDao(leaderBoardDaoConfig, this);

        registerDao(Workout.class, workoutDao);
        registerDao(Cause.class, causeDao);
        registerDao(User.class, userDao);
        registerDao(Message.class, messageDao);
        registerDao(LeaderBoard.class, leaderBoardDao);
    }
    
    public void clear() {
        workoutDaoConfig.getIdentityScope().clear();
        causeDaoConfig.getIdentityScope().clear();
        userDaoConfig.getIdentityScope().clear();
        messageDaoConfig.getIdentityScope().clear();
        leaderBoardDaoConfig.getIdentityScope().clear();
    }

    public WorkoutDao getWorkoutDao() {
        return workoutDao;
    }

    public CauseDao getCauseDao() {
        return causeDao;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public MessageDao getMessageDao() {
        return messageDao;
    }

    public LeaderBoardDao getLeaderBoardDao() {
        return leaderBoardDao;
    }

}