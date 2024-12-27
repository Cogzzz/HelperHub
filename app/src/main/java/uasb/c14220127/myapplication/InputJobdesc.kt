package uasb.c14220127.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputJobdesc : AppCompatActivity() {
    private var selectedDate = ""
    private var selectedDuration = ""
    private var workerId: String = ""
    private var selectedPrice: Int = 0 // Harga yang dipilih berdasarkan durasi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_input_jobdesc)

        val flowLayout = findViewById<FlexboxLayout>(R.id.flowLayout)
        val proceedButton = findViewById<Button>(R.id.proceedButton)
        val datePickerButton = findViewById<Button>(R.id.datePickerButton)
        val durationSpinner = findViewById<Spinner>(R.id.durationSpinner)

        // Ambil worker_id dari Intent yang diteruskan dari DetailActivity
        workerId = intent.getStringExtra("worker_id") ?: ""

        // Jobdesc options
        val jobdescList: List<String> =
            mutableListOf("Menyapu", "Mengepel", "Mencuci", "Menyetrika", "Membersihkan Kamar")
        val selectedJobs: MutableList<String> = ArrayList()

        // Create job buttons dynamically
        for (job in jobdescList) {
            val button = Button(this)
            button.text = job
            button.setBackgroundResource(R.drawable.button_default_bg)
            button.setTextColor(resources.getColor(R.color.black, theme))
            button.textSize = 16f
            button.maxLines = 2
            button.setPadding(20, 10, 20, 10)

            val layoutParams = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(8, 8, 8, 8)
            button.layoutParams = layoutParams

            button.setOnClickListener { v: View? ->
                if (selectedJobs.contains(job)) {
                    selectedJobs.remove(job)
                    button.setBackgroundResource(R.drawable.button_default_bg)
                } else {
                    selectedJobs.add(job)
                    button.setBackgroundResource(R.drawable.button_selected_bg)
                }
            }

            flowLayout.addView(button)
        }

        // Date Picker functionality
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy - hh:mm a", Locale.getDefault())
        datePickerButton.setOnClickListener { v: View? ->
            DatePickerDialog(
                this,
                { view: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                    calendar[year, month] = dayOfMonth
                    selectedDate = dateFormat.format(calendar.time)
                    datePickerButton.text = selectedDate
                },
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        // Duration Spinner functionality
        val durations = arrayOf("1 hour", "2 hours", "3 hours", "4 hours")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, durations)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        durationSpinner.adapter = adapter
        durationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedDuration = durations[position]
                selectedPrice = when (selectedDuration) {
                    "1 hour" -> 80000
                    "2 hours" -> 175000
                    "3 hours" -> 225000
                    "4 hours" -> 300000
                    else -> 0
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Proceed button functionality
        proceedButton.setOnClickListener { v: View? ->
            if (selectedDate.isEmpty() || selectedDuration.isEmpty() || selectedJobs.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please complete all fields!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val intent = Intent(
                    this,
                    BookingActivity::class.java
                )
                intent.putStringArrayListExtra(
                    "selectedJobs",
                    ArrayList(selectedJobs)
                )
                intent.putExtra("date", selectedDate)
                intent.putExtra("duration", selectedDuration)
                intent.putExtra("worker_id", workerId)
                intent.putExtra("price", selectedPrice)
                startActivity(intent)
            }
        }
    }
}