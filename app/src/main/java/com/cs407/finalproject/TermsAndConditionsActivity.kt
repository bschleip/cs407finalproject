package com.cs407.finalproject

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TermsAndConditionsActivity : AppCompatActivity() {

    private lateinit var termsTextView: TextView
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        termsTextView = findViewById(R.id.termsTextView)
        backButton = findViewById(R.id.backButton)

        termsTextView.text = """
            Welcome to our application! 
            
            By using this app, you agree to the following terms and conditions:
            
            1. Use of the app is at your own risk.
            2. All data collected will be handled according to our privacy policy.
            3. Misuse of the app may lead to account termination.
            
            Thank you for using our app!
        """.trimIndent()

        backButton.setOnClickListener {
            finish()
        }
    }
}
