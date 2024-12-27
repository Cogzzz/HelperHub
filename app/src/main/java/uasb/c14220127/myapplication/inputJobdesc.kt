package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class inputJobdesc : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_jobdesk)

        val jobdescList = listOf("Menyapu", "Mengepel", "Mencuci", "Menyetrika", "Membersihkan Kamar")
        val selectedJobs = mutableListOf<String>()

        val flowLayout: LinearLayout = findViewById(R.id.flowLayout)
        val proceedButton: Button = findViewById(R.id.proceedButton)

        // Create buttons dynamically
        jobdescList.forEach { job ->
            val button = Button(this).apply {
                text = job
                setOnClickListener {
                    if (selectedJobs.contains(job)) {
                        selectedJobs.remove(job)
                        setBackgroundResource(android.R.drawable.btn_default)
                    } else {
                        selectedJobs.add(job)
                        setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
                    }
                }
            }
            flowLayout.addView(button)
        }

        proceedButton.setOnClickListener {
            println("Selected Jobs: $selectedJobs") // Debugging untuk memastikan data terisi
                val intent = Intent(this, MainActivity::class.java).apply {
                putStringArrayListExtra("selectedJobs", ArrayList(selectedJobs)) // Pastikan selectedJobs terisi
            }
            startActivity(intent)
        }
    }
}