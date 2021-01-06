package dev.nokee.platform.base.testers;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.BaseNameAwareComponent;
import dev.nokee.platform.base.Component;
import lombok.val;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public interface BaseNameAwareComponentTester {
	BaseNameAwareComponent createSubject(String componentName);

	@Test
	default void canChangeBaseNameGlobally() {
		val subject = createSubject("main");
		subject.getBaseName().set("foo");
		ModelNodes.of(subject).finalizeValue(); // TODO: it should be automatic when variant are resolved
		assertThat(subject, hasArtifactBaseNameOf("foo"));
	}

	Matcher<Component> hasArtifactBaseNameOf(String name);
}
