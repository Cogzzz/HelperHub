package uasb.c14220127.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditWorker : AppCompatActivity() {
    private lateinit var editWorkerName: EditText
    private lateinit var editWorkerAge: EditText
    private lateinit var editWorkerSpecialization : EditText
    private lateinit var buttonSave: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.worker_edit)

        //inisialisasi firestore dan auth dari firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        initializeViews()
        loadWorkerData()
        setupButtonListeners()
    }

    private fun initializeViews() {
        editWorkerName = findViewById(R.id.etName)
        editWorkerAge = findViewById(R.id.etAge)
        editWorkerSpecialization = findViewById(R.id.etSpecialization)
        buttonSave = findViewById(R.id.btnSave)
    }

    private fun setupButtonListeners() {
        buttonSave.setOnClickListener {
            updateWorkerData()
        }
    }

    private fun loadWorkerData() {
        val workerId = intent.getStringExtra("worker_id")
        if (!workerId.isNullOrEmpty()) {
            db.collection("workers").document(workerId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        editWorkerName.setText(document.getString("name"))
                        val age = when (val ageValue = document.get("age")) {
                            is String -> ageValue
                            is Long -> ageValue.toString()
                            else -> ""
                        }
                        editWorkerAge.setText(age)
                        editWorkerSpecialization.setText(document.getString("specialization"))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error loading data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateWorkerData() {
        val workerId = intent.getStringExtra("worker_id") // Dapatkan workerId dari Intent
        if (workerId.isNullOrEmpty()) {
            // jika workerId tidak ada, kembali ke halaman sebelumnya
            Toast.makeText(this, "Worker ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val workerName = editWorkerName.text.toString().trim()
        val workerAgeStr = editWorkerAge.text.toString().trim()
        val workerSpecialization = editWorkerSpecialization.text.toString().trim()

        //validasi input
        if (workerName.isEmpty()) {
            editWorkerName.error = "Nama tidak boleh kosong"
            return
        }

        if (workerAgeStr.isEmpty()) {
            editWorkerAge.error = "Umur tidak boleh kosong"
            return
        }

        if (workerSpecialization.isEmpty()) {
            editWorkerSpecialization.error = "Specialization tidak boleh kosong"
            return
        }

        val workerAge = workerAgeStr.toIntOrNull()
        if (workerAge == null) {
            editWorkerAge.error = "Umur harus berupa angka"
            return
        }

        // update data ke firestore firebase
        val workerData = mapOf(
            "name" to workerName,
            "age" to workerAgeStr,
            "specialization" to workerSpecialization
        )

        db.collection("workers")
            .document(workerId)
            .update(workerData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data berhasil diperbarui", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
