package com.istekno.coffeebreakapp.main.cart

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.istekno.coffeebreakapp.R
import com.istekno.coffeebreakapp.base.BaseActivityViewModel
import com.istekno.coffeebreakapp.databinding.ActivityCartBinding
import com.istekno.coffeebreakapp.main.checkout.CheckoutActivity
import com.istekno.coffeebreakapp.main.maincontent.mainactivity.MainContentActivity
import com.istekno.coffeebreakapp.remote.ApiClient
import com.istekno.coffeebreakapp.utilities.SharedPreferenceUtil
import java.text.DecimalFormat

class CartActivity : BaseActivityViewModel<ActivityCartBinding, CartViewModel>() {

    private var listCart = ArrayList<CartResponse.DataCart>()
    private var sumPrice = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        setLayout = R.layout.activity_cart
        setViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        super.onCreate(savedInstanceState)

        val sharedPref = SharedPreferenceUtil(this)
        val service = ApiClient.getApiClient(this)?.create(CartApiService::class.java)
        if (service != null) {
            viewModel.setService(service)
        }
        viewModel.setSharedPref(sharedPref)
        viewModel.getListCartByCsId()
        viewModel.getListPriceCartByCsId()

        setRecyclerView()
        subscribeGetCartLiveData()
        subscribeLoadingLiveData()
        subscribeUpdateLiveData()
        subscribeDeleteLiveData()
        onClickListener()

    }

    private fun subscribeDeleteLiveData() {
        viewModel.isDeleteSuccess.observe(this, {
            if (it) {
                viewModel.isMessage.observe(this, { msg ->
                    Toast.makeText(this@CartActivity, msg, Toast.LENGTH_SHORT).show()
                })
            } else {
                viewModel.isMessage.observe(this, { msg ->
                    Toast.makeText(this@CartActivity, msg, Toast.LENGTH_SHORT).show()
                })
            }
        })
    }

    private fun subscribeUpdateLiveData() {
        viewModel.isUpdateCart.observe(this, {
            if (it) {
                viewModel.isMessage.observe(this, { msg ->
                    Toast.makeText(this@CartActivity, msg, Toast.LENGTH_SHORT).show()
                })
            } else {
                viewModel.isMessage.observe(this, { msg ->
                    Toast.makeText(this@CartActivity, msg, Toast.LENGTH_SHORT).show()
                })
            }
        })
    }

    private fun subscribeLoadingLiveData() {
        viewModel.isLoading.observe(this, {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeGetCartLiveData() {
        viewModel.isGetListCart.observe(this, {
            if (it) {
                viewModel.listCart.observe(this, { it1 ->
                    (binding.rvProductCart.adapter as CartAdapter).setData(it1)
                })
                binding.rvProductCart.visibility = View.VISIBLE
                viewModel.totalPriceCart.observe(this, { price->
                    sumPrice = price
                    val formatter = DecimalFormat("#,###")

                    binding.tvTotal.text = formatter.format(price.toDouble())
                })
                binding.layoutTotal.visibility = View.VISIBLE
                binding.ivEmptyCart.visibility = View.GONE
                binding.tvNoCart.visibility = View.GONE
                binding.tvDescToAddCart.visibility = View.GONE
                binding.btnStartOrder.visibility = View.GONE
            } else {
                binding.layoutTotal.visibility = View.GONE
                binding.ivEmptyCart.visibility = View.VISIBLE
                binding.tvNoCart.visibility = View.VISIBLE
                binding.tvDescToAddCart.visibility = View.VISIBLE
                binding.btnStartOrder.visibility = View.VISIBLE
            }
        })
    }

    private fun setRecyclerView() {
        val rvAdapter = CartAdapter(listCart)

        binding.rvProductCart.apply {

            layoutManager = LinearLayoutManager(this@CartActivity, RecyclerView.VERTICAL, false)

            rvAdapter.plusItemCartClicked(object : CartAdapter.OnPlusItemCartClickCallBack {
                override fun onPlusItemCartClicked(cartModel: CartResponse.DataCart) {
                    viewModel.updatePlusCartByOrId(cartModel.orderId)
                    rvAdapter.notifyItemChanged(cartModel.orderId)
                }
            })

            rvAdapter.minusItemCartClicked(object : CartAdapter.OnMinusCartClickCallBack {
                override fun minusItemCartClicked(cartModel: CartResponse.DataCart) {
                    if (cartModel.orderAmount.toInt() >= 2) {
                        viewModel.updateMinusCartByOrId(cartModel.orderId)
                        rvAdapter.notifyItemChanged(cartModel.orderId)
                        viewModel.getListPriceCartByCsId()
                    } else if (cartModel.orderAmount.toInt() <= 1) {
                        showDialogDelete(cartModel.orderId)
                        rvAdapter.notifyItemChanged(cartModel.orderId)
                    }
                }
            })

            rvAdapter.notifyDataSetChanged()
            adapter = rvAdapter
        }
    }

    private fun showDialogDelete(orderId: Int) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Remove Product")
        builder.setMessage("Are you sure to remove this product from your Cart ?")
        builder.setPositiveButton("Yes") { _: DialogInterface, _: Int ->
            viewModel.deleteOrderById(orderId)
            intent<MainContentActivity>(this)
            finish()
        }
        builder.setNegativeButton("No") { _: DialogInterface, _: Int -> }
        builder.show()
    }

    private fun onClickListener() {
        binding.ivBack.setOnClickListener {
            intent<MainContentActivity>(this)
            finish()
        }

        binding.fabGoCheckout.setOnClickListener {
            val intent = Intent(this, CheckoutActivity::class.java)
            intent.putExtra("total_price", sumPrice)
            startActivity(intent)
        }

        binding.btnStartOrder.setOnClickListener {
            intent<MainContentActivity>(this)
            finishAffinity()
        }
    }

    override fun onBackPressed() {
        intent<MainContentActivity>(this)
        finish()
    }
}