package com.eaglesakura.andriders.v2.dao.command.controller;

import android.database.sqlite.SQLiteDatabase;

import java.util.Map;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.AbstractDaoSession;
import de.greenrobot.dao.identityscope.IdentityScopeType;
import de.greenrobot.dao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 *
 * @see de.greenrobot.dao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig dbAcesCommandDaoConfig;

    private final DbAcesCommandDao dbAcesCommandDao;

    public DaoSession(SQLiteDatabase db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        dbAcesCommandDaoConfig = daoConfigMap.get(DbAcesCommandDao.class).clone();
        dbAcesCommandDaoConfig.initIdentityScope(type);

        dbAcesCommandDao = new DbAcesCommandDao(dbAcesCommandDaoConfig, this);

        registerDao(DbAcesCommand.class, dbAcesCommandDao);
    }

    public void clear() {
        dbAcesCommandDaoConfig.getIdentityScope().clear();
    }

    public DbAcesCommandDao getDbAcesCommandDao() {
        return dbAcesCommandDao;
    }

}