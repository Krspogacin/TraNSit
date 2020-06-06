package org.mad.transit;

import android.content.ContentResolver;
import android.content.Context;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    private final Context context;

    public AppModule(Context context) {
        this.context = context;
    }

    @Provides
    public ContentResolver provideContentResolver() {
        return this.context.getContentResolver();
    }
}