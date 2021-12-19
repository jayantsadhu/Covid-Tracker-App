package com.example.covidtracker

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    lateinit var worldCasesTV:TextView
    lateinit var worldRecoveredTV:TextView
    lateinit var worldDeathsTV:TextView
    lateinit var countryCasesTV:TextView
    lateinit var countryRecoveredTV:TextView
    lateinit var countryDeathsTV:TextView
    lateinit var stateRV:RecyclerView
    lateinit var stateRVAdapter: StateRVAdapter
    lateinit var stateList: List<StateModal>
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        worldCasesTV = findViewById(R.id.idTVWorldCases)
        worldRecoveredTV = findViewById(R.id.idTVWorldRecovered)
        worldDeathsTV = findViewById(R.id.idTVWorldDeaths)
        countryCasesTV = findViewById(R.id.idTVIndiaCases)
        countryRecoveredTV = findViewById(R.id.idTVIndiaRecovered)
        countryDeathsTV = findViewById(R.id.idTVIndiaDeaths)
        stateRV = findViewById(R.id.idRVStates)
        stateList = ArrayList<StateModal>()

        sharedPreferences = this.getSharedPreferences("com.example.covidtracker",Context.MODE_PRIVATE)

        if (!checkForInternet(this))
            fetchDataFromLocal()
        getWorldInfo()
        getStateInfo()
    }

    private fun getWorldInfo(){
        val url = "https://corona.lmao.ninja/v3/covid-19/all"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val request =
            JsonObjectRequest(Request.Method.GET, url,null,{ response ->
                try {
                    val worldCases : Int = response.getInt("cases")
                    val worldRecovered : Int = response.getInt("recovered")
                    val worldDeaths : Int = response.getInt("deaths")
                    sharedPreferences.edit().putString("worldCases", worldCases.toString()).apply()
                    sharedPreferences.edit().putString("worldRecovered", worldRecovered.toString()).apply()
                    sharedPreferences.edit().putString("worldDeaths", worldDeaths.toString()).apply()
                    worldCasesTV.text = worldCases.toString()
                    worldRecoveredTV.text = worldRecovered.toString()
                    worldDeathsTV.text = worldDeaths.toString()
                }catch (e:JSONException){
                    e.printStackTrace()
                }

            }, {
                    error->
                {
                    Toast.makeText(this, "Fail to get Data", Toast.LENGTH_SHORT).show()
                }
            })
        queue.add(request)
    }

    private fun getStateInfo(){
        val url = "https://api.rootnet.in/covid19-in/stats/latest"
        val queue = Volley.newRequestQueue(this@MainActivity)
        val request =
            JsonObjectRequest(Request.Method.GET, url, null, { response ->
                try {
                    val dataObj = response.getJSONObject("data")
                    val summaryObj = dataObj.getJSONObject("summary")
                    val cases:Int = summaryObj.getInt("total")
                    val recovered:Int = summaryObj.getInt("discharged")
                    val deaths:Int = summaryObj.getInt("deaths")

                    sharedPreferences.edit().putString("stateWise", dataObj.toString()).apply()
                    sharedPreferences.edit().putString("cases", cases.toString()).apply()
                    sharedPreferences.edit().putString("recovered", recovered.toString()).apply()
                    sharedPreferences.edit().putString("deaths", deaths.toString()).apply()

                    countryCasesTV.text = cases.toString()
                    countryRecoveredTV.text = recovered.toString()
                    countryDeathsTV.text = deaths.toString()

                    val regionalArray = dataObj.getJSONArray("regional")
                    for (i in 0 until regionalArray.length()){
                        val regionalObj = regionalArray.getJSONObject(i)
                        val stateName:String = regionalObj.getString("loc")
                        val cases:Int = regionalObj.getInt("totalConfirmed")
                        val deaths:Int = regionalObj.getInt("deaths")
                        val recovered:Int = regionalObj.getInt("discharged")

                        val stateModal = StateModal(stateName,recovered,deaths,cases)
                        stateList = stateList + stateModal

                    }
                    stateRVAdapter = StateRVAdapter(stateList)
                    stateRV.layoutManager = LinearLayoutManager(this)
                    stateRV.adapter = stateRVAdapter

                }catch (e:JSONException){
                    e.printStackTrace()
                }
            }, {error -> {
                Toast.makeText(this,"Fail to get Data", Toast.LENGTH_SHORT).show()
            }

            })
        queue.add(request)
    }

    private fun fetchDataFromLocal(){
        val worldCases = sharedPreferences.getString("worldCases","World Cases")
        val worldRecovered = sharedPreferences.getString("worldRecovered","World Recovered")
        val worldDeaths = sharedPreferences.getString("worldDeaths","World Deaths")
        worldCasesTV.text = worldCases.toString()
        worldRecoveredTV.text = worldRecovered.toString()
        worldDeathsTV.text = worldDeaths.toString()

        val cases = sharedPreferences.getString("cases","Cases")
        val recovered = sharedPreferences.getString("recovered","Recovered")
        val deaths = sharedPreferences.getString("deaths","Deaths")
        countryCasesTV.text = cases.toString()
        countryRecoveredTV.text = recovered.toString()
        countryDeathsTV.text = deaths.toString()

        val dataObj = JSONObject(sharedPreferences.getString("stateWise","State Wise"))
        val regionalArray = dataObj.getJSONArray("regional")
        if(regionalArray.length()==0) return
        for (i in 0 until regionalArray.length()){
            val regionalObj = regionalArray.getJSONObject(i)
            val stateName:String = regionalObj.getString("loc")
            val cases:Int = regionalObj.getInt("totalConfirmed")
            val deaths:Int = regionalObj.getInt("deaths")
            val recovered:Int = regionalObj.getInt("discharged")

            val stateModal = StateModal(stateName,recovered,deaths,cases)
            stateList = stateList + stateModal
        }
        stateRVAdapter = StateRVAdapter(stateList)
        stateRV.layoutManager = LinearLayoutManager(this)
        stateRV.adapter = stateRVAdapter
    }

    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}