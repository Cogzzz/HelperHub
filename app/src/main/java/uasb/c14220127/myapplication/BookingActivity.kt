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
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

class BookingActivity : AppCompatActivity() {
    private var spinner: Spinner? = null
    private var workerId: String = ""
    private lateinit var db: FirebaseFirestore
    private lateinit var confirmButton: Button // Add this at class level
    private lateinit var loadingDialog: AlertDialog
    private var selectedDateTime: Long = 0L

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
    private lateinit var jobRecyclerView: RecyclerView

    private lateinit var priceTextView: TextView  // TextView untuk menampilkan harga
    private var price: Int = 0  // Variabel untuk menyimpan harga yang diteruskan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pembayaran)

        // inisial Firestore
        db = FirebaseFirestore.getInstance()

        // Initialize views
        initializeViews()

        // Setup window insets
        setupWindowInsets()


        // ambil worker ID dari intent
        workerId = intent.getStringExtra("worker_id") ?: ""

        // ambil data dari intent
        selectedDateTime = intent.getLongExtra("scheduledDateTime", 0L)
        val selectedDate = intent.getStringExtra("date")
        val selectedJobs = intent.getStringArrayListExtra("selectedJobs")
        price = intent.getIntExtra("price", 0)  // Ambil harga dari Intent


        confirmButton.setOnClickListener {
            if (validateBooking()) {
                saveBookingToFirebase()
            }
        }

        // Fetch & display data
        if (workerId.isNotEmpty()) {
            fetchWorkerData()
        } else {
            Log.e("BookingActivity", "No worker ID received")
        }
        fetchCurrentUserData()
        displayTaskInfo(selectedDate,selectedJobs)

        // Setup payment spinner
        setupPaymentSpinner()


        // insialisasi loading dialog
        createLoadingDialog()
    }

    private fun validateBooking(): Boolean {
        if (selectedDateTime == 0L) {
            Toast.makeText(this, "Invalid booking date", Toast.LENGTH_SHORT).show()
            return false
        }

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDateTime

        //cek apakah booking diantara jam 08.00 - 17.00
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        if (hour < 8 || hour >= 17) {
            Toast.makeText(this, "Booking time must be between 8 AM and 5 PM", Toast.LENGTH_SHORT).show()
            return false
        }

        // cek apakah pemilihan tanggal bukan di hari ini (minimal pemesanan untuk hari selanjutnya)
        if (selectedDateTime <= System.currentTimeMillis()) {
            Toast.makeText(this, "Booking date must be in the future", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
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
        jobRecyclerView = findViewById(R.id.recyclerView)

        // Payment spinner
        spinner = findViewById(R.id.payment_method_spinner)
        priceTextView = findViewById(R.id.hargaService)
        confirmButton = findViewById(R.id.proceedButton)
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
                            .placeholder(R.drawable.workers)
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
    private fun displayTaskInfo(date: String?, jobs: ArrayList<String>?) {
        // tampilkan date dan price
        val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy - HH:mm", Locale.getDefault())
        dateTextView.text = date ?: dateFormat.format(selectedDateTime)
        priceTextView.text = price.toString()

        //rec view untuk jobs
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
               //kosong
            }
        }
    }

    //simpan ke firebase
    private fun saveBookingToFirebase() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }


        val selectedPaymentMethod = (spinner?.selectedItem as? SpinnerItem)?.text ?: "Unknown"

        //buat kode booking
        val bookingId = db.collection("bookings").document().id

        //ambil daftar job dari rec view adapter
        val jobsList = (jobRecyclerView.adapter as? JobAdapter)?.getJobs() ?: listOf()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val scheduledDate = dateFormat.format(selectedDateTime)
        val scheduledTime = timeFormat.format(selectedDateTime)
        val bookingDate = dateFormat.format(selectedDateTime)

        val booking = BookingData(
            bookingId = bookingId,
            userId = currentUser.uid,
            workerId = workerId,
            date = bookingDate,
            scheduledDateTime = selectedDateTime,
            scheduledDate = scheduledDate,
            scheduledTime = scheduledTime,
            jobs = jobsList,
            price = price,
            paymentMethod = selectedPaymentMethod,
            userName = userNameTv.text.toString(),
            userAddress = userMainAddressTv.text.toString(),
            userPhone = userPhoneTv.text.toString(),
            workerName = workerNameTv.text.toString(),
            workerAddress = workerAddressTv.text.toString(),
            workerPhone = workerPhoneTv.text.toString(),
            status = "pending"
        )

        showLoadingDialog()

        //cek ketersediaan worker pada saat pemilihan schedule
        checkWorkerAvailability(bookingDate) { isAvailable ->
            if (isAvailable) {
                saveBookingAndCreateInvoice(booking)
            } else {
                hideLoadingDialog()
                Toast.makeText(this, "Worker is not available for this date", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkWorkerAvailability(date: String, callback: (Boolean) -> Unit) {
        db.collection("bookings")
            .whereEqualTo("workerId", workerId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty())
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    private fun saveBookingAndCreateInvoice(booking: BookingData) {
        db.collection("bookings")
            .document(booking.bookingId)
            .set(booking)
            .addOnSuccessListener {
                createInvoice(booking)
                saveBookingReferenceToWorker(booking.bookingId)
                saveBookingReferenceToUser(booking.bookingId)
                // Show success dialog before navigating
                showBookingSuccessDialog()
            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(this, "Error saving booking: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createInvoice(booking: BookingData) {
        val invoice = InvoiceData(
            bookingId = booking.bookingId,
            userId = booking.userId,
            workerId = booking.workerId,
            workerName = booking.workerName,
            date = booking.date,
            scheduledDateTime = booking.scheduledDateTime,
            amount = booking.price,
            paymentMethod = booking.paymentMethod,
            timestamp = System.currentTimeMillis()
        )

        db.collection("invoices")
            .document(booking.bookingId)
            .set(invoice)
            .addOnSuccessListener {
                hideLoadingDialog()
                navigateToHomePage()
            }
            .addOnFailureListener { e ->
                hideLoadingDialog()
                Toast.makeText(this, "Error creating invoice: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHomePage() {
        val intent = Intent(this, HomePageActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun createLoadingDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.loading_dialog, null)
        builder.setView(dialogView)
        builder.setCancelable(false)
        loadingDialog = builder.create()
    }

    private fun showBookingSuccessDialog() {
        AlertDialog.Builder(this)
            .setTitle("Booking Success")
            .setMessage("Your booking has been successfully created!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                navigateToHomePage()
            }
            .setCancelable(false)
            .show()
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
}
