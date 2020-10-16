package com.example.appsenasoft2020.ui.FragmentRegister

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.appsenasoft2020.R

class RegisterConductorFragment : Fragment() {

    private lateinit var registerConductorViewModel: RegisterConductorViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        registerConductorViewModel =
                ViewModelProvider(this).get(RegisterConductorViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_register_conductor, container, false)
        //val textView: TextView = root.findViewById(R.id.text_home)
        //registerConductorViewModel.text.observe(viewLifecycleOwner, Observer {
        //    textView.text = it
        //})
        return root
    }
}