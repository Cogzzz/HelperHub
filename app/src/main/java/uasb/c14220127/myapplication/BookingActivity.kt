package uasb.c14220127.myapplication

import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import uasb.c14220127.myapplication.databinding.Coba3Binding

class BookingActivity : AppCompatActivity() {

    private lateinit var binding: Coba3Binding
    private lateinit var paymentMethodSpinner: Spinner
    private lateinit var bookButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = Coba3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Views
        paymentMethodSpinner = binding.paymentMethodSpinner
        bookButton = binding.bookButton

        // Setup spinner metode pembayaran
        setupPaymentMethodSpinner()

        // Setup button "Book"
        bookButton.setOnClickListener {
            handleBooking()
        }
    }

    private fun setupPaymentMethodSpinner() {
        // Daftar metode pembayaran
        val paymentMethods = listOf("Cash", "Credit Card", "Bank Transfer", "E-Wallet")

        // Adapter untuk spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paymentMethods).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        paymentMethodSpinner.adapter = adapter

        // Listener untuk spinner (opsional)
        paymentMethodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedMethod = paymentMethods[position]
                Toast.makeText(this@BookingActivity, "Selected: $selectedMethod", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak ada aksi
            }
        }
    }

    private fun handleBooking() {
        // Ambil metode pembayaran yang dipilih
        val selectedPaymentMethod = paymentMethodSpinner.selectedItem.toString()

        // Tampilkan toast atau lanjutkan dengan logika pemesanan
        Toast.makeText(this, "Booking confirmed with $selectedPaymentMethod", Toast.LENGTH_SHORT).show()

        // TODO: Tambahkan logika untuk menyimpan data pemesanan ke database (Firestore atau lainnya)
    }
}
