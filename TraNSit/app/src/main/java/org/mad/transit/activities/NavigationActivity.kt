package org.mad.transit.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.github.islamkhsh.CardSliderIndicator
import com.github.islamkhsh.CardSliderViewPager
import com.github.islamkhsh.viewpager2.ViewPager2
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import org.mad.transit.MainActivity
import org.mad.transit.R
import org.mad.transit.TransitApplication
import org.mad.transit.adapters.NavigationCardsAdapter
import org.mad.transit.dto.*
import org.mad.transit.fragments.NavigationMapFragment
import org.mad.transit.model.DepartureTime
import org.mad.transit.model.LineDirection
import org.mad.transit.model.Location
import org.mad.transit.model.NavigationStop
import org.mad.transit.navigation.GeofenceHelper
import org.mad.transit.navigation.NavigationService
import org.mad.transit.repository.StopRepository
import org.mad.transit.util.Constants
import org.mad.transit.util.Constants.TIME_FORMAT
import org.mad.transit.util.Constants.getTimeInMilliseconds
import org.mad.transit.util.LocationsUtil
import org.mad.transit.view.model.TimetableViewModel
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class NavigationActivity : AppCompatActivity() {

    companion object {
        const val ROUTE = "route"
        const val START_LOCATION = "start_location"
        const val END_LOCATION = "end_location"
        const val NAVIGATION_DTO = "navigation_Dto"
        const val RECEIVER = "receiver"
        const val DATA_BUNDLE = "data_bundle"
        const val GEOFENCE_NAVIGATION_DTO_LIST = "geofence_navigation_dto_list"
        const val GEOFENCE_NAVIGATION_DTO = "geofence_navigation_dto"
    }

    private lateinit var defaultSharedPreferences: SharedPreferences
    private var route: RouteDto? = null
    private lateinit var startLocation: Location
    private lateinit var endLocation: Location
    private lateinit var endLocations: ArrayList<Location>
    private lateinit var navigationMapFragment: NavigationMapFragment
    private lateinit var cardSliderViewPager: CardSliderViewPager
    private lateinit var navigationCardsAdapter: NavigationCardsAdapter
    private lateinit var geofenceHelper: GeofenceHelper
    private var geofenceNavigationDtoList: ArrayList<GeofenceNavigationDto>? = null
    private var resultReceiver: ResultReceiver? = null
    private var routeParts: ArrayList<List<ActionDto>>? = null
    private var navigationStops: HashMap<Int, List<NavigationStop>>? = null
    private var startTime: Date? = null
    private var cardPosition: Int = -1
    private var startTimes: HashMap<Long, String>? = null

    @Inject
    lateinit var stopRepository: StopRepository

    @Inject
    lateinit var timetableViewModel: TimetableViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        (applicationContext as TransitApplication).appComponent.inject(this)

        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_navigation)

        this.defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        //TODO Retrieve real time from timetable
        this.startTime = Date()

        this.route = this.intent.getParcelableExtra(ROUTE)
        this.startLocation = this.intent.getSerializableExtra(START_LOCATION) as Location
        this.endLocation = this.intent.getSerializableExtra(END_LOCATION) as Location
        val navigationDto: NavigationDto? = this.intent.getParcelableExtra(NAVIGATION_DTO)

        this.resultReceiver = object : ResultReceiver(Handler()) {
            override fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                val geofenceNavigationDto = resultData.getSerializable(GEOFENCE_NAVIGATION_DTO) as GeofenceNavigationDto

                if (geofenceNavigationDto.actionType == ActionType.BUS) {
                    for (stop in navigationStops!![geofenceNavigationDto.position]!!) {
                        if (stop == geofenceNavigationDto.stop as NavigationStop) {
                            stop.setPassed(true)
                            navigationCardsAdapter.notifyDataSetChanged()
                            if (stop == navigationStops!![geofenceNavigationDto.position]!!.last() && geofenceNavigationDto.position == cardSliderViewPager.currentItem) {
                                goToNextCard()
                            }
                            break
                        }
                    }
                } else if (geofenceNavigationDto.position == cardSliderViewPager.currentItem) {
                    goToNextCard()
                }
            }
        }

        if (navigationDto != null) {
            this.cardPosition = navigationDto.cardPosition
            this.routeParts = navigationDto.routeParts
            this.navigationStops = navigationDto.navigationStops
            initNavigationService()
        } else {
            this.navigationStops = HashMap()
            this.routeParts = ArrayList()
            initDataForNavigationCardsAdapter()

            val navigationNotification = defaultSharedPreferences.getBoolean(this.getString(R.string.navigation_notification_pref_key), false)

            if (!navigationNotification) {
                defaultSharedPreferences.edit().putBoolean(this.getString(R.string.service_active_pref_key), true).apply()
                val dialogClickListener = DialogInterface.OnClickListener { dialog: DialogInterface, which: Int ->
                    when (which) {
                        DialogInterface.BUTTON_POSITIVE -> {
                            defaultSharedPreferences.edit().putBoolean(this.getString(R.string.navigation_notification_pref_key), true).apply()
                            dialog.dismiss()
                            initNavigationService()
                        }
                        DialogInterface.BUTTON_NEGATIVE -> {
                            dialog.dismiss()
                            initNavigationService()
                        }
                    }
                }

                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.navigation_notifications_request_title)
                        .setMessage(R.string.navigation_notifications_request_message)
                        .setIcon(R.drawable.ic_baseline_directions_bus_24)
                        .setPositiveButton(R.string.positive_answer, dialogClickListener)
                        .setNegativeButton(R.string.negative_answer, dialogClickListener)
                        .show()
            } else {
                initNavigationService()
            }
        }

        navigationMapFragment = NavigationMapFragment.newInstance()
        val transaction: FragmentTransaction = this.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navigation_map_container, navigationMapFragment).commit()

        navigationMapFragment.setRoute(route)
        navigationMapFragment.setStartLocation(startLocation)
        navigationMapFragment.setEndLocation(endLocation)

        initBusDepartureTimes()
        initCardSliderViewPager()
    }

    override fun onResume() {
        super.onResume()

        val serviceActiveFlag = this.defaultSharedPreferences.getBoolean(getString(R.string.service_active_pref_key), true)

        if (!serviceActiveFlag) {
            val activityIntent = Intent(this, MainActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            this.startActivity(activityIntent)
        }
    }

    private fun initDataForNavigationCardsAdapter() {
        this.geofenceHelper = GeofenceHelper(this)
        this.geofenceNavigationDtoList = ArrayList()

        var currentActionType: ActionType? = null
        var currentLine: String? = null
        var currentActions: MutableList<ActionDto> = ArrayList()
        var actionsCounter = -1
        for (action in route?.actions!!) {
            if (action.type != currentActionType || action.line != null && action.line.number != currentLine) {
                if (currentActionType == ActionType.BUS) {
                    val stops: MutableList<NavigationStop> = ArrayList()
                    for (busAction in currentActions) {
                        val navigationStop = NavigationStop(busAction.stop, false)
                        stops.add(navigationStop)

                        val geofenceNavigationDto = GeofenceNavigationDto(navigationStop, ActionType.BUS, actionsCounter, UUID.randomUUID().toString())
                        geofenceNavigationDtoList!!.add(geofenceNavigationDto)
                    }
                    this.navigationStops!![actionsCounter] = stops
                } else if (actionsCounter == -1) {
                    val geofenceNavigationDto = GeofenceNavigationDto(action.stop, ActionType.WALK, actionsCounter + 1, UUID.randomUUID().toString())
                    geofenceNavigationDtoList!!.add(geofenceNavigationDto)
                }
                actionsCounter++
                currentActions = ArrayList()
                currentActions.add(action)
                this.routeParts!!.add(currentActions)
                currentActionType = action.type
                if (action.line != null) {
                    currentLine = action.line.number
                }
            } else {
                currentActions.add(action)
            }
        }
    }

    private fun initNavigationService() {
        val groupActionsByLine = route?.groupActionsByLine()

        if (groupActionsByLine != null) {
            endLocations = arrayListOf()
            for ((_, actions) in groupActionsByLine) {
                for (action in actions) {
                    if (action.type == ActionType.BUS) {
                        endLocations.add(actions.last().endLocation)
                    }
                }
            }

            defaultSharedPreferences.edit().putBoolean(this.getString(R.string.service_active_pref_key), true).apply()
            startService(endLocations)
        }
    }

    private fun startService(endLocations: ArrayList<Location>) {
        val serviceIntent = Intent(this, NavigationService::class.java)
        serviceIntent.putExtra(NavigationService.END_LOCATIONS, endLocations)
        serviceIntent.putExtra(ROUTE, route)
        serviceIntent.putExtra(START_LOCATION, startLocation)
        serviceIntent.putExtra(END_LOCATION, endLocation)

        val bundle = Bundle()
        bundle.putParcelable(RECEIVER, this.resultReceiver)

        if (this.geofenceNavigationDtoList != null) {
            bundle.putSerializable(GEOFENCE_NAVIGATION_DTO_LIST, this.geofenceNavigationDtoList)

            val navigationDto = NavigationDto(0, this.routeParts, this.navigationStops)
            bundle.putParcelable(NAVIGATION_DTO, navigationDto)
        }

        serviceIntent.putExtra(DATA_BUNDLE, bundle)

        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun initBusDepartureTimes() {
        var totalDuration = 0
        startTimes = HashMap()
        for (routePart in this.routeParts!!) {
            val firstAction = routePart[0]
            if (firstAction.type == ActionType.BUS) {
                val timetableMap = timetableViewModel.findAllByLineIdAndLineDirection(firstAction.line.id, firstAction.lineDirection)
                val timetable = timetableMap[Constants.getCurrentTimetableDay().toString()]
                if (timetable != null) {
                    startTimes!![firstAction.line.id] = getRouteNextDeparture(firstAction, totalDuration, timetable.departureTimes)
                }
            }
            for (action in routePart) {
                totalDuration += action.duration
            }
        }
    }

    private fun initCardSliderViewPager() {
        this.cardSliderViewPager = this.findViewById(R.id.card_slider_view_pager)

        val onPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                zoomOnCurrentPartOfRoute(position)
                val view = cardSliderViewPager.findViewWithTag<View>(position)
                view?.post {
                    val wMeasureSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
                    val hMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                    view.measure(wMeasureSpec, hMeasureSpec)
                    if (cardSliderViewPager.layoutParams.height != view.measuredHeight) {
                        cardSliderViewPager.layoutParams = (cardSliderViewPager.layoutParams as ConstraintLayout.LayoutParams)
                                .also { lp ->
                                    lp.height = view.measuredHeight
                                }
                    }
                }
            }
        }

        cardSliderViewPager.registerOnPageChangeCallback(onPageChangeCallback)

        navigationCardsAdapter = NavigationCardsAdapter(this@NavigationActivity, routeParts, navigationStops, startTimes)
        cardSliderViewPager.adapter = navigationCardsAdapter

        if (cardPosition > -1) {
            cardSliderViewPager.post { cardSliderViewPager.currentItem = cardPosition }
        }

        val previousItem: Button = this.findViewById(R.id.previous_item)
        previousItem.setOnClickListener {
            val currentItem = cardSliderViewPager.currentItem
            if (currentItem > 0) {
                cardSliderViewPager.post { cardSliderViewPager.currentItem = currentItem - 1 }
            }
        }

        val nextItem: Button = this.findViewById(R.id.next_item)
        nextItem.setOnClickListener {
            goToNextCard()
        }

        val cardSliderIndicator: CardSliderIndicator = findViewById(R.id.indicator)
        cardSliderIndicator.defaultIndicator = getDrawable(R.drawable.default_indicator_dot)
        cardSliderIndicator.selectedIndicator = getDrawable(R.drawable.selected_indicator_dot)
    }

    fun zoomOnCurrentPartOfRoute(position: Int) {
        if (!navigationMapFragment.followMyLocation) {
            val startLocation = navigationCardsAdapter.routeParts[position].first().startLocation
            val endLocation = navigationCardsAdapter.routeParts[position].last().endLocation

            val routeBoundsBuilder = LatLngBounds.builder()
            val startLocationLatLng = LatLng(startLocation.latitude, startLocation.longitude)
            val endLocationLatLng = LatLng(endLocation.latitude, endLocation.longitude)
            routeBoundsBuilder.include(startLocationLatLng)
            routeBoundsBuilder.include(endLocationLatLng)

            navigationMapFragment.zoomOnRoute(routeBoundsBuilder.build(), 100)
        }
    }

    private fun goToNextCard() {
        val currentItem = cardSliderViewPager.currentItem
        if (currentItem < cardSliderViewPager.adapter!!.itemCount - 1) {
            cardSliderViewPager.post { cardSliderViewPager.currentItem = currentItem + 1 }
        }
    }

    private fun getRouteNextDeparture(busAction: ActionDto, totalDuration: Int, departureTimes: List<DepartureTime>): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.forLanguageTag("sr-RS"))
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")

        val lineStops = stopRepository.findAllByLineIdAndLineDirection(busAction.line.id, busAction.lineDirection)

        if (busAction.line.id == 4L && busAction.lineDirection == LineDirection.A) {
            lineStops.reverse()
        }

        var stopWaitTime = 0.0
        val stopIndex = lineStops.indexOf(busAction.stop)
        println(stopIndex)
        for (i in 1..(stopIndex + 1)) {
            val previousStop = lineStops[i - 1]
            val nextStop = lineStops[i]
            stopWaitTime += LocationsUtil.calculateDistance(previousStop.location.latitude, previousStop.location.longitude,
                    nextStop.location.latitude, nextStop.location.longitude) * Constants.MILLISECONDS_IN_HOUR / 40 // 40 = bus speed
        }

        for (departureTime in departureTimes) {
            val departureTimeInMS: Long = getTimeInMilliseconds(departureTime.formattedValue)
            // TODO handle 00:00 (and after) departure times
            if (getTimeInMilliseconds(TIME_FORMAT.format(Date())) + totalDuration * Constants.MILLISECONDS_IN_MINUTE < departureTimeInMS + stopWaitTime.toLong()) {
                return dateFormat.format(Date(departureTimeInMS + stopWaitTime.toLong()))
            }
        }
        return dateFormat.format(Date())
    }
}