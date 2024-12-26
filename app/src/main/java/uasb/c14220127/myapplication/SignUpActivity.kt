package uasb.c14220127.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var nameField: EditText
    private lateinit var usernameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var phoneField: EditText
    private lateinit var registerButton: Button
    private lateinit var backButton: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Link UI elements
        nameField = findViewById(R.id.fullName)
        usernameField = findViewById(R.id.username)
        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        phoneField = findViewById(R.id.phoneField)
        registerButton = findViewById(R.id.signupButton)
        backButton = findViewById(R.id.loginText)

        registerButton.setOnClickListener { v: View? ->
            val name = nameField.text.toString().trim { it <= ' ' }
            val username = usernameField.text.toString().trim { it <= ' ' }
            val email = emailField.text.toString().trim { it <= ' ' }
            val password = passwordField.text.toString().trim { it <= ' ' }
            val phone = phoneField.text.toString().trim { it <= ' ' }
            val address = "" // Set address to empty string

            if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty()) {
                Toast.makeText(
                    this@SignUpActivity,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Register user in Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser!!.uid

                        // Save user details to Firestore
                        val user = User(name, email, phone, username, password, address)
                        firestore.collection("users").document(userId).set(user)
                            .addOnCompleteListener { dbTask: Task<Void?> ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Registration Successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(
                                        Intent(
                                            this@SignUpActivity,
                                            MainActivity::class.java
                                        )
                                    )
                                    finish()
                                } else {
                                    Log.e("SignUpActivity", "Database Error: ${dbTask.exception?.message}")
                                    Toast.makeText(
                                        this@SignUpActivity,
                                        "Database Error: ${dbTask.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        Log.e("SignUpActivity", "Registration Failed: ${task.exception?.message}")
                        Toast.makeText(
                            this@SignUpActivity,
                            "Registration Failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("SignUpActivity", "Registration Exception: ${exception.message}")
                    Toast.makeText(
                        this@SignUpActivity,
                        "Registration Exception: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, MainActivity::class.java))
            finish()
        }
    }
}
