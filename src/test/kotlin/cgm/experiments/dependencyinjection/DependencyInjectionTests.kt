package cgm.experiments.dependencyinjection

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class DependencyInjectionTests {

    @Test
    fun `test creation of a class with no dependency`() {
        DependencyInjection.add<Dependency>()

        val expected = Dependency()
        DependencyInjection.get<Dependency>() shouldBe expected
    }

}

