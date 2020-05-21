package org.mad.transit.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.Toast
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
import kotlinx.android.synthetic.main.place_item.view.*
import org.mad.transit.R
import org.mad.transit.adapters.PlacesAdapter
import org.mad.transit.util.LocationsUtil
import org.mad.transit.view.OnItemClickListener
import org.mad.transit.view.SearchBoxNoEmptyQuery
import org.mad.transit.view.addOnItemClickListener
import java.io.IOException

class PlacesActivity : AppCompatActivity() {

    companion object {
        const val LOCATION_KEY = "LOCATION"
        const val CHOOSE_ON_MAP_REQUEST_CODE = 555
    }

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    private val query = PlacesQuery(
            type = PlaceType.Address,
            hitsPerPage = 10,
            aroundLatLngViaIP = true,
            countries = listOf(Country.Serbia)
    )
    private val searcher = SearcherPlaces(query = query, language = Language.Other("rs"))
    private val searchBox = SearchBoxConnector(searcher)
    val adapter = PlacesAdapter()
    private val connection = ConnectionHandler(searchBox)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.places_activity)

        val chooseOnMapDefaultOption = resources.getString(R.string.choose_on_map)
        val chooseCurrentLocationDefaultOption = resources.getString(R.string.choose_current_location)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val searchView = findViewById<SearchView>(R.id.location_search)
        searchView.setIconifiedByDefault(false)
        searchView.requestFocus()

        val editText = searchView.findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)

        val color = ContextCompat.getColor(this, R.color.colorBlack)
        editText.setTextColor(color)
        editText.setHintTextColor(color)

        connection += searchBox.connectView(SearchBoxNoEmptyQuery(searchView, adapter))
        connection += searcher.connectHitsView(adapter) { hits -> hits.hits }

        val placesList = findViewById<RecyclerView>(R.id.location_search_list)

        placesList.let {
            it.itemAnimator = null
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(this)
            it.autoScrollToStart(adapter)
        }

        placesList.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                when (val place = view.place_item_text.text.toString()) {
                    chooseOnMapDefaultOption -> {
                        val intent = Intent(this@PlacesActivity, ChooseOnMapActivity::class.java)
                        startActivityForResult(intent, CHOOSE_ON_MAP_REQUEST_CODE)
                    }
                    chooseCurrentLocationDefaultOption -> {
                        if (!LocationsUtil.locationSettingsAvailability(locationManager)) {
                            LocationsUtil.retrieveLocationSettings(null, this@PlacesActivity)
                        } else if (!LocationsUtil.locationPermissionsGranted(this@PlacesActivity)) {
                            LocationsUtil.requestPermissions(this@PlacesActivity)
                        } else {
                            chooseCurrentLocation()
                        }
                    }
                    else -> {
                        locationChosen(place)
                    }
                }
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
                        locationChosen(address)
                    } catch (e: IOException) {
                        Toast.makeText(this@PlacesActivity, R.string.location_not_found_message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@PlacesActivity, R.string.location_not_found_message, Toast.LENGTH_SHORT).show()
                }
                fusedLocationProviderClient.removeLocationUpdates(this)
            }
        }
        val locationRequest = LocationsUtil.createLocationRequest()
        this.fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun locationChosen(location: String) {
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
            val location = data?.getSerializableExtra(LOCATION_KEY) as String
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
    }
}