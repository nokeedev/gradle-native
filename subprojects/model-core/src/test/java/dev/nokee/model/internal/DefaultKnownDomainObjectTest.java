package dev.nokee.model.internal;

import dev.nokee.model.*;
import dev.nokee.model.core.ModelProjection;
import dev.nokee.model.graphdb.Graph;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertAll;

public class DefaultKnownDomainObjectTest implements KnownDomainObjectTester<TestProjection> {
	private KnownDomainObject<TestProjection> subject;

	@BeforeEach
	void setup() {
		val graph = Graph.builder().build();
		val projection = DefaultModelProjection.builder()
			.graph(graph)
			.modelFactory(new DefaultModelFactory(graph))
			.forInstance(new TestProjection("test"))
			.type(TestProjection.class)
			.build();
		subject = new DefaultKnownDomainObject<>(TestProjection.class, projection);
	}

	@Override
	public KnownDomainObject<TestProjection> subject() {
		return subject;
	}

	@Test
	void canResolveAsCallable() {
		assertAll(
			() -> assertThat("is callable", subject(), isA(Callable.class)),
			() -> assertThat("returns a provider object", ((Callable<?>) subject()).call(), isA(Provider.class)),
			() -> assertThat("returns a provider for the object",
				((Callable<Provider<?>>) subject()).call(), providerOf(isA(TestProjection.class)))
		);
	}

	@Nested
	class DomainObjectIdentifierTest implements DomainObjectIdentifierTester {
		@Override
		public DomainObjectIdentifier createSubject(ModelProjection projection) {
			return new DefaultKnownDomainObject<>(projection.getType(), projection).getIdentifier();
		}
	}
}
