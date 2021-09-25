package com.semisonfire.cloudgallery.ui.main.ui.state

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.semisonfire.cloudgallery.R
import com.semisonfire.cloudgallery.core.ui.state.State
import com.semisonfire.cloudgallery.core.ui.state.StateViewDelegate
import com.semisonfire.cloudgallery.core.ui.state.strategy.EnterActionStrategy
import com.semisonfire.cloudgallery.di.module.OAUTH_URL
import com.semisonfire.cloudgallery.utils.string
import javax.inject.Inject
import javax.inject.Singleton

enum class MainStateView {
    AUTH,
    LOADER,
    CONTENT,
    EMPTY
}

@Singleton
class StateViewController @Inject constructor() {

    private lateinit var stateViewDelegate: StateViewDelegate<MainStateView>

    fun bindStateDelegate(view: View) {
        val context = view.context

        stateViewDelegate = StateViewDelegate(
            State(MainStateView.CONTENT, view.findViewById<View>(R.id.fragment_container)),
            State(MainStateView.LOADER, view.findViewById<View>(R.id.progress_loader))
        )

        val informView = view.findViewById<View>(R.id.include_inform)
        val informImage = informView.findViewById<ImageView>(R.id.image_inform)
        val informTitle = informView.findViewById<TextView>(R.id.text_inform_title)
        val informBody = informView.findViewById<TextView>(R.id.text_inform_body)
        val informButton = informView.findViewById<Button>(R.id.btn_inform)

        informView?.let {

            stateViewDelegate.addState(
                State(
                    MainStateView.AUTH,
                    informView,
                    EnterActionStrategy {
                        informTitle.text = context.string(R.string.msg_yandex_start)
                        informBody.text = context.string(R.string.msg_yandex_account)
                        informButton.text = context.string(R.string.action_yandex_link_account)

                        informImage.setImageResource(R.drawable.ic_cloud_off)

                        informButton.visibility = View.VISIBLE
                        informButton.setOnClickListener {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OAUTH_URL))
                            context.startActivity(intent)
                        }
                    }
                ),
                State(
                    MainStateView.EMPTY,
                    informView,
                    EnterActionStrategy {
                        informTitle.text = context.string(R.string.msg_yandex_failed_retrieve)
                        informBody.text = context.string(R.string.action_yandex_check_connection)

                        informImage.setImageResource(R.drawable.ic_yandex_disk)
                        informButton.visibility = View.GONE
                    }
                )
            )
        }
    }

    fun updateStateView(state: MainStateView) {

        if (!::stateViewDelegate.isInitialized) return

        stateViewDelegate.currentState = state
    }

}