package uasb.c14220127.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InputJobdesc : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var datePickerButton: Button
    private lateinit var proceedButton: Button
    private var selectedDateTime: Long = 0L
    private var selectedDate: String = ""
    private var workerId: String = ""
    private var selectedPrice: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.act_input_jobdesc)

        initializeViews()
        setupFirestore()
        createJobButtons()
        setupListeners()
    }

    private fun initializeViews() {
        datePickerButton = findViewById(R.id.datePickerButton)
        proceedButton = findViewById(R.id.proceedButton)
    }

    private fun setupFirestore() {
        db = FirebaseFirestore.getInstance()
        workerId = intent.getStringExtra("worker_id") ?: ""
    }

    private fun createJobButtons() {
        val flowLayout = findViewById<FlexboxLayout>(R.id.flowLayout)
        val jobdescList = mapOf(
            "Menyapu" to 50000,
            "Mengepel" to 60000,
            "Mencuci" to 70000,
            "Menyetrika" to 80000,
            "Membersihkan Kamar" to 75000
        )
        val selectedJobs = mutableListOf<String>()

        jobdescList.forEach { (job, price) ->
            Button(this).apply {
                text = job
                setBackgroundResource(R.drawable.button_default_bg)
                setTextColor(resources.getColor(R.color.black, theme))
                textSize = 16f
                maxLines = 2
                setPadding(20, 10, 20, 10)

                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }

                setOnClickListener {
                    if (selectedJobs.contains(job)) {
                        selectedJobs.remove(job)
                        selectedPrice -= price
                        setBackgroundResource(R.drawable.button_default_bg)
                    } else {
                        selectedJobs.add(job)
                        selectedPrice += price
                        setBackgroundResource(R.drawable.button_selected_bg)
                    }
                }

                flowLayout.addView(this)
            }
        }

        setupProceedButton(selectedJobs)
    }

    private fun setupListeners() {
        datePickerButton.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun setupProceedButton(selectedJobs: MutableList<String>) {
        proceedButton.setOnClickListener {
            if (selectedDateTime == 0L || selectedJobs.isEmpty()) {
                Toast.makeText(this, "Please complete all fields!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Intent(this, BookingActivity::class.java).apply {
                putExtra("selectedJobs", ArrayList(selectedJobs))
                putExtra("date", selectedDate)
                putExtra("worker_id", workerId)
                putExtra("price", selectedPrice)
                putExtra("scheduledDateTime", selectedDateTime)
                startActivity(this)
            }
        }
    }

    private fun showDateTimePicker() {
        val currentDateTime = Calendar.getInstance()

        // Tampilkan date picker dulu
        DatePickerDialog(this, { _, year, month, dayOfMonth ->
            // Setelah memilih tanggal, cek ketersediaan dulu sebelum memilih waktu
            val selectedCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
                // Reset waktu ke 00:00:00 untuk memastikan perbandingan tanggal saja
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Basic validation untuk tanggal
            if (selectedCalendar.timeInMillis <= System.currentTimeMillis()) {
                Toast.makeText(this, "Please select a future date", Toast.LENGTH_SHORT).show()
                return@DatePickerDialog
            }

            // Cek ketersediaan tanggal
            checkWorkerAvailability(selectedCalendar) { isAvailable ->
                if (isAvailable) {
                    // Jika tanggal tersedia, tampilkan time picker
                    showTimePicker(selectedCalendar)
                } else {
                    Toast.makeText(
                        this,
                        "Worker sudah memiliki jadwal di tanggal tersebut. Silakan pilih tanggal lain.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        },
            currentDateTime.get(Calendar.YEAR),
            currentDateTime.get(Calendar.MONTH),
            currentDateTime.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker(selectedCalendar: Calendar) {
        TimePickerDialog(this,
            { _, hourOfDay, minute ->
                if (hourOfDay < 8 || hourOfDay >= 17) {
                    Toast.makeText(this, "Please select a time between 8 AM and 5 PM", Toast.LENGTH_SHORT).show()
                    return@TimePickerDialog
                }

                // Set waktu yang dipilih
                selectedCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedCalendar.set(Calendar.MINUTE, minute)

                selectedDateTime = selectedCalendar.timeInMillis

                // Format dan tampilkan tanggal dan waktu yang dipilih
                val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy - HH:mm", Locale.getDefault())
                selectedDate = dateFormat.format(selectedDateTime)
                datePickerButton.text = selectedDate
            },
            8, // Default ke jam 8 pagi
            0,
            true
        ).show()
    }

    private fun checkWorkerAvailability(selectedCalendar: Calendar, callback: (Boolean) -> Unit) {
        // Format tanggal untuk perbandingan (hanya tanggal tanpa waktu)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val selectedDateStr = dateFormat.format(selectedCalendar.time)

        db.collection("bookings")
            .whereEqualTo("workerId", workerId)
            .whereEqualTo("date", selectedDateStr)  // Gunakan field 'date' yang berisi string tanggal
            .get()
            .addOnSuccessListener { documents ->
                // Jika tidak ada dokumen, berarti tanggal tersedia
                callback(documents.isEmpty)

                if (!documents.isEmpty) {
                    selectedDateTime = 0L
                    datePickerButton.text = "Select Date and Time"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Error checking availability: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
                callback(false)
                selectedDateTime = 0L
                datePickerButton.text = "Select Date and Time"
            }
    }
}