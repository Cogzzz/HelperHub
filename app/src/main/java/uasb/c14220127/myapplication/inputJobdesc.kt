package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.flexbox.FlexboxLayout

class inputJobdesc : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_jobdesk)

        val jobdescList = listOf("Menyapu", "Mengepel", "Mencuci", "Menyetrika", "Membersihkan Kamar")
        val selectedJobs = mutableListOf<String>()

        val flowLayout: FlexboxLayout = findViewById(R.id.flowLayout)
        val proceedButton: Button = findViewById(R.id.proceedButton)

        // Create buttons dynamically
        jobdescList.forEach { job ->
            val button = Button(this).apply {
                text = job
                setBackgroundResource(R.drawable.button_default_bg)
                setTextColor(resources.getColor(R.color.black, theme))
                textSize = 16f
                maxLines = 2
                setPadding(20, 10, 20, 10)
                layoutParams = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(8, 8, 8, 8)
                }
                setOnClickListener {
                    if (selectedJobs.contains(job)) {
                        selectedJobs.remove(job)
                        setBackgroundResource(R.drawable.button_default_bg)
                    } else {
                        selectedJobs.add(job)
                        setBackgroundResource(R.drawable.button_selected_bg)
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