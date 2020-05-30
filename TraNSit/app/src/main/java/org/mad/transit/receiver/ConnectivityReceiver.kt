package org.mad.transit.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.mad.transit.util.NetworkUtil

class ConnectivityReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(NetworkUtil.isConnected(context!!))
        }

    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }
}