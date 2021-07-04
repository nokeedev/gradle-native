package dev.nokee.utils;

import lombok.val;
import org.gradle.api.file.ConfigurableFileCollection;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.internal.testing.FileSystemMatchers.aFileNamed;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class FileCollectionUtils_ElementsOfTest {
	@Test
	void canGetFileCollectionElements() {
		val subject = objectFactory().newInstance(TestComponent.class);
		val provider = providerFactory().provider(() -> subject);
		subject.getSources().from("file.txt");
		assertThat(provider.flatMap(elementsOf(TestComponent::getSources)), providerOf(contains(aFileNamed("file.txt"))));
	}

	@Test
	void checkToString() {
		assertThat(elementsOf(TestComponent::getSources), hasToString(startsWith("FileCollectionUtils.elementsOf(")));
	}

	interface TestComponent {
		ConfigurableFileCollection getSources();
	}
}
