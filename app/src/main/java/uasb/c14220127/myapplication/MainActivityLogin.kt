package uasb.c14220127.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivityLogin : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var passwordField: EditText
    private lateinit var loginButton: Button
    private lateinit var registerText: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        FirebaseApp.initializeApp(this) // Initialize Firebase
        setContentView(R.layout.login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Link UI elements
        usernameField = findViewById(R.id.username)
        passwordField = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        registerText = findViewById(R.id.signupText)

        // Login button listener
        loginButton.setOnClickListener { v: View? ->
            val username = usernameField.text.toString().trim { it <= ' ' }
            val password = passwordField.text.toString().trim { it <= ' ' }

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    this@MainActivityLogin,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Fetch email from Firestore using username
            firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val email = documents.documents[0].getString("email") ?: ""
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task: Task<AuthResult?> ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    if (user != null) {
                                        Log.d("MainActivity", "Login successful! User: ${user.email}")
                                        val intent = Intent(this@MainActivityLogin, HomePageActivity::class.java)
                                        startActivity(intent)
                                        finish()
                                    }
                                } else {
                                    Log.e("MainActivity", "Login failed: ${task.exception?.message}")
                                    Toast.makeText(
                                        this@MainActivityLogin,
                                        "Login failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("MainActivity", "Login Exception: ${exception.message}")
                                Toast.makeText(
                                    this@MainActivityLogin,
                                    "Login Exception: ${exception.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        Toast.makeText(
                            this@MainActivityLogin,
                            "Username not found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("MainActivity", "Error fetching user: ${exception.message}")
                    Toast.makeText(
                        this@MainActivityLogin,
                        "Error fetching user: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        // Register text listener
        registerText.setOnClickListener {
            val intent = Intent(this@MainActivityLogin, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
