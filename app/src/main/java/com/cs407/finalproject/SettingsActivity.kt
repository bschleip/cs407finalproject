package com.cs407.finalproject

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SettingsActivity : AppCompatActivity() {

    private lateinit var backSettingsBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        backSettingsBtn = findViewById(R.id.settings_back_button)

        val settingsRecyclerView: RecyclerView = findViewById(R.id.settings_recycler_view)
        settingsRecyclerView.layoutManager = LinearLayoutManager(this)
        settingsRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        val settingsList = listOf("Version: 1.0.0", "Terms and Conditions", "Logout", "View Friends")
        settingsRecyclerView.adapter = createSettingsAdapter(settingsList)

        backSettingsBtn.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }
    }

    private fun createSettingsAdapter(settings: List<String>): RecyclerView.Adapter<RecyclerView.ViewHolder> {
        return object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                return createSettingsViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.item_setting, parent, false)
                )
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                bindSettingsViewHolder(holder, settings[position])
            }

            override fun getItemCount(): Int = settings.size
        }
    }

    private fun createSettingsViewHolder(view: View): RecyclerView.ViewHolder {
        return object : RecyclerView.ViewHolder(view) {
            val settingText: TextView = view.findViewById(R.id.setting_text)
        }
    }

    private fun bindSettingsViewHolder(holder: RecyclerView.ViewHolder, setting: String) {
        val settingText = holder.itemView.findViewById<TextView>(R.id.setting_text)
        settingText.text = setting

        holder.itemView.setOnClickListener {
            when (setting) {
                "Terms and Conditions" -> navigateToTerms()
                "Logout" -> logout()
                "View Friends" -> navigateToFriends() // New case for "View Friends"
            }
        }
    }

    private fun navigateToTerms() {
        val intent = Intent(this, TermsAndConditionsActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToFriends() {
        val intent = Intent(this, FriendsActivity::class.java)
        startActivity(intent)
    }


    private fun logout() {
        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        // Remove user session
        with(sharedPref.edit()) {
            remove("LOGGED_IN_USER_ID")
            apply()
        }

        // Redirect to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
