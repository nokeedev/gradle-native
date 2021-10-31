package dev.nokee.language.nativebase;

import dev.nokee.language.base.testers.HasDestinationDirectoryTester;
import dev.nokee.language.base.testers.SourceCompileTester;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.GradleProviderMatchers.presentProvider;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

public interface NativeSourceCompileTester extends SourceCompileTester, HasDestinationDirectoryTester, HasObjectFilesTester {
	NativeSourceCompile subject();

	@Override
	default void hasToolChain() {
		assertThat("provide NativeToolChain", subject().getToolChain(), providerOf(isA(NativeToolChain.class)));
	}

	@Test
	default void hasHeaderSearchPaths() {
		assertThat("not null as per contract", subject().getHeaderSearchPaths(), notNullValue());
		assertThat("provide a value", subject().getHeaderSearchPaths(), presentProvider());
	}
}
