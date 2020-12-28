package dev.nokee.language.base.testers;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public abstract class LanguageSourceSetTester<T extends LanguageSourceSet> extends LanguageSourceSetContentTester<T> implements LanguageSourceSetEmptyTester<T>, LanguageSourceSetSourceDirectoryTester<T>, LanguageSourceSetFilterTester<T>, LanguageSourceSetBuildDependenciesTester<T>, LanguageSourceSetCommonUsageTester<T>, LanguageSourceSetConventionTester<T> {
	@Test
	void canGetBackingNodeFromInstance() {
		assertThat(ModelNodes.of(createSubject()), notNullValue(ModelNode.class));
	}
}
