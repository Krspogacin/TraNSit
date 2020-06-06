package org.mad.transit.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.algolia.instantsearch.core.connection.ConnectionHandler
import com.algolia.instantsearch.core.hits.connectHitsView
import com.algolia.instantsearch.helper.android.list.autoScrollToStart
import com.algolia.instantsearch.helper.searchbox.SearchBoxConnector
import com.algolia.instantsearch.helper.searchbox.connectView
import com.algolia.instantsearch.helper.searcher.SearcherPlaces
import com.algolia.search.model.places.Country
import com.algolia.search.model.places.PlaceType
import com.algolia.search.model.places.PlacesQuery
import com.algolia.search.model.search.Language
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import org.mad.transit.R
import org.mad.transit.TransitApplication
import org.mad.transit.adapters.FavouritePlacesAdapter
import org.mad.transit.adapters.PlacesAdapter
import org.mad.transit.model.Location
import org.mad.transit.receiver.ConnectivityReceiver
import org.mad.transit.repository.FavouriteLocationRepository
import org.mad.transit.util.LocationsUtil
import org.mad.transit.util.NetworkUtil
import org.mad.transit.view.OnItemClickListener
import org.mad.transit.view.SearchBoxNoEmptyQuery
import org.mad.transit.view.addOnItemClickListener
import java.io.IOException
import javax.inject.Inject
import kotlin.properties.Delegates

class PlacesActivity : AppCompatActivity(), FavouritePlacesAdapter.OnItemClickListener, ConnectivityReceiver.ConnectivityReceiverListener {

