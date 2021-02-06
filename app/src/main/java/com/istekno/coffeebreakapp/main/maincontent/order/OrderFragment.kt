package com.istekno.coffeebreakapp.main.maincontent.order

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.navigation.NavigationView
import com.istekno.coffeebreakapp.R
import com.istekno.coffeebreakapp.base.BaseFragmentViewModel
import com.istekno.coffeebreakapp.databinding.FragmentOrderBinding
import com.istekno.coffeebreakapp.main.maincontent.mainactivity.MainContentActivity
import com.istekno.coffeebreakapp.main.maincontent.order.detail.DetailOrderActivity
import com.istekno.coffeebreakapp.remote.ApiClient
import com.istekno.coffeebreakapp.utilities.SharedPreferenceUtil
import kotlinx.coroutines.Runnable

class OrderFragment(
    private val title: TextView,
    private val navDrawer: NavigationView
) : BaseFragmentViewModel<FragmentOrderBinding, OrderViewModel>(),
    OrderAdapter.OnListOrderClickListenerr {

    companion object {
        const val ORDER_HISTORY_KEY = "orID_KEY"
    }

    private var listOrder = ArrayList<OrderResponse.Data>()
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setLayout = R.layout.fragment_order
        setView()
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setViewModel = ViewModelProvider(this).get(OrderViewModel::class.java)
        super.onViewCreated(view, savedInstanceState)
        navDrawer.setCheckedItem(R.id.nav_order)

        val sharedPref = SharedPreferenceUtil(requireContext())
        viewModel.setSharedPref(sharedPref)
        val service = ApiClient.getApiClient(requireContext())?.create(OrderApiService::class.java)
        if (service != null) {
            viewModel.setService(service)
        }

        if (sharedPref.getPreference().level == 0) {
            viewModel.callOrderCustomerApi()
        } else {
            viewModel.callOrderAdminApi()
            binding.btnStartOrder.visibility = View.GONE
        }

        setRecyclerView(view, sharedPref.getPreference().level!!)
        subscribeLiveData()
        subscribeLoadingLiveData()
        viewListener(view)

    }

    private fun viewListener(view: View) {
        binding.btnStartOrder.setOnClickListener {
            intent<MainContentActivity>(view.context)
            activity?.finishAffinity()
        }
    }

    private fun subscribeLiveData() {
        viewModel.getListData.observe(viewLifecycleOwner, {
            if (it) {
                viewModel.listData.observe(viewLifecycleOwner) { list ->
                    Log.e("list", "list")
                    (binding.rvOrderHistory.adapter as OrderAdapter).setData(list)
                }
                binding.rvOrderHistory.visibility = View.VISIBLE
                binding.historyNotFound.visibility = View.GONE
                binding.tvInfo.visibility = View.GONE
                binding.tvNoHistoryYet.visibility = View.GONE
            } else {
                binding.historyNotFound.visibility = View.VISIBLE
                binding.tvInfo.visibility = View.GONE
                binding.tvNoHistoryYet.visibility = View.GONE
            }
        })
    }

    private fun subscribeLoadingLiveData() {
        viewModel.isLoading.observe(viewLifecycleOwner, {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun setRecyclerView(view: View, role: Int) {
        binding.rvOrderHistory.isNestedScrollingEnabled = false
        binding.rvOrderHistory.layoutManager =
            LinearLayoutManager(view.context, RecyclerView.VERTICAL, false)

        val adapter = OrderAdapter(listOrder, this, role)
        binding.rvOrderHistory.adapter = adapter
    }

    @SuppressLint("SetTextI18n")
    private fun setView() {
        title.text = "Order"
    }

    override fun onOrderItemClicked(position: Int) {
        val sendIntent = Intent(requireContext(), DetailOrderActivity::class.java)
        sendIntent.putExtra(ORDER_HISTORY_KEY, listOrder[position])
        startActivity(sendIntent)
    }

    private fun dataRefreshManagement() {
        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                viewModel.callOrderAdminApi()
                handler.postDelayed(this, 2000)
            }
        })
    }
}