package com.example.appsenasoft2020

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.appsenasoft2020.ui.utils.NetworkRequestManager
import com.example.appsenasoft2020.ui.utils.NetworkRequestManager.getDrivingRoutePlanningResult
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*


class MapaActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "RoutePlanningDemoActivity"

    private var mSupportMapFragment: SupportMapFragment? = null

    private var hMap: HuaweiMap? = null

    private var mMarkerOrigin: Marker? = null

    private var mMarkerDestination: Marker? = null

    private var edtOriginLat: EditText? = null

    private var edtOriginLng: EditText? = null

    private var edtDestinationLat: EditText? = null

    private var edtDestinationLng: EditText? = null

    private var latLng1 = LatLng(6.2549097,-75.5774016)

    private var latLng2 = LatLng(54.209673, -4.64002)

    private val mPolylines: MutableList<Polyline> = ArrayList()

    private val mPaths: MutableList<MutableList<LatLng>> = ArrayList()

    private var mLatLngBounds: LatLngBounds? = null


    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                0 -> renderRoute(mPaths, mLatLngBounds)
                1 -> {
                    val bundle: Bundle = msg.getData()
                    val errorMsg = bundle.getString("errorMsg")
                    Toast.makeText(this@MapaActivity, errorMsg, Toast.LENGTH_SHORT)
                        .show()
                }
                else -> {
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mapa)

        val fragment: Fragment? =
            supportFragmentManager.findFragmentById(R.id.mapfragment_routeplanningdemo)
        if (fragment is SupportMapFragment) {
            mSupportMapFragment = fragment
            mSupportMapFragment!!.getMapAsync(this)
        }
    }

    override fun onMapReady(huaweiMap: HuaweiMap) {
        hMap = huaweiMap
        hMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 13f))

        getDrivingRouteResult()
    }

    fun getDrivingRouteResult() {
        removePolylines()
        getDrivingRoutePlanningResult(latLng1, latLng2,
            object : NetworkRequestManager.OnNetworkListener {
                override fun requestSuccess(result: String?) {
                    generateRoute(result)
                }

                override fun requestFail(errorMsg: String?) {
                    val msg = Message.obtain()
                    val bundle = Bundle()
                    bundle.putString("errorMsg", errorMsg)
                    msg.what = 1
                    msg.data = bundle
                    mHandler.sendMessage(msg)
                }
            })
    }

    private fun generateRoute(json: String?) {
        try {
            val jsonObject = JSONObject(json)
            val routes = jsonObject.optJSONArray("routes")
            if (null == routes || routes.length() == 0) {
                return
            }
            val route = routes.getJSONObject(0)

            // get route bounds
            val bounds = route.optJSONObject("bounds")
            if (null != bounds && bounds.has("southwest") && bounds.has("northeast")) {
                val southwest = bounds.optJSONObject("southwest")
                val northeast = bounds.optJSONObject("northeast")
                val sw = LatLng(southwest.optDouble("lat"), southwest.optDouble("lng"))
                val ne = LatLng(northeast.optDouble("lat"), northeast.optDouble("lng"))
                mLatLngBounds = LatLngBounds(sw, ne)
            }

            // get paths
            val paths = route.optJSONArray("paths")
            for (i in 0 until paths.length()) {
                val path = paths.optJSONObject(i)
                val mPath: MutableList<LatLng> = ArrayList()
                val steps = path.optJSONArray("steps")
                for (j in 0 until steps.length()) {
                    val step = steps.optJSONObject(j)
                    val polyline = step.optJSONArray("polyline")
                    for (k in 0 until polyline.length()) {
                        if (j > 0 && k == 0) {
                            continue
                        }
                        val line = polyline.getJSONObject(k)
                        val lat = line.optDouble("lat")
                        val lng = line.optDouble("lng")
                        val latLng = LatLng(lat, lng)
                        mPath.add(latLng)
                    }
                }
                mPaths.add(i, mPath)
            }
            mHandler.sendEmptyMessage(0)
        } catch (e: JSONException) {
        }
    }

    /**
     * Render the route planning result
     *
     * @param paths
     * @param latLngBounds
     */
    private fun renderRoute(paths: List<List<LatLng>>?, latLngBounds: LatLngBounds?) {
        if (null == paths || paths.size <= 0 || paths[0].size <= 0) {
            return
        }
        for (i in paths.indices) {
            val path = paths[i]
            val options = PolylineOptions().color(Color.BLUE).width(5f)
            for (latLng in path) {
                options.add(latLng)
            }
            val polyline = hMap!!.addPolyline(options)
            mPolylines.add(i, polyline)
        }
        addOriginMarker(paths[0][0])
        addDestinationMarker(paths[0][paths[0].size - 1])
        if (null != latLngBounds) {
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 5)
            hMap!!.moveCamera(cameraUpdate)
        } else {
            hMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(paths[0][0], 13f))
        }
    }

    fun setOrigin(view: View?) {
        if (!TextUtils.isEmpty(edtOriginLat!!.text) && !TextUtils.isEmpty(edtOriginLng!!.text)) {
            latLng1 =
                LatLng(
                    java.lang.Double.valueOf(edtOriginLat!!.text.toString().trim { it <= ' ' }),
                    java.lang.Double.valueOf(edtOriginLng!!.text.toString().trim { it <= ' ' })
                )
            removePolylines()
            addOriginMarker(latLng1)
            hMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng1, 13f))
            mMarkerOrigin!!.showInfoWindow()
        }
    }

    fun setDestination(view: View?) {
        if (!TextUtils.isEmpty(edtDestinationLat!!.text) && !TextUtils.isEmpty(edtDestinationLng!!.text)) {
            latLng2 = LatLng(
                java.lang.Double.valueOf(
                    edtDestinationLat!!.text.toString().trim { it <= ' ' }),
                java.lang.Double.valueOf(edtDestinationLng!!.text.toString().trim { it <= ' ' })
            )
            removePolylines()
            addDestinationMarker(latLng2)
            hMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 13f))
            mMarkerDestination!!.showInfoWindow()
        }
    }

    private fun addOriginMarker(latLng: LatLng) {
        if (null != mMarkerOrigin) {
            mMarkerOrigin!!.remove()
        }
        mMarkerOrigin = hMap!!.addMarker(
            MarkerOptions().position(latLng)
                .anchor(0.5f, 0.9f) // .anchorMarker(0.5f, 0.9f)
                .title("Origin")
                .snippet(latLng.toString())
        )
    }

    private fun addDestinationMarker(latLng: LatLng) {
        if (null != mMarkerDestination) {
            mMarkerDestination!!.remove()
        }
        mMarkerDestination = hMap!!.addMarker(
            MarkerOptions().position(latLng).anchor(0.5f, 0.9f).title("Destination")
                .snippet(latLng.toString())
        )
    }

    private fun removePolylines() {
        for (polyline in mPolylines) {
            polyline.remove()
        }
        mPolylines.clear()
        mPaths.clear()
        mLatLngBounds = null
    }

}