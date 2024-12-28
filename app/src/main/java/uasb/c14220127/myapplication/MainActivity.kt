package uasb.c14220127.myapplication

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    private lateinit var spinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.payment)

        // Menyesuaikan padding untuk spinner
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paymentpinner)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        spinner = findViewById(R.id.payment_method_spinner)

        // Data untuk Spinner
        val paymentMethods = listOf(
            SpinnerItem(R.drawable.icon_bca, "BCA Virtual Account"),
            SpinnerItem(R.drawable.mandiri, "Mandiri Virtual Account"),
            SpinnerItem(R.drawable.gopay, "Gopay"),
            SpinnerItem(R.drawable.ovo, "OVO"),
            SpinnerItem(R.drawable.creditcard, "Credit Card"),
            SpinnerItem(R.drawable.cash, "Cash on Delivery")
        )

        // Pasang Custom Adapter ke Spinner
        val spinnerAdapter = SpinnerAdapter(this, paymentMethods)
        spinner.adapter = spinnerAdapter

        // Event ketika item dipilih
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = paymentMethods[position].text
                Toast.makeText(
                    this@MainActivity,
                    "Pembayaran Menggunakan: $selectedItem",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak ada aksi jika tidak ada item dipilih
            }
        }
        val jobRecyclerView: RecyclerView = findViewById(R.id.recyclerView)

        // Ambil data dari Intent
        val selectedJobs = intent.getStringArrayListExtra("selectedJobs") ?: arrayListOf()

        // Setup RecyclerView
        val adapter = MyAdapter(selectedJobs)
        jobRecyclerView.layoutManager = LinearLayoutManager(this)
        jobRecyclerView.adapter = adapter
    }
}
