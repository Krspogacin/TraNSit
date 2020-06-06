package org.mad.transit;

import android.app.Application;

public class TransitApplication extends Application {

    private final ApplicationComponent appComponent;

    public TransitApplication() {
        this.appComponent = DaggerApplicationComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public ApplicationComponent getAppComponent() {
        return this.appComponent;
    }
}