package cgm.experiments.dependencyinjection

object DependencyInjection {
    inline fun <reified T> get(): T {
        TODO("Not yet implemented")
    }

    inline fun <reified T> add() {
        TODO("Not yet implemented")
    }

    inline fun <reified T: Any> add(noinline factoryFn: DependencyInjection.() -> T) {
        TODO("Not yet implemented")
    }

    fun reset() {
        TODO("Not yet implemented")
    }
}