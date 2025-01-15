package com.example.moneyconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    /**  XIN CHAO! DAY LA PHAN MEM VIET BOI BUI TAN MINH - JONATHAN BUI
     * * NGAY CHINH SUA CUOI CUNG: 15 Thang Mot 2025
     * * HELLO! THIS APPLICATION IS MADE BY BUI TAN MINH - JONATHAN BUI
     * * LAST UPDATE ON: Jan 15 2025
     * * <3
     **/


    //get API key
    private val apiKey = com.example.moneyconverter.BuildConfig.API_KEY

    private lateinit var edtInput: EditText
    private lateinit var edtResult: EditText
    private lateinit var btnConvert: Button
    private lateinit var btnCopy: Button
    private lateinit var spnFormCurrency: Spinner
    private lateinit var spnToCurrency: Spinner
    private lateinit var progressBar: ProgressBar

    data class ExchangeRatesResponse(
        val success: Boolean,
        val timestamp: Long,
        val base: String,
        val date: String,
        val rates: Map<String, Double>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Mapping
        edtInput = findViewById(R.id.edt_from_money_input)
        edtResult = findViewById(R.id.edt_to_money_result)
        btnConvert = findViewById(R.id.btn_convert)
        btnCopy = findViewById(R.id.btn_copy)
        spnFormCurrency = findViewById(R.id.spn_from_currency)
        spnToCurrency = findViewById(R.id.spn_to_currency)
        progressBar = findViewById(R.id.progressBar)
        //Set focus
        edtInput.requestFocus()

        //Import Data to 2 Spinner
        spinnerSetup()

        //Internet and mobile data checking
        //Internet connect = false
        if (!isInternetAvailable(this)){
            showInternetWarning(this)
        }

        if(isUsingMobileData()){
            showMobileDataWarning()
        }


        //Click Handler
        btnConvert.setOnClickListener {
            //Progress bar
            progressBar.visibility = View.VISIBLE

            //Internet connect = true && no empty input
            if (isInternetAvailable(this)){
                if (edtInput.text != null || edtInput.text.isNotBlank() || edtInput.text.isNotEmpty()) {

                    val fromCurrency = spnFormCurrency.selectedItem.toString()
                    val toCurrency = spnToCurrency.selectedItem.toString()
                    val among = edtInput.text.toString().toDouble()

                    println("FROM: ${fromCurrency}| TO: ${toCurrency}| AMONG: $among")
                    getApiData(fromCurrency,toCurrency,among)
                    progressBar.visibility = View.GONE

                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "You need to input among!", Toast.LENGTH_SHORT).show()

                }
            }
            else{
                progressBar.visibility = View.GONE
                showInternetWarning(this)

            }
        }


        // Copy Result.text to Clipboard
        btnCopy.setOnClickListener {
            val textToCopy = edtResult.text.toString()
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textToCopy)
            clipboard.setPrimaryClip(clip)
            //pop-up announcement
            Toast.makeText(this, "Result copied!", Toast.LENGTH_SHORT).show()
        }


    }


    private fun showMobileDataWarning() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Warning!")
        builder.setMessage("You are using mobile data. Switch to Wi-Fi to save your data")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun isUsingMobileData(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        } else {
            false // limit on old version Android
        }
    }

    private fun spinnerSetup() {
        val currencyFrom = resources.getStringArray(R.array.currencyListFrom)
        val currencyTo = resources.getStringArray(R.array.currencyListTo)

        val arrayAdapterFrom =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyFrom)
        arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val arrayAdapterTo = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyTo)
        arrayAdapterFrom.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spnFormCurrency.adapter = arrayAdapterFrom
        spnToCurrency.adapter = arrayAdapterTo
    }


    private fun getApiData(currencyInput: String, convertToCurrency: String, among: Double) {

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // URL API
                val apiUrl =
                    "https://api.exchangeratesapi.io/v1/latest?access_key=$apiKey&symbols=USD,EUR,CHF,GBP,JPY,AUD,CAD,SGD,NOK,SEK,VND&format=1"

                // OkHttpClient
                val client = OkHttpClient()

                // Request
                val request = Request.Builder()
                    .url(apiUrl)
                    .build()

                // Send response
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    withContext(Dispatchers.Main) {
                        println("Error fetching data: CODE: ${response.code}| BODY: ${response.body} | MESSAGE: ${response.message}")
                    }
                    return@launch
                }

                // Read JSON
                val jsonResponse = response.body?.string()
                if (jsonResponse != null) {
                    // Parse JSON by Gson
                    val gson = Gson()
                    val exchangeRatesResponse =
                        gson.fromJson(jsonResponse, ExchangeRatesResponse::class.java)

                    // Get rates
                    val rates = exchangeRatesResponse.rates
                    val inputRate = rates[currencyInput] ?: 0.0
                    val convertRate = rates[convertToCurrency] ?: 0.0

                    // Check rates
                    if (inputRate != 0.0 && convertRate != 0.0) {
                        // calculate
                        val finalResult = (convertRate / inputRate) * among

                        // Send result to EditText
                        runOnUiThread {
                            val df = DecimalFormat("#,###.##")  // 23,244,000.54
                            val formattedResult = df.format(finalResult)
                            edtResult.setText(formattedResult)
                        }
                    } else {
                        println("Invalid currency code or missing rates")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        println("Empty response body")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    println("ERROR OCCURRED: ${e.message}")
                }
            }
        }

    }

   private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showInternetWarning(context: Context) {

            AlertDialog.Builder(context)
                .setTitle("No Internet Connection")
                .setMessage("You are not connected to the internet. Please check your connection.")
                .setCancelable(false)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()

    }
}
