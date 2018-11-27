package com.apps.elliotgrin.picalc.main

import android.annotation.TargetApi
import android.arch.lifecycle.Observer
import android.view.View
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
}
