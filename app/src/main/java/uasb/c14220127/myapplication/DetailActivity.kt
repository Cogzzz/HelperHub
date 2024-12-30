package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import uasb.c14220127.myapplication.databinding.WorkerDetailBinding

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: WorkerDetailBinding
    private lateinit var firestore: FirebaseFirestore
    private var workerId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WorkerDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Get worker ID from intent
        workerId = intent.getStringExtra("worker_id") ?: ""

        if (workerId.isNotEmpty()) {
            fetchWorkerData()
        } else {
            Toast.makeText(this, "Invalid worker ID", Toast.LENGTH_SHORT).show()
            finish()
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.imageView5.setOnClickListener {
            finish()
        }

        binding.btnBooking.setOnClickListener {
            val intent = Intent(this, InputJobdesc::class.java).apply {
                putExtra("worker_id", workerId)
            }
            startActivity(intent)
        }
    }

    private fun fetchWorkerData() {
        firestore.collection("workers").document(workerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    document.toObject(Worker::class.java)?.let { worker ->
                        updateUI(worker)
                    }
                } else {
                    Toast.makeText(this, "Worker not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DetailActivity", "Error fetching worker details", exception)
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateUI(worker: Worker) {
        with(binding) {
            // Profile Image
            Glide.with(this@DetailActivity)
                .load(worker.imageUrl)
                .placeholder(R.drawable.workers)
                .into(imageView4)

            // Basic Information
            tvNamaPembantu.text = worker.name
            tvAlamatPembantu.text = worker.address
            ratingBar2.rating = worker.rating ?: 0f


            // Personal Information
            tvAge.text = worker.age.toString() // Age
            tvExperience.text = worker.experience // Experience

            // Skills Section
            // You might want to format this text with proper styling
            val skillsText = TextView(this@DetailActivity).apply {
                tvSkill.text = worker.skills
                setTextColor(getColor(R.color.black))
            }

            // About Me Section
            val aboutMeText = TextView(this@DetailActivity).apply {
                tvAboutme.text = worker.aboutMe
                setTextColor(getColor(R.color.black))
            }

            // Work Experience Section
            // Work Experience Section
            tvWorkPeriod.text = worker.workPeriod ?: "No Period Provided"
            tvWorkExp.text = "${worker.workPosition ?: "N/A"} - ${worker.workEmployer ?: "N/A"}"
            tvDuties.text = "Duties: ${worker.workDuties ?: "N/A"}"


        }
    }
}