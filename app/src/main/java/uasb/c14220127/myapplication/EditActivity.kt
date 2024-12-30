package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditActivity : AppCompatActivity() {
    private lateinit var workerNameTxt: TextView
    private lateinit var specTxt: TextView
    private lateinit var workerNameEdit: EditText
    private lateinit var specEdit: EditText
    private lateinit var editProfileButton: Button
    private lateinit var saveButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.worker_edit)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
//        fetchAndDisplayUserData()
    }

    private fun initializeViews() {

        // TextViews
        workerNameTxt = findViewById(R.id.workerNameText)
        specTxt = findViewById(R.id.specText)


        // EditTexts
        workerNameEdit = findViewById(R.id.fullNameEdit)
        specEdit = findViewById(R.id.specEdit)

        // Buttons
        editProfileButton = findViewById(R.id.btnEditWorker)
        saveButton = findViewById(R.id.saveEdit)

        setEditMode(false)
    }



//    private fun fetchAndDisplayUserData() {
//        val currentUser = auth.currentUser
//        if (currentUser == null) {
//            navigateToViewHolder()
//            return
//        }
//
//        db.collection("workers")
//            .document(currentUser.uid)
//            .get()
//            .addOnSuccessListener { document ->
//                if (document != null && document.exists()) {
//                    // Display user data
//                    workerNameTxt.text = document.getString("name") ?: ""
//                    specTxt.text = document.getString("specialization") ?: ""
//
//                    //set edit
//                    workerNameEdit.setText(workerNameTxt.text)
//                    specEdit.setText(specTxt.text)
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }



    private fun updateUserData() {
        val currentWorker = auth.currentUser ?: return

        // Data yang akan diperbarui
        val updatedData = hashMapOf(
            "name" to workerNameEdit.text.toString(),
            "specialization" to specEdit.text.toString(),
        )

        // Perbarui data di Firestore
        db.collection("workers")
            .document(currentWorker.uid)
            .update(updatedData as Map<String, Any>)
            .addOnSuccessListener {
                setEditMode(false)
//                fetchAndDisplayUserData()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating profile in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun setEditMode(editing: Boolean) {
        workerNameTxt.visibility = if (editing) View.GONE else View.VISIBLE
        specTxt.visibility = if (editing) View.GONE else View.VISIBLE

        //edit
        workerNameEdit.visibility = if (editing) View.VISIBLE else View.GONE
        specEdit.visibility = if (editing) View.VISIBLE else View.GONE

        editProfileButton.visibility = if (editing) View.GONE else View.VISIBLE
        saveButton.visibility = if (editing) View.VISIBLE else View.GONE
    }

    private fun setupButtonListeners() {
        editProfileButton.setOnClickListener {
            setEditMode(true)
        }

        saveButton.setOnClickListener {
            updateUserData()
        }
    }

    private fun navigateToViewHolder() {
        val intent = Intent(this, MainActivityLogin::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}