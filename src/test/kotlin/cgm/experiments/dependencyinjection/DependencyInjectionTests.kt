package cgm.experiments.dependencyinjection

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DependencyInjectionTests {

    @BeforeEach
    fun beforeEach(){
        DependencyInjection.reset()
    }

    @Test
    fun `test creation of a class with no dependency`() {
        DependencyInjection.add<Dependency>()

        val expected = Dependency()
        DependencyInjection.get<Dependency>() shouldBe expected
    }

    @Test
    fun `test creation of a class with one dependency`() {
        DependencyInjection.add<Dependency>()
        DependencyInjection.add<Dependent>()

        val expected = Dependent(Dependency())
        DependencyInjection.get<Dependent>() shouldBe expected
    }

    @Test
    fun `test creation of a class with more then one dependency`() {
        DependencyInjection.add<Dependency>()
        DependencyInjection.add<Dependency2>()
        DependencyInjection.add<Dependent>()
        DependencyInjection.add<Dependent2>()

        val expected = Dependent2(Dependency2(), Dependency())
        DependencyInjection.get<Dependent2>() shouldBe expected
    }

    @Test
    fun `test creation of a class with nested dependencies`() {
        DependencyInjection.add<Dependency>()
        DependencyInjection.add<Dependency2>()
        DependencyInjection.add<Dependent>()
        DependencyInjection.add<Dependent2>()
        DependencyInjection.add<DependentByNested>()

        val expected = DependentByNested(Dependent2(Dependency2(), Dependency()))
        DependencyInjection.get<DependentByNested>() shouldBe expected
    }

    @Test
    fun `test creation of a class by factory function`() {
        DependencyInjection.add { Dependent(Dependency()) }

        val expected = Dependent(Dependency())
        DependencyInjection.get<Dependent>() shouldBe expected
    }

    @Test
    fun `test creation of a class by factory with context`(){
        DependencyInjection.add { Dependent(get()!!) }
        DependencyInjection.add<Dependency>()

        val expected = Dependent(Dependency())
        DependencyInjection.get<Dependent>() shouldBe expected
    }

    @Test
    fun `test creation of a class with dsl`(){
        di {
            add { Dependent2(Dependency2(), get()!!) }
            add<Dependent>()
            add<Dependency>()
        }

        val expected = Dependent2(Dependency2(), Dependency())
        di { get<Dependent2>() } shouldBe expected
    }

    @Test
    fun `test creation of an interface with no dependency`(){
        di {
            addI<IDependency, Dependency>()

            get<IDependency>() shouldBe Dependency()
        }

    }

    @Test
    fun `test creation of an interface with nested dependencies`(){
        di {
            addI<INested, Nested>()
            addI<IDependency, Dependency>()

            val expected = Nested(Dependency())
            get<INested>() shouldBe expected
        }
    }

    @Test
    fun `test creation of an interface with factory functions`(){
        di {
            add<INested> { Nested(get()!!) }
            addI<IDependency, Dependency>()

            val expected = Nested(Dependency())
            get<INested>() shouldBe expected
        }
    }

    @Test
    fun `test dependency injection autoconfiguration`(){
        diAutoConfigure("com.example.di.tests")

        val expected = Nested(Dependency())
        di { get<INested>() } shouldBe expected
    }
}

