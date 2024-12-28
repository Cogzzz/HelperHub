package uasb.c14220127.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//import androidx.media3.common.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import java.util.Arrays

class BookingActivity : AppCompatActivity() {
    private var spinner: Spinner? = null
    private var workerId: String = ""
    private lateinit var db: FirebaseFirestore
    private lateinit var confirmButton: Button // Add this at class level
    private lateinit var loadingDialog: AlertDialog

    // Worker details views
    private lateinit var workerNameTv: TextView
    private lateinit var workerAddressTv: TextView
    private lateinit var workerPhoneTv: TextView

    // User details views
    private lateinit var userNameTv: TextView
    private lateinit var userMainAddressTv: TextView
    private lateinit var userPhoneTv: TextView

    // Task info views
    private lateinit var dateTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var jobRecyclerView: RecyclerView

    private lateinit var priceTextView: TextView  // TextView untuk menampilkan harga
    private var price: Int = 0  // Variabel untuk menyimpan harga yang diteruskan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pembayaran)

        // Initialize Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()

        // Setup window insets
        setupWindowInsets()


        // Get worker ID from intent
        workerId = intent.getStringExtra("worker_id") ?: ""

        // Get data from intent
        val selectedDate = intent.getStringExtra("date")
        val selectedDuration = intent.getStringExtra("duration")
        val selectedJobs = intent.getStringArrayListExtra("selectedJobs")
        price = intent.getIntExtra("price", 0)  // Ambil harga dari Intent

        confirmButton = findViewById(R.id.proceedButton) // Initialize the button
        confirmButton.setOnClickListener {
            saveBookingToFirebase()
        }

        // Fetch and display data
        if (workerId != null) {
            fetchWorkerData()
        } else {
            Log.e("BookingActivity", "No worker ID received")
        }
        fetchCurrentUserData()
        displayTaskInfo(selectedDate, selectedDuration, selectedJobs)

        // Setup payment spinner
        setupPaymentSpinner()


