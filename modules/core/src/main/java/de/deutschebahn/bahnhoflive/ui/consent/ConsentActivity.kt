package de.deutschebahn.bahnhoflive.ui.consent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.deutschebahn.bahnhoflive.R

class ConsentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_consent)
    }

    companion object {
        fun createIntent(context: Context) = Intent(context, ConsentActivity::class.java)
    }
}