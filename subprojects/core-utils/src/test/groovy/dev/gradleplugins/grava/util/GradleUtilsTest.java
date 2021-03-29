package dev.gradleplugins.grava.util;

import lombok.val;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.invocation.Gradle;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.util.GradleUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GradleUtilsTest {
	@Test
	void canDetectRootProject() {
		assertAll(
			() -> {
				val hostBuild = mock(Gradle.class);
				when(hostBuild.getParent()).thenReturn(null);
				assertThat(isHostBuild(hostBuild), is(true));
			},
			() -> {
				val includedBuild = mock(Gradle.class);
				when(includedBuild.getParent()).thenReturn(mock(Gradle.class));
				assertThat(isHostBuild(includedBuild), is(false));
			}
		);
	}

	@Test
	void canDetectIncludedBuilds() {
		given:
		assertAll(
			() -> {
				val buildWithIncludedBuilds = mock(Gradle.class);
				when(buildWithIncludedBuilds.getIncludedBuilds()).thenReturn(asList(mock(IncludedBuild.class), mock(IncludedBuild.class)));
				assertThat(hasIncludedBuilds(buildWithIncludedBuilds), is(true));
			},
			() -> {
				val buildWithIncludedBuilds = mock(Gradle.class);
				when(buildWithIncludedBuilds.getIncludedBuilds()).thenReturn(emptyList());
				assertThat(hasIncludedBuilds(buildWithIncludedBuilds), is(false));
			}
		);
	}

	@Test
	void canDetectWhenABuildIsIncludedInsideAnotherOne() {
		assertAll(
			() -> {
				val root = mock(Gradle.class);
				when(root.getParent()).thenReturn(null);
				assertThat(isIncludedBuild(root), is(false));
			},
			() -> {
				val child = mock(Gradle.class);
				when(child.getParent()).thenReturn(mock(Gradle.class));
				assertThat(isIncludedBuild(child), is(true));
			}
		);
	}

	@Test
	void canDetectWhenABuildIsACompositeBuild() {
		assertAll(
			() -> {
				val root = mock(Gradle.class);
				when(root.getParent()).thenReturn(null);
				when(root.getIncludedBuilds()).thenReturn(singletonList(mock(IncludedBuild.class)));
				assertThat(isCompositeBuild(root), is(true));
			},
			() -> {
				val child = mock(Gradle.class);
				when(child.getParent()).thenReturn(mock(Gradle.class));
				when(child.getIncludedBuilds()).thenReturn(emptyList());
				assertThat(isCompositeBuild(child), is(true));
			}
		);
	}

	@Test
	void canDetectWhenBuildIsNotCompositeBuild() {
		val build = mock(Gradle.class);
		when(build.getParent()).thenReturn(null);
		when(build.getIncludedBuilds()).thenReturn(emptyList());
		assertThat(isCompositeBuild(build), is(false));
	}
}
