package cgm.experiments.dependencyinjection

import cgm.experiments.dependencyinjection.annotation.Injected
import org.reflections.Reflections
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.jvmErasure

object DependencyInjection {
    private var container: MutableMap<KClass<Any>, KClass<Any>> = mutableMapOf()
    private var containerFunctions: MutableMap<KClass<Any>, DependencyInjection.() -> Any> = mutableMapOf()

    inline fun <reified T: Any> get(): T? {
        return get(T::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> get(clazz: KClass<T>): T? = container[getContainerKey(clazz)]?.constructors?.let { constructors ->
        if (constructors.isEmpty()) null
        else {
            val emptyConstructor = constructors
                .firstOrNull { it.parameters.isEmpty() }

            val result: Any = emptyConstructor
                ?.call()
                ?: callConstructorWithArgs(constructors)

            result
        }
    } as T?
    ?: constructByFactoryFn(clazz)

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> constructByFactoryFn(clazz: KClass<T>): T? =
        containerFunctions[getContainerKey(clazz)]?.invoke(this) as T?

    private fun callConstructorWithArgs(constructors: Collection<KFunction<Any>>): Any {
        val constructor = constructors
            .sortedBy { it.parameters.size }
            .first()

        val args = constructor.parameters.map {
            get(it.type.jvmErasure)
        }.toTypedArray()

        return constructor.call(*args)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> getContainerKey(clazz: KClass<T>) = clazz as KClass<Any>

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T: Any> add() {
        add(T::class)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: Any> add(clazz: KClass<T>) {
        container[getContainerKey(clazz)] = getContainerValue(clazz)
        clazz.supertypes
            .asSequence()
            .map { getContainerKey(it.jvmErasure) }
            .forEach { addI(it, clazz) }
    }


    inline fun <reified T: Any, reified U: T> addI() {
        addI(T::class, U::class)
    }

    fun <T: Any, U: T> addI(interfaze: KClass<T>, clazz: KClass<U>) {
        container[getContainerKey(interfaze)] = getContainerValue<T, U>(clazz)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Any, U : T> getContainerValue(clazz: KClass<U>) = clazz as KClass<Any>

    inline fun <reified T: Any> add(noinline factoryFn: DependencyInjection.() -> T) {
        add(T::class, factoryFn)
    }

    fun <T: Any> add(clazz: KClass<T>, factoryFn: DependencyInjection.() -> T) {
        containerFunctions[getContainerKey(clazz)] = factoryFn
    }

    fun reset() {
        container.clear()
        containerFunctions.clear()
    }
}

inline fun <T> di(function: DependencyInjection.() -> T): T = DependencyInjection.function()

fun diAutoConfigure(packageName: String) {
    Reflections(packageName)
        .getTypesAnnotatedWith(Injected::class.java)
        .forEach {
            DependencyInjection.add(it.kotlin)
        }
}