        // Initialize loading dialog
        createLoadingDialog()
    }

    private fun initializeViews() {
        // Worker details
        workerNameTv = findViewById(R.id.workerName)
        workerAddressTv = findViewById(R.id.workerAddress)
        workerPhoneTv = findViewById(R.id.workerContact)

        // User details
        userNameTv = findViewById(R.id.namaPemesan)
        userMainAddressTv = findViewById(R.id.alamatUtama)
        userPhoneTv = findViewById(R.id.nomorHpPemesan)

        // Task info
        dateTextView = findViewById(R.id.tanggalMulaiKerja)
        durationTextView = findViewById(R.id.lamaKerja)
        jobRecyclerView = findViewById(R.id.recyclerView)

        // Payment spinner
        spinner = findViewById(R.id.payment_method_spinner)

        priceTextView = findViewById(R.id.hargaService)  // Pastikan ada TextView untuk harga
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.paymentpinner)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun fetchWorkerData() {
        Log.d("BookingActivity", "Fetching worker data for ID: $workerId")

        db.collection("workers").document(workerId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("BookingActivity", "Worker document exists: ${document.exists()}")
                if (document != null && document.exists()) {
                    val name = document.getString("name")
                    val address = document.getString("address")
                    val phone = document.getString("phoneNum")

                    Log.d("BookingActivity", "Worker data - Name: $name, Address: $address, Phone: $phone")

                    workerNameTv.text = name ?: "N/A"
                    workerAddressTv.text = address ?: "N/A"
                    workerPhoneTv.text = phone ?: "N/A"

                    document.getString("imageUrl")?.let { imageUrl ->
                        Log.d("BookingActivity", "Loading image from: $imageUrl")
                        Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.women)
                            .into(findViewById(R.id.workerImage))
                    }
                } else {
                    Log.e("BookingActivity", "Worker document does not exist")
                    Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("BookingActivity", "Error fetching worker data", e)
                Toast.makeText(this, "Error fetching worker data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCurrentUserData() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userNameTv.text = document.getString("name") ?: "N/A"
                    userMainAddressTv.text = document.getString("address") ?: "N/A"
                    userPhoneTv.text = document.getString("phone") ?: "N/A"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun displayTaskInfo(date: String?, duration: String?, jobs: ArrayList<String>?) {
        // Display date and duration
        dateTextView.text = date ?: "Not specified"
        durationTextView.text = duration ?: "Not specified"
        priceTextView.text = "$price"  // Tampilkan harga

        // Setup RecyclerView for jobs
        jobs?.let {
            val adapter = JobAdapter(it)
            jobRecyclerView.layoutManager = LinearLayoutManager(this)
            jobRecyclerView.adapter = adapter
        }
    }

    private fun setupPaymentSpinner() {
        val paymentMethods = Arrays.asList(
            SpinnerItem(R.drawable.icon_bca, "BCA Virtual Account"),
            SpinnerItem(R.drawable.mandiri, "Mandiri Virtual Account"),
            SpinnerItem(R.drawable.gopay, "Gopay"),
            SpinnerItem(R.drawable.ovo, "OVO"),
            SpinnerItem(R.drawable.creditcard, "Credit Card"),
            SpinnerItem(R.drawable.cash, "Cash on Delivery")
        )

        val spinnerAdapter = SpinnerAdapter(this, paymentMethods)
        spinner?.adapter = spinnerAdapter

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = paymentMethods[position].text
                Toast.makeText(this@BookingActivity,
                    "Pembayaran Menggunakan: $selectedItem",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // No action needed
            }
        }
    }

    private fun saveBookingToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Get selected payment method
        val selectedPaymentMethod = (spinner?.selectedItem as? SpinnerItem)?.text ?: "Unknown"

        // Create a unique booking ID
        val bookingId = db.collection("bookings").document().id

        // Get the jobs list from the RecyclerView adapter
        val jobsList = (jobRecyclerView.adapter as? JobAdapter)?.getJobs() ?: listOf()

        val booking = BookingData(
            bookingId = bookingId,
            userId = currentUser.uid,
            workerId = workerId,
            date = dateTextView.text.toString(),
            duration = durationTextView.text.toString(),
            jobs = jobsList,
            price = price,
            paymentMethod = selectedPaymentMethod,
            userName = userNameTv.text.toString(),
            userAddress = userMainAddressTv.text.toString(),
            userPhone = userPhoneTv.text.toString(),
            workerName = workerNameTv.text.toString(),
            workerAddress = workerAddressTv.text.toString(),
            workerPhone = workerPhoneTv.text.toString()
        )

        // Show loading dialog before saving
        showLoadingDialog()

        // Simpan ke firebase
        db.collection("bookings")
            .document(bookingId)
            .set(booking)
            .addOnSuccessListener {
                // Save references
                saveBookingReferenceToUser(bookingId)
                saveBookingReferenceToWorker(bookingId)

                hideLoadingDialog()
                Toast.makeText(this, "Booking berhasil disimpan!", Toast.LENGTH_SHORT).show()
                navigateToBookingConfirmation(bookingId)
            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(this, "Error menyimpan booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
    }

    private fun saveBookingReferenceToWorker(bookingId: String) {
        val workerBookingRef = hashMapOf(
            "bookingId" to bookingId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("workers")
            .document(workerId)
            .collection("bookings")
            .document(bookingId)
            .set(workerBookingRef)
    }

    private fun saveBookingReferenceToUser(bookingId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val userBookingRef = hashMapOf(
            "bookingId" to bookingId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(currentUser)
            .collection("bookings")
            .document(bookingId)
            .set(userBookingRef)
    }

    private fun hideLoadingDialog() {
        if (loadingDialog.isShowing) {
            loadingDialog.dismiss()
        }
    }

    private fun showLoadingDialog() {
        if (!loadingDialog.isShowing) {
            loadingDialog.show()
        }
    }
    private fun navigateToBookingConfirmation(bookingId: String) {
        val intent = Intent(this, BookingConfirmationActivity::class.java).apply {
            putExtra("booking_id", bookingId)
        }
        startActivity(intent)
        finish()
    }
}