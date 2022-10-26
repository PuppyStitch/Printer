package com.simcom.printer.database.printerdb;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}

//    使用
//    AppDatabase db = Room.databaseBuilder(getApplicationContext(),
//            AppDatabase.class, "database-name").build();
//    db.userDao().insert(new User());