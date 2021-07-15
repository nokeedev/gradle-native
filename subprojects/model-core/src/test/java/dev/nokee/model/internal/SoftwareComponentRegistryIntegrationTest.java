package dev.nokee.model.internal;

import dev.nokee.utils.Cast;
import lombok.val;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;

class SoftwareComponentRegistryIntegrationTest extends AbstractDomainObjectContainerRegistryIntegrationTest<SoftwareComponent> {
	@Override
	protected NamedDomainObjectContainerRegistry<SoftwareComponent> createSubject() {
		val project = rootProject();
		return project.getObjects().newInstance(NamedDomainObjectContainerRegistry.SoftwareComponentContainerRegistry.class, project.getComponents());
	}

	@Override
	protected Class<SoftwareComponent> getType() {
		return Cast.uncheckedCast("we can only create adhoc components", AdhocComponentWithVariants.class);
	}
}
