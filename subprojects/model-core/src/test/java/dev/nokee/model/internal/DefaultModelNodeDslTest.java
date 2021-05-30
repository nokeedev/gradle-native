package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.TestProjection;
import dev.nokee.model.dsl.*;
import dev.nokee.model.graphdb.Graph;
import dev.nokee.model.registry.DomainObjectRegistry;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelNodeDslTest implements ModelNodeTester, ModelNodeCreateChildNodeTester, ModelNodeCreateProjectionTester, ModelNodeGetExistingChildNodeTester, ModelNodeGetExistingProjectionTester {
	@Override
	public ModelNode createSubject() {
		val graph = Graph.builder().build();
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		return new DefaultModelNodeDsl(new DomainObjectRegistry() {
			@Override
			public <T> NamedDomainObjectProvider<T> register(DomainObjectIdentifier identifier, Class<T> type) {
				if (TestProjection.class.isAssignableFrom(type)) {
					return (NamedDomainObjectProvider<T>) container.register("test");
				}
				throw new UnsupportedOperationException();
			}
		}, new DefaultModelNode(graph, graph.createNode()));
	}
}
