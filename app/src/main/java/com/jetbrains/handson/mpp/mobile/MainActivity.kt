package com.jetbrains.handson.mpp.mobile

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.results_table_cell.view.*

class MainActivity : AppCompatActivity(), ApplicationContract.View {
    private lateinit var autoAdapter: ArrayAdapter<String>
    val autoValidator=AutoValidator()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val presenter = ApplicationPresenter()
        presenter.onViewTaken(this)
        setupAutos()
        setupButton(presenter)
        setupTable()
    }

    private fun setupAutos() {
        autoAdapter =
            ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, mutableListOf())
        val autos = listOf<AutoCompleteTextView>(
            findViewById(R.id.arrival_station),
            findViewById(R.id.departure_station)
        )
        autos.forEach {

            it.setAdapter(autoAdapter)
            it.validator = autoValidator
        }
    }
    private fun hideKeyboard(){
        val view = this.currentFocus
        view?.let { v ->
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(v.windowToken, 0)
        }
    }
    private fun showTable(){
        val headers=listOf(resultsTableArrivalHeader,resultsTableDepartureHeader,
            resultsTableChangesHeader,resultsTableCostHeader,resultsTableButtonHeader)
        table_footer.visibility = View.VISIBLE
        headers.forEach {
            it.visibility=View.VISIBLE
        }
    }
    private fun setupButton(presenter: ApplicationPresenter) {
        val button: Button = findViewById(R.id.done_button)

        button.setOnClickListener {
            hideKeyboard()
            showTable()
            val departureArrival=getDepartureArrivalStations()
            if (departureArrival.first!="" && departureArrival.second!="") {
                presenter.onStationsSubmitted(departureArrival.first, departureArrival.second)
            }
        }
    }


    override fun openURL(url: String) {
        startActivity(Intent(ACTION_VIEW, Uri.parse(url)))
    }

    private fun getDepartureArrivalStations(): Pair<String, String> {
        val arrivalText: AutoCompleteTextView = findViewById(R.id.arrival_station)
        arrivalText.performValidation()
        val departureText: AutoCompleteTextView = findViewById(R.id.departure_station)
        departureText.performValidation()
        return Pair(
            departureText.text.toString(),
            arrivalText.text.toString()
        )
    }

    override fun showData(data: List<ApplicationContract.TrainJourney>) {
        val departureTimes = mutableListOf<String>()
        val arrivalTimes = mutableListOf<String>()
        val stationChanges = mutableListOf<String>()
        val pricesList = mutableListOf<String>()

        data.forEach{
            departureTimes.add(it.departureTime)
            arrivalTimes.add(it.arrivalTime)
            stationChanges.add((it.legs- 1).toString())
            if (it.cost%100 == 0) {
                pricesList.add("£" + (it.cost/100).toString() + ".00")
            } else {
                pricesList.add("£" + (it.cost/100).toString() + "." + (it.cost%100).toString())
            }

        }
        obtainedDepartureData = departureTimes
        obtainedArrivalData = arrivalTimes
        obtainedChangesData = stationChanges
        obtainedPricesData = pricesList
        setupTable()
    }



    override fun updateStations(data: List<String>) {
        autoAdapter.clear()
        autoAdapter.addAll(data)
        autoValidator.valid_list=data
    }

    override fun setLabel(text: String) {
        findViewById<TextView>(R.id.main_text).text = text
    }

    override fun showAPIError(info:String) {
        //TODO - bring up error message
        println("API CALL FAILED")
        println(info)
    }
    //Results Table

    private var obtainedDepartureData = listOf<String>()
    private var obtainedArrivalData = listOf<String>()
    private var obtainedChangesData = listOf<String>()
    private var obtainedPricesData = listOf<String>()

    private fun setupTable() {
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ResultsTableAdapter()
        adapter.updateData(obtainedDepartureData,obtainedArrivalData,obtainedChangesData,obtainedPricesData)
        resultsTable.apply {
            this.layoutManager = layoutManager
            this.adapter = adapter
        }
    }
}

//--------------------------------------------------------------------------------------------
//Results Table

class  ResultsTableAdapter: RecyclerView.Adapter<ResultsTableAdapter.MyViewHolder>() {
    private var departureData = emptyList<String>()
    private var arrivalData = emptyList<String>()
    private var changesData = emptyList<String>()
    private var pricesData = emptyList<String>()


    inner class MyViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bindData(departTime: String,arriveTime: String, trainChanges: String, price: String, onButtonClick:() -> Unit) {
            itemView.resultsTableCellDepart.text = departTime
            itemView.resultsTableCellArrive.text = arriveTime
            itemView.resultsTableCellChanges.text = trainChanges
            itemView.resultsTableCellCost.text = price
            itemView.seeMoreButton.setOnClickListener {
                onButtonClick()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.results_table_cell, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return departureData.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(
            departureData[position],arrivalData[position],changesData[position],pricesData[position]
        ) {
            //TODO: Url to website
        }
    }

    fun updateData(departData: List<String>,arriveData: List<String>,changeData: List<String>,priceData: List<String>) {
        departureData = departData
        arrivalData = arriveData
        changesData = changeData
        pricesData = priceData
        notifyDataSetChanged()
    }
}
