package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.dsl.*;
import dev.nokee.model.graphdb.Graph;
import lombok.val;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelNodeDslTest implements ModelNodeTester, ModelNodeCreateChildNodeTester, ModelNodeCreateProjectionTester, ModelNodeGetExistingChildNodeTester, ModelNodeGetExistingProjectionTester {
	@Override
	public ModelNode createSubject() {
		val graph = Graph.builder().build();
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		val registry = new DefaultNamedDomainObjectRegistry().registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		return new DefaultModelNodeDslFactory(registry).create(new DefaultModelFactory(graph).createNode(graph.createNode()));
	}
}
