package dev.nokee.model.internal;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.KnownDomainObjectTester;
import dev.nokee.model.TestProjection;
import dev.nokee.model.graphdb.Graph;
import lombok.val;

public class DefaultKnownDomainObjectTest implements KnownDomainObjectTester {
	@Override
	public KnownDomainObject<TestProjection> createSubject() {
		val graph = Graph.builder().build();
		val projection = DefaultModelProjection.builder()
			.forInstance(new TestProjection("test"))
			.type(TestProjection.class)
			.graph(graph).build();
		return new DefaultKnownDomainObject<>(TestProjection.class, projection);
	}
}
