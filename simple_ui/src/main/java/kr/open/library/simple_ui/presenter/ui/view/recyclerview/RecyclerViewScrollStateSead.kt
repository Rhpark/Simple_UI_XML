package kr.open.library.simple_ui.presenter.ui.view.recyclerview


public sealed class ScrollDirection {
    public data object UP : ScrollDirection()
    public data object DOWN : ScrollDirection()
    public data object LEFT : ScrollDirection()
    public data object RIGHT : ScrollDirection()
    public data object IDLE : ScrollDirection()
}

public sealed class ScrollEdge {
    public data object TOP: ScrollEdge()
    public data object BOTTOM: ScrollEdge()
    public data object LEFT: ScrollEdge()
    public data object RIGHT: ScrollEdge()
}
