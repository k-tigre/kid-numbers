package by.tigre.numbers.presentation.root

import android.os.Parcelable
import by.tigre.numbers.presentation.additional.RootAdditionalComponent
import by.tigre.numbers.presentation.menu.MenuComponent
import by.tigre.numbers.presentation.menu.MenuNavigator
import by.tigre.numbers.presentation.multiplication.component.RootMultiplicationComponent
import by.tigre.tools.presentation.base.BaseComponentContext
import by.tigre.tools.presentation.base.appChildStack
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.value.Value
import kotlinx.parcelize.Parcelize

interface RootComponent {

    val pages: Value<ChildStack<*, PageChild>>

    sealed interface PageChild {
        class Menu(val component: MenuComponent) : PageChild
        class Multiplication(val component: RootMultiplicationComponent) : PageChild
        class Additional(val component: RootAdditionalComponent) : PageChild
    }

    class Impl(
        context: BaseComponentContext,
    ) : RootComponent, BaseComponentContext by context {

        private val navigator: MenuNavigator = object : MenuNavigator {
            override fun showMultiplicationScreen() {
                pagesNavigation.bringToFront(PagesConfig.Multiplication)
            }

            override fun showAdditionScreen() {
                pagesNavigation.bringToFront(PagesConfig.Additional)
            }
        }

        private val pagesNavigation = StackNavigation<PagesConfig>()

        override val pages: Value<ChildStack<*, PageChild>> =
            appChildStack(
                source = pagesNavigation,
                initialStack = { listOf(PagesConfig.Menu) },
                key = "pages",
                handleBackButton = true
            ) { config, componentContext ->
                when (config) {
                    PagesConfig.Menu -> PageChild.Menu(
                        MenuComponent.Impl(componentContext, navigator = navigator)
                    )

                    PagesConfig.Multiplication -> PageChild.Multiplication(
                        RootMultiplicationComponent.Impl(componentContext)
                    )

                    PagesConfig.Additional -> PageChild.Additional(
                        RootAdditionalComponent.Impl(componentContext)
                    )
                }
            }

        private sealed interface PagesConfig : Parcelable {
            @Parcelize
            data object Menu : PagesConfig

            @Parcelize
            data object Multiplication : PagesConfig

            @Parcelize
            data object Additional : PagesConfig
        }

    }
}
