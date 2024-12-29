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

class InvoiceAdapter(
    private val invoices: List<InvoiceData>,
    private val onItemClick: (InvoiceData) -> Unit
) : RecyclerView.Adapter<InvoiceAdapter.InvoiceViewHolder>() {

    class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Sesuai dengan ID di viewholder_invoice.xml
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

        // Generate QR code untuk ikon transaksi
        val qrCode = generateQRCode(invoice.bookingId)
        holder.transactionIcon.setImageBitmap(qrCode)

        // Set judul transaksi (menggunakan nama worker)
        holder.transactionTitle.text = "Booking with ${invoice.workerName}"

        // Set tanggal transaksi
        holder.transactionDate.text = invoice.date

        // Set jumlah pembayaran
        holder.transactionAmount.text = "Amount: Rp ${invoice.amount}"

        // Handler untuk tombol view detail
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
                96,  // Sesuaikan dengan ukuran transactionIcon di XML
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
            // Jika gagal generate QR code, return bitmap kosong
            return Bitmap.createBitmap(96, 96, Bitmap.Config.RGB_565)
        }
    }
}