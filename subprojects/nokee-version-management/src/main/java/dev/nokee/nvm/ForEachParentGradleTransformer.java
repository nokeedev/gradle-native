/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.nvm;

import org.gradle.api.Transformer;
import org.gradle.api.artifacts.component.BuildIdentifier;
import org.gradle.api.initialization.IncludedBuild;
import org.gradle.api.internal.GradleInternal;
import org.gradle.api.invocation.Gradle;
import org.gradle.internal.build.BuildState;
import org.gradle.util.GradleVersion;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Returns the first non-null mapping for each parent builds as presented by the build graph.
 * There are some trickery we need to do in order to properly iterate through parents in the build graph.
 * Gradle has a built-in difference between implicit and explicit build.
 * The parent of an implicit build is correctly its parent in the build graph.
 * However, the parent of an explicit build seems to be the root build.
 * For the explicit builds, we then need to walk the included build forward until we find our current build.
 * At that point, we now have the full chain between our current build and its top-most parent.
 *
 * @param <OUT>  the transformed type
 */
class ForEachParentGradleTransformer<OUT> implements Transformer<OUT, Gradle> {
	private final Function<Gradle, OUT> mapper;

	public ForEachParentGradleTransformer(Function<Gradle, OUT> mapper) {
		this.mapper = mapper;
	}

	@Nullable // it's fine to return null despite non-null API, provider mapping accept null return value
	@Override
	public OUT transform(Gradle gradle) {
		return transform((GradleInternal) gradle);
	}

	@Nullable
	private OUT transform(GradleInternal gradle) {
		while (gradle.getParent() != null) {
			if (gradle.getOwner().isImplicitBuild()) {
				final Gradle parent = gradle.getParent();
				final OUT result = mapper.apply(parent);
				if (result != null) {
					return result;
				}
			} else {
				// walk backward
				try {
					List<GradleInternal> parents = new ArrayList<>();
					findParents(gradle.getParent(), gradle).forEach(parents::add);
					for (final GradleInternal parent : parents) {
						final OUT result = mapper.apply(parent);
						if (result != null) {
							return result;
						}
					}
				} catch (IllegalStateException ex) {
					// When the build applies enterprise plugin AND uses plugin builds,
					//   we enter an illegal state.
					// We will assume, at this stage, that we haven't found a parent service...
				}
			}
			gradle = gradle.getParent();
		}
		return null;
	}

	private static Iterable<GradleInternal> findParents(GradleInternal parent, GradleInternal childToFind) {
		final List<GradleInternal> result = new ArrayList<>();
		findParents(result, parent, childToFind);
		return Collections.unmodifiableList(result);
	}

	private static boolean findParents(List<GradleInternal> result, GradleInternal parent, GradleInternal childToFind) {
		// getIncludedBuilds may throw an exception if, for some reason, we are too early.
		//   In that scenario, we will try our best to find the Nokee version.
		for (final IncludedBuild includedBuild : parent.getIncludedBuilds()) {
			// To verify the child, we don't try to get the model for the included build because this can cause a realized loop
			if (getBuildIdentifier(includedBuild).equals(childToFind.getOwner().getBuildIdentifier())) {
				result.add(parent);
				return true; // found our child
			} else {
				final GradleInternal includedBuildModel = getGradle(includedBuild);
				if (findParents(result, includedBuildModel, childToFind)) {
					result.add(parent);
					return true; // exit with our parent chain
				}
			}
		}
		return false; // nothing found
	}

	private static BuildIdentifier getBuildIdentifier(IncludedBuild includedBuild) {
		return asBuildState(includedBuild).getBuildIdentifier();
	}

	private static BuildState asBuildState(IncludedBuild includedBuild) {
		if (includedBuild instanceof BuildState) {
			return (BuildState) includedBuild;
		} else {
			try {
				Class<?> IncludedBuildInternal = Class.forName("org.gradle.internal.composite.IncludedBuildInternal");
				Method method = IncludedBuildInternal.getMethod("getTarget");
				return (BuildState) method.invoke(includedBuild);
			} catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static GradleInternal getGradle(IncludedBuild includedBuild) {
		return getGradle(asBuildState(includedBuild));
	}

	// 7.1 can use gradle.getOwner().getMutableModel()
	// 7.2 and lower can use gradle.getOwner().getLoadedSettings().getGradle()
	private static GradleInternal getGradle(BuildState self) {
		if (GradleVersion.current().compareTo(GradleVersion.version("7.1")) >= 0) {
			try {
				return (GradleInternal) BuildState.class.getMethod("getMutableModel").invoke(self);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				throw new RuntimeException(e);
			}
		} else {
			return self.getLoadedSettings().getGradle();
		}
	}
}
