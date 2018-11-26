package com.apps.elliotgrin.picalc.main

import android.annotation.TargetApi
import android.arch.lifecycle.Observer
import android.os.AsyncTask
import android.view.View
import com.apps.elliotgrin.chudnovsky.calculatePi
import com.apps.elliotgrin.picalc.R
import com.apps.elliotgrin.picalc.base.BaseActivity
import com.apps.elliotgrin.picalc.extensions.setVisible
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    private val mainViewModel: MainViewModel by viewModel()

    override val layoutId: Int
        get() = R.layout.activity_main

    override fun created() {

        mainViewModel.piNumberData.observe(this, Observer { pi ->
            if (pi == null) output_text_view.setVisible(false)
            else output_text_view.setVisible(true); output_text_view.text = pi
        })
        run_fab.setOnClickListener { mainViewModel.run() }

//        bindFab()

        /*run_fab.setOnClickListener {
            val s = input_edit_text.text.toString()
            try {
                val k = s.toLong()
                val task = ComputationTask(k)
                task.execute()
            } catch (e: Exception) {
                output_text_view.text = ""
                output_text_view.setVisible(false)
            }
        }*/
    }

    private fun showLoading(show: Boolean) {
        progress_bar.setVisible(show)
        output_text_view.setVisible(!show)
    }

    @TargetApi(23)
    private fun bindFab() {
        scroll_view.setOnScrollChangeListener { _, _, y, _, oldY ->
            if (y > oldY && run_fab.visibility == View.VISIBLE) run_fab.hide()
            else if (y < oldY && run_fab.visibility != View.VISIBLE) run_fab.show()
        }
    }

    // region AsyncTask
    inner class ComputationTask(val k: Long) : AsyncTask<Unit, Unit, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
            showLoading(true)
        }

        override fun doInBackground(vararg p0: Unit?): String {
            return "${calculatePi(k)}"
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            output_text_view.text = result
            showLoading(false)
        }
    }
    // endregion
}
