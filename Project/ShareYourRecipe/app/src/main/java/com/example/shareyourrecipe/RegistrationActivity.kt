package com.example.shareyourrecipe

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.shareyourrecipe.databinding.ActivityRegistrationBinding
import com.example.shareyourrecipe.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase

class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val firstName = binding.etFirstName.text.toString()
            val lastName = binding.etLastName.text.toString()
            val country = binding.etCountry.text.toString()
            val password = binding.etPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val firebaseUser = auth.currentUser
                        val userId = firebaseUser?.uid
                        if (userId != null) {
                            val userRef = FirebaseDatabase.getInstance().reference.child("users")
                                .child(userId)
                            val userData = User(firstName, lastName, country)
                            userRef.setValue(userData)
                                .addOnSuccessListener {
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, exception.message, Toast.LENGTH_LONG).show()

                                }
                        }
                    } else {
                        try {
                            throw task.exception!!
                        } catch (emailExists: FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                this,
                                "მომხმარებელი ამ ელფოსტით უკვე არსებობს",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (e: Exception) {
                            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }

    }
}
