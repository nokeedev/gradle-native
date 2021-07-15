package dev.nokee.model.internal;

import dev.nokee.model.TestProjection;
import dev.nokee.model.dsl.*;
import lombok.val;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultModelNodeDslTest implements ModelNodeTester, ModelNodeCreateChildNodeTester, ModelNodeCreateProjectionTester, ModelNodeGetExistingChildNodeTester, ModelNodeGetExistingProjectionTester, ModelNodeMatchingTester {
	@Override
	public ModelNode createSubject() {
		val modelRegistry = new DefaultModelRegistry(objectFactory());
		val container = objectFactory().domainObjectContainer(TestProjection.class);
		val registry = new DefaultNamedDomainObjectRegistry().registerContainer(new NamedDomainObjectContainerRegistry.NamedContainerRegistry<>(container));
		return new DefaultModelNodeDslFactory(registry, modelRegistry.allProjections(), objectFactory()).create(modelRegistry.getRoot());
	}
}
