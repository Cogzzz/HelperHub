package uasb.c14220127.myapplication

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import android.graphics.Color
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class InvoiceAdapter(
    private val invoices: List<InvoiceData>,
    private val onItemClick: (InvoiceData) -> Unit
) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val transactionIcon: ImageView = itemView.findViewById(R.id.transactionIcon)
        val transactionTitle: TextView = itemView.findViewById(R.id.transactionTitle)
        val transactionDate: TextView = itemView.findViewById(R.id.transactionDate)
        val transactionAmount: TextView = itemView.findViewById(R.id.transactionAmount)
        val viewDetailButton: AppCompatButton = itemView.findViewById(R.id.viewDetailButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.viewholder_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        Log.d("InvoiceAdapter", "Binding invoice: ${invoice.bookingId}")

        FirebaseFirestore.getInstance()
            .collection("bookings")
            .document(invoice.bookingId)
            .get()
            .addOnSuccessListener { document ->
                val booking = document.toObject(BookingData::class.java)
                if (booking != null) {
                    //format tanggal menggunakan scheduledDateTime
                    val dateFormat = SimpleDateFormat("EEEE, dd/MM/yyyy - HH:mm", Locale.getDefault())
                    val formattedDate = dateFormat.format(booking.scheduledDateTime)
                    holder.transactionDate.text = formattedDate

                    //ambil data dari firebase
                    if (booking.workerId != null) {
                        FirebaseFirestore.getInstance()
                            .collection("workers")
                            .document(booking.workerId!!)
                            .get()
                            .addOnSuccessListener { workerDoc ->
                                val worker = workerDoc.toObject(Worker::class.java)
                                if (worker?.imageUrl != null) {
                                    // Memuat gambar pekerja menggunakan Glide
                                    Glide.with(holder.transactionIcon.context)
                                        .load(worker.imageUrl)
                                        .into(holder.transactionIcon)
                                } else {
                                    holder.transactionIcon.setImageResource(R.drawable.baseline_person_24) // Gambar default
                                }
                            }
                            .addOnFailureListener {
                                holder.transactionIcon.setImageResource(R.drawable.baseline_person_24) // Gambar default jika gagal mengambil data
                            }
                    }
                }
            }
            .addOnFailureListener {
                //kembali format tanggal biasa jika gagal mengambil booking
                holder.transactionDate.text = invoice.date
            }

        holder.transactionTitle.text = "Booking with ${invoice.workerName}"
        holder.transactionAmount.text = "Amount: Rp ${invoice.amount}"

        // generate QR code untuk ikon transaksi
        val qrCode = generateQRCode(invoice.bookingId)
        holder.transactionIcon.setImageBitmap(qrCode)

        //handler untuk tombol view detail
        holder.viewDetailButton.setOnClickListener {
            onItemClick(invoice)
        }
    }

    override fun getItemCount() = invoices.size

    private fun generateQRCode(content: String): Bitmap {
        try {
            val multiFormatWriter = MultiFormatWriter()
            val bitMatrix = multiFormatWriter.encode(
                content,
                BarcodeFormat.QR_CODE,
                96,
                96
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
            //jika gagal generate QR code, return bitmap kosong
            return Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565)
        }
    }
}