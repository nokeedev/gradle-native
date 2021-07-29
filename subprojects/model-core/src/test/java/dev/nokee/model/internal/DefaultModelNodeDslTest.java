package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.dsl.*;
import lombok.val;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelNodeDslTest implements ModelNodeTester, ModelNodeCreateChildNodeTester, ModelNodeCreateProjectionTester, ModelNodeGetExistingChildNodeTester, ModelNodeGetExistingProjectionTester, ModelNodeMatchingTester {
	@Override
	public ModelNode createSubject() {
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		val registry = new DefaultNamedDomainObjectRegistry().registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		val modelRegistry = new DefaultModelRegistry(objectFactory(), registry);
		return new DefaultModelNodeDslFactory(modelRegistry.allProjections(), objectFactory()).create(modelRegistry.getRoot());
	}
}
