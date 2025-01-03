package uasb.c14220127.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
//import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var fullNameText: TextView

    private lateinit var phoneText: TextView
    private lateinit var addressText: TextView
    private lateinit var usernameText: TextView

    private lateinit var fullNameEdit: EditText

    private lateinit var phoneEdit: EditText
    private lateinit var addressEdit: EditText

    private lateinit var editProfileButton: Button
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileImage: ShapeableImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupBottomNavigation()
        fetchAndDisplayUserData()
        setupButtonListeners()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profileImage)

        // TextViews
        fullNameText = findViewById(R.id.fullNameText)

        phoneText = findViewById(R.id.phoneText)
        addressText = findViewById(R.id.addressText)


        // EditTexts
        fullNameEdit = findViewById(R.id.fullNameEdit)

        phoneEdit = findViewById(R.id.phoneEdit)
        addressEdit = findViewById(R.id.addressEdit)

        // Buttons
        editProfileButton = findViewById(R.id.editProfileButton)
        saveButton = findViewById(R.id.saveButton)
        logoutButton = findViewById(R.id.logoutButton)

        setEditMode(false)
    }

    private fun setupBottomNavigation() {
        val explorerLayout = findViewById<LinearLayout>(R.id.explorerLayout)
        val transactionLayout = findViewById<LinearLayout>(R.id.transactionLayout)
        val profileLayout = findViewById<LinearLayout>(R.id.profileLayout)

        explorerLayout.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
            finish()
        }

        transactionLayout.setOnClickListener {
            startActivity(Intent(this, InvoiceActivity::class.java))
            finish()
        }
    }

    private fun fetchAndDisplayUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            navigateToLogin()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    //tampilkan user data
                    fullNameText.text = document.getString("name") ?: ""
                    phoneText.text = document.getString("phone") ?: ""
                    addressText.text = document.getString("address") ?: ""

                    //set edit text
                    fullNameEdit.setText(fullNameText.text)
                    phoneEdit.setText(phoneText.text)
                    addressEdit.setText(addressText.text)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserData() {
        val currentUser = auth.currentUser ?: return

        //data yang ingin diperbarui
        val updatedData = hashMapOf(
            "name" to fullNameEdit.text.toString(),
            "phone" to phoneEdit.text.toString(),
            "address" to addressEdit.text.toString()
        )

        //perbarui data di Firestore
        db.collection("users")
            .document(currentUser.uid)
            .update(updatedData as Map<String, Any>)
            .addOnSuccessListener {
                setEditMode(false)
                fetchAndDisplayUserData()
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating profile in Firestore: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun setEditMode(editing: Boolean) {
        fullNameText.visibility = if (editing) View.GONE else View.VISIBLE

        phoneText.visibility = if (editing) View.GONE else View.VISIBLE
        addressText.visibility = if (editing) View.GONE else View.VISIBLE

        fullNameEdit.visibility = if (editing) View.VISIBLE else View.GONE

        phoneEdit.visibility = if (editing) View.VISIBLE else View.GONE
        addressEdit.visibility = if (editing) View.VISIBLE else View.GONE

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

        logoutButton.setOnClickListener {
            auth.signOut()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivityLogin::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}