    companion object {
        const val LOCATION_KEY = "LOCATION"
        const val CHOOSE_ON_MAP_REQUEST_CODE = 555
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var noInternetConnectionMessageView: TextView
    private lateinit var adapter: PlacesAdapter
    private lateinit var favouritePlacesAdapter: FavouritePlacesAdapter

    private val query = PlacesQuery(
            type = PlaceType.Address,
            hitsPerPage = 10,
            aroundLatLngViaIP = true,
            countries = listOf(Country.Serbia)
    )

    private val searcher = SearcherPlaces(query = query, language = Language.Other("rs"))
    private val searchBox = SearchBoxConnector(searcher)
    private val connection = ConnectionHandler(searchBox)
    private val connectivityReceiver = ConnectivityReceiver()
    private var currentNetworkAvailability by Delegates.notNull<Boolean>()

    @Inject
    lateinit var favouriteLocationRepository: FavouriteLocationRepository

    override fun onCreate(savedInstanceState: Bundle?) {

        (applicationContext as TransitApplication).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.places_activity)

        registerReceiver(connectivityReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnectivityReceiver.connectivityReceiverListener = this
        currentNetworkAvailability = NetworkUtil.isConnected(this)

        noInternetConnectionMessageView = findViewById(R.id.no_internet_connection_message_text_view)
        noInternetConnectionMessageView.visibility = if (currentNetworkAvailability) View.GONE else View.VISIBLE

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val searchView = findViewById<SearchView>(R.id.location_search)
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()

        val editText = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)

        val color = ContextCompat.getColor(this, R.color.colorBlack)
        val hintColor = ContextCompat.getColor(this, R.color.kindOfGray)
        editText.setTextColor(color)
        editText.setHintTextColor(hintColor)

        val chooseOnMapItemContainer = findViewById<LinearLayout>(R.id.choose_on_map_item_container)

        chooseOnMapItemContainer.setOnClickListener {
            val intent = Intent(this@PlacesActivity, ChooseOnMapActivity::class.java)
            startActivityForResult(intent, CHOOSE_ON_MAP_REQUEST_CODE)
        }

        val chooseCurrentLocationItemContainer = findViewById<LinearLayout>(R.id.choose_current_location_item_container)

        chooseCurrentLocationItemContainer.setOnClickListener {
            if (!LocationsUtil.locationSettingsAvailability(locationManager)) {
                LocationsUtil.retrieveLocationSettings(null, this@PlacesActivity)
            } else if (!LocationsUtil.locationPermissionsGranted(this@PlacesActivity)) {
                LocationsUtil.requestPermissions(this@PlacesActivity)
            } else {
                chooseCurrentLocation()
            }
        }

        val favouriteLocationsListHeaderContainer = findViewById<LinearLayout>(R.id.favourite_locations_list_header_container)
        val placesListHeaderContainer = findViewById<LinearLayout>(R.id.places_list_header_container)

        adapter = PlacesAdapter(placesListHeaderContainer)
        favouritePlacesAdapter = FavouritePlacesAdapter(this, favouriteLocationRepository, this)

        val searchBoxNoEmptyQuery = SearchBoxNoEmptyQuery(searchView,
                adapter,
                favouritePlacesAdapter,
                chooseOnMapItemContainer,
                chooseCurrentLocationItemContainer,
                favouriteLocationsListHeaderContainer,
                favouriteLocationRepository)
        connection += searchBox.connectView(searchBoxNoEmptyQuery)
        connection += searcher.connectHitsView(adapter) { hits -> hits.hits }

        val placesList = findViewById<RecyclerView>(R.id.places_list)

        placesList.let {
            it.itemAnimator = null
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
            it.autoScrollToStart(adapter)
        }

        val favouriteLocationsList = findViewById<RecyclerView>(R.id.favourite_locations_list)

        favouriteLocationsList.let {
            it.itemAnimator = null
            it.adapter = favouritePlacesAdapter
            it.layoutManager = LinearLayoutManager(this)
            it.autoScrollToStart(adapter)
        }

        placesList.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                val placeLanguage = adapter.currentList[position]
                val point = placeLanguage.geolocation[0]
                val locationWithName = Location(placeLanguage.localNames[0], point.latitude.toDouble(), point.longitude.toDouble())
                locationChosen(locationWithName)
            }
        })
    }

    private fun chooseCurrentLocation() {
        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    try {
                        val address = LocationsUtil.retrieveAddressFromLatAndLng(this@PlacesActivity, location.latitude, location.longitude)
                        val locationWithName = Location(address, location.latitude, location.longitude)
                        locationChosen(locationWithName)
                    } catch (e: IOException) {
                        val view: View = findViewById(android.R.id.content)
                        val snackBar: Snackbar = Snackbar.make(view, R.string.location_not_found_message, Snackbar.LENGTH_SHORT)
                        snackBar.setAction(R.string.dismiss_snack_bar) { snackBar.dismiss() }
                        snackBar.show()
                    }
                } else {
                    val view: View = findViewById(android.R.id.content)
                    val snackBar: Snackbar = Snackbar.make(view, R.string.location_not_found_message, Snackbar.LENGTH_SHORT)
                    snackBar.setAction(R.string.dismiss_snack_bar) { snackBar.dismiss() }
                    snackBar.show()
                }
                fusedLocationProviderClient.removeLocationUpdates(this)
            }
        }
        val locationRequest = LocationsUtil.createLocationRequest()
        this.fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun locationChosen(location: Location) {
        val intent = Intent()
        intent.putExtra(LOCATION_KEY, location)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LocationsUtil.LOCATION_REQUEST_CHECK_SETTINGS && resultCode == Activity.RESULT_OK) {
            if (LocationsUtil.locationPermissionsGranted(this)) {
                chooseCurrentLocation()
            } else {
                LocationsUtil.requestPermissions(this)
            }
        } else if (requestCode == CHOOSE_ON_MAP_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val location = data?.getSerializableExtra(LOCATION_KEY) as Location
            locationChosen(location)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LocationsUtil.LOCATION_PERMISSIONS_REQUEST
                && LocationsUtil.locationPermissionsGranted(this)) {
            if (LocationsUtil.locationSettingsAvailability(locationManager)) {
                chooseCurrentLocation()
            } else {
                LocationsUtil.retrieveLocationSettings(null, this)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        connection.disconnect()
        ConnectivityReceiver.connectivityReceiverListener = null
        unregisterReceiver(connectivityReceiver)
    }

    override fun onItemClick(position: Int) {
        val favouriteLocation = favouritePlacesAdapter.favouriteLocations[position]
        locationChosen(favouriteLocation.location)
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if (currentNetworkAvailability != isConnected) {
            if (isConnected) {
                searcher.searchAsync()
                noInternetConnectionMessageView.visibility = View.GONE
            } else {
                noInternetConnectionMessageView.visibility = View.VISIBLE
            }
            currentNetworkAvailability = isConnected
        }
    }
}