package by.tigre.numbers.presentation.additional

import by.tigre.tools.presentation.base.BaseComponentContext

interface RootAdditionalComponent {

    class Impl(
        context: BaseComponentContext,
    ) : RootAdditionalComponent, BaseComponentContext by context
}
