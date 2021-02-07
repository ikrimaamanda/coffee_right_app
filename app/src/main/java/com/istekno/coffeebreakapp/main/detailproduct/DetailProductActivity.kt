package com.istekno.coffeebreakapp.main.detailproduct

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import com.bumptech.glide.Glide
import com.istekno.coffeebreakapp.R
import com.istekno.coffeebreakapp.base.BaseActivityViewModel
import com.istekno.coffeebreakapp.databinding.ActivityDetailProductBinding
import com.istekno.coffeebreakapp.main.cart.CartActivity
import com.istekno.coffeebreakapp.remote.ApiClient
import com.istekno.coffeebreakapp.utilities.SharedPreferenceUtil
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class DetailProductActivity :
    BaseActivityViewModel<ActivityDetailProductBinding, DetailProductViewModel>() {

    companion object {
        const val HOME_KEY = "home_key"
        const val img = "http://184.72.105.243:3000/images/"
    }

    private lateinit var sharePref: SharedPreferenceUtil
    private var prID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setLayout = R.layout.activity_detail_product
        setViewModel = ViewModelProvider(this).get(DetailProductViewModel::class.java)
        super.onCreate(savedInstanceState)

        sharePref = SharedPreferenceUtil(this)
        val service = ApiClient.getApiClient(this)?.create(DetailProductApiService::class.java)
        if (service != null) {
            viewModel.setService(service)
        }
        viewModel.setSharePref(sharePref)
        viewModel.getListCartByCsId()

        prID = intent.getIntExtra(HOME_KEY, -1)
        viewModel.getProductDetail(prID)
        viewModel.checkProductOnCartApi(prID, sharePref.getPreference().roleID!!)

        subscribeLiveData()
        onClickListener()
        setBadgeCart()
    }

    private fun setBadgeCart() {
        val id = binding.cartBadge

            viewModel.listCart.observe(this, {
                if (it != 0) {
                    id.visibility = View.VISIBLE
                    id.text = it.toString()
                }
            })


    }

    private fun onClickListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.ivCart.setOnClickListener {
            intent<CartActivity>(this)
        }

        binding.btnAddCart.setOnClickListener {
            val customerId = sharePref.getPreference().roleID

            viewModel.isCheckProduct.observe(this, androidx.lifecycle.Observer {
                if (it) {
                    viewModel.orderId.observe(this, androidx.lifecycle.Observer { orderId ->
                        viewModel.updateAmountOrderApi(orderId)
                    })
                } else {
                    viewModel.createOrder(prID, customerId!!)
                }
            })

            subscribeCreateOrder()
            subscribeUpdateLiveData()
        }
    }

    private fun subscribeUpdateLiveData() {
        viewModel.isUpdateSuccess.observe(this, androidx.lifecycle.Observer {
            if (it) {
                Toast.makeText(this, "Success add amount of this product!", Toast.LENGTH_SHORT)
                    .show()
                intent<CartActivity>(this)
                finish()
            } else {
                Toast.makeText(this, "Add to cart failed!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeLiveData() {
        viewModel.isLoading.observe(this) {
            if (it) {
                binding.scrollView.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.scrollView.visibility = View.VISIBLE
            }
        }

        viewModel.listData.observe(this) {

            if (it[0].discountId == 0 || it[0].discountId == 1) {
                binding.lineThrough.visibility = View.GONE
                binding.tvPromoPrice.visibility = View.GONE

                val formatter = DecimalFormat("#,###")
                val price = formatter.format(it[0].productPrice.toDouble())

                binding.tvPrice.text = "IDR $price"

            } else {
                binding.lineThrough.visibility = View.VISIBLE
                binding.tvPromoPrice.visibility = View.VISIBLE

                val formatter = DecimalFormat("#,###")
                val price = formatter.format(it[0].productPrice.toDouble())
                val promoPrice =  it[0].productPrice.toInt() - (it[0].productPrice.toInt() * 0.1)

                binding.tvPromoPrice.text = "IDR $price"
                binding.tvPrice.text = "IDR ${formatter.format(promoPrice)}"

            }

            binding.model = it[0]
            Glide.with(binding.root).load(img + it[0].productImage)
                .placeholder(R.drawable.error)
                .error(R.drawable.ic_profile).into(binding.ivProduct)

        }
    }

    private fun subscribeCreateOrder() {
        viewModel.isCreateSuccess.observe(this) {
            if (it) {
                Toast.makeText(this, "Success add to cart!", Toast.LENGTH_SHORT).show()
                intent<CartActivity>(this)
                finish()
            } else {
                Toast.makeText(this, "Add to cart failed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}