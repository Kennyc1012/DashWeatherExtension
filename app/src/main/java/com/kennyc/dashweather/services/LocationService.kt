package com.kennyc.dashweather.services

class LocationService {
    /* private val TAG = "LocationService"

     @SuppressWarnings("MissingPermission")
     override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
         Log.v(TAG, "Fetching Location")

         val request = LocationRequest.create()
                 .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                 .setExpirationDuration(DateUtils.MINUTE_IN_MILLIS)
                 .setNumUpdates(1)

         LocationServices.getFusedLocationProviderClient(applicationContext).requestLocationUpdates(request, locationCallback, null)
         return Service.START_NOT_STICKY
     }

     override fun onBind(p0: Intent?): IBinder? {
         return null
     }

     override fun onDestroy() {
         Log.v(TAG, "onDestroy")
         super.onDestroy()
         LocationServices.getFusedLocationProviderClient(applicationContext).removeLocationUpdates(locationCallback)
     }

     private val locationCallback = object : LocationCallback() {
         override fun onLocationResult(result: LocationResult?) {
             Log.v(TAG, "Received Location")

             result?.let {
                 val location = result.locations[0]
                 val intent = Intent(DarkSkyDashExtension.INTENT_ACTION)
                         .putExtra(DarkSkyDashExtension.EXTRA_LONGITUDE, location.longitude)
                         .putExtra(DarkSkyDashExtension.EXTRA_LATITUDE, location.latitude)

                 applicationContext.sendBroadcast(intent)
                 LocationServices.getFusedLocationProviderClient(applicationContext).removeLocationUpdates(this)
                 stopSelf()
             }
         }
     }*/

}