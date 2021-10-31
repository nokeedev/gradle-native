package dev.nokee.language.base.testers;

import dev.nokee.language.base.tasks.SourceCompile;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;
import org.gradle.platform.base.ToolChain;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface SourceCompileTester {
	SourceCompile subject();

	@Test
	default void hasToolChain() {
		assertThat("not null as per contract", subject().getToolChain(), notNullValue(Provider.class));
		assertThat("provide a ToolChain", subject().getToolChain(), providerOf(isA(ToolChain.class)));
	}

	@Test
	default void hasCompilerArgs() {
		assertThat("not null as per contract", subject().getCompilerArgs(), notNullValue(ListProperty.class));
		assertThat("there should be a value", subject().getCompilerArgs(), presentProvider());
	}

	@Test
	default void hasSource() {
		assertThat("not null as per contract", subject().getSource(), notNullValue(ConfigurableFileCollection.class));
	}

	@Test
	default void hasDescription() {
		assertThat(subject().getDescription(), not(blankOrNullString()));
	}
}
