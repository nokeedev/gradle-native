package dev.nokee.utils;

import lombok.val;
import org.gradle.api.Named;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.utils.NamedDomainObjectCollectionUtils.getElementType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NamedDomainObjectCollectionUtils_GetElementTypeTest {
	@Test
	void canGetTypeOnTaskContainer() {
		val type = assertDoesNotThrow(() -> getElementType(rootProject().getTasks()));
		assertThat(type, is(Task.class));
	}

	@Test
	void canGetTypeOnNamedContainer() {
		val container = objectFactory().domainObjectContainer(Bean.class);
		val type = assertDoesNotThrow(() -> getElementType(container));
		assertThat(type, is(Bean.class));
	}

	@Test
	void canGetTypeOnPolymorphicContainer() {
		val container = objectFactory().polymorphicDomainObjectContainer(Bean.class);
		val type = assertDoesNotThrow(() -> getElementType(container));
		assertThat(type, is(Bean.class));
	}

	interface Bean extends Named {}
}
