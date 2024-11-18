package com.cs407.finalproject

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

        val settingsList = listOf("Setting 1", "Setting 2", "Setting 3")
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
                    LayoutInflater.from(parent.context).inflate(R.layout.settings_item, parent, false)
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
    }
}
