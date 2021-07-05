package dev.nokee.model.internal;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.KnownDomainObjectTester;
import dev.nokee.model.TestProjection;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DefaultKnownDomainObjectTest implements KnownDomainObjectTester<TestProjection> {
	@Override
	public KnownDomainObject<TestProjection> createSubject() {
		val graph = Graph.builder().build();
		val projection = DefaultModelProjection.builder()
			.forInstance(new TestProjection("test"))
			.type(TestProjection.class)
			.graph(graph).build();
		return new DefaultKnownDomainObject<>(TestProjection.class, projection);
	}

	@Test
	void canResolveAsCallable() {
		assertAll(
			() -> assertThat("is callable", createSubject(), isA(Callable.class)),
			() -> assertThat("returns a provider object", ((Callable<?>) createSubject()).call(), isA(Provider.class)),
			() -> assertThat("returns a provider for the object",
				((Callable<Provider<?>>) createSubject()).call(), providerOf(isA(TestProjection.class)))
		);
	}
}
