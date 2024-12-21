package by.tigre.numbers

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import by.tigre.numbers.presentation.root.RootComponent
import by.tigre.numbers.presentation.root.RootView
import by.tigre.tools.presentation.base.BaseComponentContextImpl
import by.tigre.tools.tools.platform.compose.AppTheme
import com.arkivanov.decompose.defaultComponentContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val graph = (application as App).graph
        val root = RootComponent.Impl(
            context = BaseComponentContextImpl(defaultComponentContext()),
            gameDependencies = graph,
            analytics = graph.screenAnalytics
        )
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Surface(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                    RootView(
                        component = root,
                    ).Draw(Modifier.safeDrawingPadding())
                }
            }
        }
    }
}
