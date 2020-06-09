package org.mad.transit;

import org.mad.transit.activities.FavouriteLocationsActivity;
import org.mad.transit.activities.PastDirectionsActivity;
import org.mad.transit.activities.PlacesActivity;
import org.mad.transit.activities.RoutesActivity;
import org.mad.transit.activities.SingleLineActivity;
import org.mad.transit.activities.SplashScreenActivity;
import org.mad.transit.activities.TimetableActivity;
import org.mad.transit.fragments.DirectionsFragment;
import org.mad.transit.fragments.FavouriteLinesFragment;
import org.mad.transit.fragments.LinesFragment;
import org.mad.transit.fragments.SingleLineMapFragment;
import org.mad.transit.fragments.StopsFragment;
import org.mad.transit.fragments.StopsMapFragment;
import org.mad.transit.fragments.TimetableFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class})
public interface ApplicationComponent {
    void inject(MainActivity mainActivity);

    void inject(SplashScreenActivity splashScreenActivity);

    void inject(StopsFragment stopsFragment);

    void inject(StopsMapFragment stopsMapFragment);

    void inject(FavouriteLocationsActivity favouriteLocationsActivity);

    void inject(PastDirectionsActivity pastDirectionsActivity);

    void inject(SingleLineActivity singleLineActivity);

    void inject(TimetableActivity timetableActivity);

    void inject(TimetableFragment timetableFragment);

    void inject(DirectionsFragment directionsFragment);

    void inject(LinesFragment linesFragment);

    void inject(FavouriteLinesFragment favouriteLinesFragment);

    void inject(PlacesActivity placesActivity);

    void inject(RoutesActivity routesActivity);

    void inject(SingleLineMapFragment singleLineMapFragment);
}