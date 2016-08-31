package jp.techacademy.tomonari.watanabe.taskapp;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by moggmogg on 2016/08/29.
 */
public class TaskApp extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder(this).build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }


}
