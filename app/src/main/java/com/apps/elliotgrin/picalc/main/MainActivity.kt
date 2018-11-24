package com.apps.elliotgrin.picalc.main

import android.arch.lifecycle.Observer
import com.apps.elliotgrin.picalc.R
import com.apps.elliotgrin.picalc.base.BaseActivity
import com.apps.elliotgrin.picalc.extensions.setVisible
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : BaseActivity() {

    override val layoutId: Int
        get() = R.layout.activity_main

    private val mainViewModel: MainViewModel by viewModel()

    override fun created() {
        mainViewModel.piNumberData.observe(this, Observer { pi ->
            if (pi == null) output_text_view.setVisible(false)
            else output_text_view.setVisible(true); output_text_view.text = pi
        })
        run_fab.setOnClickListener { mainViewModel.run() }
    }
}
