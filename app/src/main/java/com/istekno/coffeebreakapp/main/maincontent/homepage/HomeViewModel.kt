package com.istekno.coffeebreakapp.main.maincontent.homepage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.istekno.coffeebreakapp.main.cart.CartResponse
import com.istekno.coffeebreakapp.utilities.SharedPreferenceUtil
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class HomeViewModel : ViewModel(), CoroutineScope {

    val isLoading = MutableLiveData<Boolean>()
    val listFavorite = MutableLiveData<MutableList<GetProductResponse.DataProduct>>()
    val listPromo = MutableLiveData<MutableList<GetProductResponse.DataProduct>>()
    val listCart = MutableLiveData<Int>()

    override val coroutineContext: CoroutineContext
        get() = Job() + Dispatchers.Main

    private lateinit var service: HomeService
    private lateinit var sharedPref: SharedPreferenceUtil

    fun setService(service: HomeService) {
        this.service = service
    }

    fun setSharedPref(sharedPreferenceUtil: SharedPreferenceUtil) {
        this.sharedPref = sharedPreferenceUtil
    }

    fun getAllProduct() {
        launch {

            isLoading.value = true
            val resultOrder = withContext(Job() + Dispatchers.IO) {
                try {
                    service.getAllOrder()
                } catch (e: Throwable) {
                    e.printStackTrace()

                    withContext(Job() + Dispatchers.Main) {
                        isLoading.value = false
                    }
                }
            }

            val resultProduct = withContext(Job() + Dispatchers.IO) {
                try {
                    service.getAllProduct()
                } catch (e: Throwable) {
                    e.printStackTrace()

                    withContext(Job() + Dispatchers.Main) {
                        isLoading.value = false
                    }
                }
            }

            if (resultOrder is GetOrderResponse && resultProduct is GetProductResponse) {
                data class Model(val productId: Int, val totalProduct: Int)

                val listTotalOrderPerProductId = mutableListOf<Model>()
                val listIdProductFromOrder = mutableListOf<String>()
                val listProduct = mutableListOf<GetProductResponse.DataProduct>()

                resultProduct.data.map {
                    if (it.productFavorite == "Y") {
                        listProduct.add(
                            GetProductResponse.DataProduct(
                                it.productId,
                                it.discountId,
                                it.productName,
                                it.productDesc,
                                it.productPrice,
                                it.productImage,
                                it.productFavorite,
                                it.productCategory,
                                it.productCreated,
                                it.productUpdated
                            )
                        )
                    }
                }

                resultOrder.data.map {
                    if (it.orderStatus == "Done") listIdProductFromOrder.add(it.productId.toString())
                }

                listIdProductFromOrder.groupingBy { it }.eachCount()
                    .map { listTotalOrderPerProductId.add(Model(it.key.toInt(), it.value)) }
                listTotalOrderPerProductId.sortByDescending { it.totalProduct }

                if (listTotalOrderPerProductId.isNullOrEmpty()) {

                    listFavorite.value = listProduct

                } else {

                    val allFavorite = mutableListOf<GetProductResponse.DataProduct>()
                    val listDrink = mutableListOf<GetProductResponse.DataProduct>()
                    val listFood = mutableListOf<GetProductResponse.DataProduct>()

                    for (i in 0 until listTotalOrderPerProductId.size) {
                        val id = listTotalOrderPerProductId[i].productId

                        resultProduct.data.map {
                            if (it.productId == id) {
                                allFavorite.add(
                                    GetProductResponse.DataProduct(
                                        it.productId,
                                        it.discountId,
                                        it.productName,
                                        it.productDesc,
                                        it.productPrice,
                                        it.productImage,
                                        it.productFavorite,
                                        it.productCategory,
                                        it.productCreated,
                                        it.productUpdated
                                    )
                                )

                                if (it.productCategory == "Drink") {
                                    listDrink.add(
                                        GetProductResponse.DataProduct(
                                            it.productId,
                                            it.discountId,
                                            it.productName,
                                            it.productDesc,
                                            it.productPrice,
                                            it.productImage,
                                            it.productFavorite,
                                            it.productCategory,
                                            it.productCreated,
                                            it.productUpdated
                                        )
                                    )
                                } else {
                                    listFood.add(
                                        GetProductResponse.DataProduct(
                                            it.productId,
                                            it.discountId,
                                            it.productName,
                                            it.productDesc,
                                            it.productPrice,
                                            it.productImage,
                                            it.productFavorite,
                                            it.productCategory,
                                            it.productCreated,
                                            it.productUpdated
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (listTotalOrderPerProductId.size < 4 && (listDrink.size < 2 || listFood.size < 2)) {
                        listFavorite.value = listProduct
                    } else {
                        listFavorite.value = allFavorite
                    }
                }
            }

            if (resultProduct is GetProductResponse) {
                val listProduct = mutableListOf<GetProductResponse.DataProduct>()

                resultProduct.data.map {
                    listProduct.add(
                        GetProductResponse.DataProduct(
                            it.productId,
                            it.discountId,
                            it.productName,
                            it.productDesc,
                            it.productPrice,
                            it.productImage,
                            it.productFavorite,
                            it.productCategory,
                            it.productCreated,
                            it.productUpdated
                        )
                    )
                }

                listProduct.removeIf { it.discountId < 2 }

                listPromo.value = listProduct
            }

            isLoading.value = false
        }
    }

    fun getListCartByCsId() {
        launch {
            isLoading.value = true

            val result = withContext(Dispatchers.IO) {
                try {
                    service.getListCartByCsId(sharedPref.getPreference().roleID!!)
                } catch (e: Throwable) {
                    e.printStackTrace()

                    withContext(Dispatchers.Main) {
                        isLoading.value = false
                    }
                }
            }

            if (result is CartResponse) {
                val data = result.data.map {
                    CartResponse.DataCart(
                        it.orderId,
                        it.productId,
                        it.productName,
                        it.productImage,
                        it.customerId,
                        it.orderStatus,
                        it.orderAmount,
                        it.orderPrice,
                        it.orderCreated,
                        it.orderUpdated
                    )
                }
                listCart.value = data.size
            }
        }
    }

    override fun onCleared() {
        Job().cancel()
        super.onCleared()
    }
}