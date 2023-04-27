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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.EqualsAndHashCode;
import org.gradle.api.specs.Spec;

import java.util.function.Predicate;

@EqualsAndHashCode
public final class XcodeTargetExecTaskOutOfDateSpec implements Spec<HasXcodeTargetReference> {
	private final XCLoader<PBXTarget, XCTargetReference> loader;
	private final AlwaysOutOfDateShellScriptBuildPhasePredicate outOfDatePredicate;

	public XcodeTargetExecTaskOutOfDateSpec(XCLoader<PBXTarget, XCTargetReference> loader, AlwaysOutOfDateShellScriptBuildPhasePredicate outOfDatePredicate) {
		this.loader = loader;
		this.outOfDatePredicate = outOfDatePredicate;
	}

	@Override
	public boolean isSatisfiedBy(HasXcodeTargetReference o) {
		return o.getTargetReference().map(ref -> ref.load(loader)
				.getBuildPhases()
				.stream()
				.filter(PBXShellScriptBuildPhase.class::isInstance)
				.map(PBXShellScriptBuildPhase.class::cast)
				.noneMatch(outOfDatePredicate)
			).getOrElse(false);
		// WHAT-IF: there is one xcfilelist (as input and/or output) but is empty?
	}

	public interface AlwaysOutOfDateShellScriptBuildPhasePredicate extends Predicate<PBXShellScriptBuildPhase> {}

	@EqualsAndHashCode
	public static final class DefaultPredicate implements AlwaysOutOfDateShellScriptBuildPhasePredicate {
		@Override
		public boolean test(PBXShellScriptBuildPhase buildPhase) {
			return !hasAtLeastOneInputFile(buildPhase) || !hasAtLeastOneOutputFile(buildPhase);
		}

		private boolean hasAtLeastOneInputFile(PBXShellScriptBuildPhase buildPhase) {
			return !buildPhase.getInputPaths().isEmpty() || !buildPhase.getInputFileListPaths().isEmpty();
		}

		private boolean hasAtLeastOneOutputFile(PBXShellScriptBuildPhase buildPhase) {
			return !buildPhase.getOutputPaths().isEmpty() || !buildPhase.getOutputFileListPaths().isEmpty();
		}
	}
}
