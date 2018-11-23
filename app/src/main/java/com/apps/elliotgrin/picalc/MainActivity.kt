package com.apps.elliotgrin.picalc

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.apps.elliotgrin.chudnovsky.ChudnovskyAlgorithm
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input_edit_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                try {
                    val k = p0.toString().toLong()
                    runComputationThread(k)
                } catch (e: Exception) {
                    output_text_view.text = null
                    Toast.makeText(this@MainActivity, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun runComputationThread(k: Long) {
        Thread {
            val pi = ChudnovskyAlgorithm.calculatePi(k)
            runOnUiThread { output_text_view.text = "$pi" }
        }.run()
    }
}
