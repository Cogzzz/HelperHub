package uasb.c14220127.myapplication

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceDetailActivity : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private var currentBookingId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.invoice_detail)

        db = FirebaseFirestore.getInstance()
        currentBookingId = intent.getStringExtra("bookingId")

        // tambah button Mark as Finished untuk mengupdate status pekerjaan
        val markFinishedButton = findViewById<Button>(R.id.markFinishedButton)

        if (currentBookingId != null) {
            loadInvoiceDetails(currentBookingId!!)

            // Check current status to show/hide button
            checkBookingStatus(currentBookingId!!) { status ->
                markFinishedButton.visibility = if (status == "pending") View.VISIBLE else View.GONE
            }

            markFinishedButton.setOnClickListener {
                updateBookingStatus(currentBookingId!!, "finished")
            }
        } else {
            Toast.makeText(this, "Invalid invoice", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadInvoiceDetails(bookingId: String) {
        db.collection("bookings").document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                val booking = document.toObject(BookingData::class.java)
                booking?.let { displayBookingDetails(it) }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading invoice details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayBookingDetails(booking: BookingData) {
        // proses format tanggal dan jam pemesanan
        val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy - HH:mm", Locale.getDefault())
        val date = dateFormat.format(booking.scheduledDateTime)

        findViewById<TextView>(R.id.invoiceNumberText).text = "Invoice #${booking.bookingId.takeLast(6)}"
        findViewById<TextView>(R.id.dateText).text = "Date: $date"
        findViewById<TextView>(R.id.workerNameText).text = "Worker: ${booking.workerName}"
        findViewById<TextView>(R.id.amountText).text = "Amount: Rp ${booking.price}"
        findViewById<TextView>(R.id.paymentMethodText).text = "Payment Method: ${booking.paymentMethod}"
        findViewById<TextView>(R.id.workingStatusText).text = "Status: ${booking.status.uppercase()}"

        // Generate and display QR Code
        val qrBitmap = generateQRCode(booking.bookingId)
        findViewById<ImageView>(R.id.qrCodeImage).setImageBitmap(qrBitmap)
    }

    private fun generateQRCode(content: String): Bitmap {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                200,  // Ukuran QR code lebih besar untuk detail view
                200
            )

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }

            return bitmap
        } catch (e: Exception) {
            // Jika gagal generate QR code, return bitmap kosong
            return Bitmap.createBitmap(200, 200, Bitmap.Config.RGB_565)
        }
    }

    private fun checkBookingStatus(bookingId: String, callback: (String) -> Unit) {
        db.collection("bookings").document(bookingId)
            .get()
            .addOnSuccessListener { document ->
                val status = document.getString("status") ?: "pending"
                callback(status)
            }
            .addOnFailureListener {
                callback("pending")
            }
    }

    private fun updateBookingStatus(bookingId: String, newStatus: String) {
        db.collection("bookings")
            .document(bookingId)
            .update("status", newStatus)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking marked as finished", Toast.LENGTH_SHORT).show()
                //meload invoice kembali dari perubahan status
                loadInvoiceDetails(bookingId)
                //button dibuat hide ketika sudah mengklik 'mark as finished'
                findViewById<Button>(R.id.markFinishedButton).visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}