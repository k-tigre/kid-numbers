package by.tigre.tools.presentation.base

import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.router.slot.SlotNavigationSource
import com.arkivanov.decompose.router.slot.childSlot
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigationSource
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.serialization.KSerializer

fun <C : Any, T : Any> BaseComponentContext.appChildStack(
    source: StackNavigationSource<C>,
    initialStack: () -> List<C>,
    serializer: KSerializer<C>? = null,
    key: String = "DefaultStack",
    handleBackButton: Boolean = false,
    childFactory: (configuration: C, BaseComponentContext) -> T
): Value<ChildStack<C, T>> =
    childStack(
        source = source,
        initialStack = initialStack,
        serializer = serializer,
        key = key,
        handleBackButton = handleBackButton
    ) { configuration, componentContext ->
        childFactory(
            configuration,
            BaseComponentContextImpl(
                componentContext = componentContext
            )
        )
    }

fun <C : Any, T : Any> BaseComponentContext.appChildSlot(
    source: SlotNavigationSource<C>,
    initialConfiguration: () -> C? = { null },
    serializer: KSerializer<C>? = null,
    key: String = "DefaultChildSlot",
    handleBackButton: Boolean = false,
    childFactory: (configuration: C, BaseComponentContext) -> T
): Value<ChildSlot<C, T>> =
    childSlot(
        source = source,
        serializer = serializer,
        initialConfiguration = initialConfiguration,
        key = key,
        handleBackButton = handleBackButton,
    ) { configuration, componentContext ->
        childFactory(
            configuration,
            BaseComponentContextImpl(
                componentContext = componentContext
            )
        )
    }

fun BaseComponentContext.appChildContext(key: String, lifecycle: Lifecycle? = null): BaseComponentContext =
    BaseComponentContextImpl(
        componentContext = childContext(key, lifecycle)
    )
