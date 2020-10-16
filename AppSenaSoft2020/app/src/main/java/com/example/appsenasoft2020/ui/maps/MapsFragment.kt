package com.example.appsenasoft2020.ui.maps

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.appsenasoft2020.MapaActivity
import com.example.appsenasoft2020.R
import com.example.appsenasoft2020.ui.home.HomeViewModel
import com.huawei.hms.maps.CameraUpdateFactory
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.OnMapReadyCallback
import com.huawei.hms.maps.SupportMapFragment
import com.huawei.hms.maps.model.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.Double
import java.util.ArrayList
import com.huawei.hms.maps.model.*
import com.example.appsenasoft2020.ui.utils.NetworkRequestManager.OnNetworkListener
import com.example.appsenasoft2020.ui.utils.NetworkRequestManager.getDrivingRoutePlanningResult


class MapsFragment : Fragment(){

    private lateinit var mapsViewModel: MapsViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        mapsViewModel =
                ViewModelProvider(this).get(MapsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_maps, container, false)

        val btnViaje: Button = root.findViewById(R.id.btnmapa)

        btnViaje.setOnClickListener {
            //FirebaseAuth.getInstance().signOut()
            val intent = Intent (getActivity(), MapaActivity::class.java)
            getActivity()?.startActivity(intent)
        }

        return root
    }


}
