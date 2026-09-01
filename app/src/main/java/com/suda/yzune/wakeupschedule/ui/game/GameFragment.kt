package com.suda.yzune.wakeupschedule.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.suda.yzune.wakeupschedule.ui.theme.WakeupScheduleTheme

class GameFragment : Fragment() {

    private val viewModel: GameViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WakeupScheduleTheme {
                    GameScreen(
                        viewModel = viewModel,
                        onGameClick = { gameName ->
                            Toast.makeText(
                                requireContext(),
                                "🎮 $gameName — 即将上线！",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }
        }
    }
}
