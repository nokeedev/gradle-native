package dev.nokee.model.internal;

import dev.nokee.model.NokeeExtension;
import dev.nokee.model.NokeeExtensionTester;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;

class DefaultNokeeExtensionTest implements NokeeExtensionTester {
	@Override
	public NokeeExtension createSubject() {
		return objectFactory().newInstance(DefaultNokeeExtension.class, new DefaultNamedDomainObjectRegistry());
	}
}
