package com.marknkamau.unipool.ui.main.driver

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.ui.driverScheduledRides.DriverScheduledRidesActivity
import com.marknkamau.unipool.ui.main.MainActivity
import com.marknkamau.unipool.ui.preFindRequests.PreFindRequestsActivity
import com.marknkamau.unipool.utils.start
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.fragment_driver_main.*

class DriverMainFragment : Fragment() {
    private val mainActivity by lazy { activity as MainActivity }
    var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_driver_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFindRequests.setOnClickListener {
            if (mainActivity.canViewMap()) {
                if (user?.vehicles?.isEmpty() == true){
                    requireContext().toast("You cannot act as a driver without any vehicles.")
                }else{
                    requireContext().start(PreFindRequestsActivity::class.java)
                }
            }
        }
        btnFindScheduledRides.setOnClickListener {
            requireContext().start(DriverScheduledRidesActivity::class.java)
        }
    }

}