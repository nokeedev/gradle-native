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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.HasXcodeTargetReference;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeTargetExecTaskOutOfDateSpec;
import dev.nokee.xcode.XCLoader;
import dev.nokee.xcode.XCTargetReference;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.targets.BuildPhaseAwareBuilder;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Consumer;

import static dev.nokee.buildadapter.xcode.TestTargetReference.target;
import static dev.nokee.internal.testing.GradleSpecMatchers.satisfiedBy;
import static dev.nokee.internal.testing.GradleSpecMatchers.unsatisfiedBy;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static dev.nokee.util.internal.DoNothingConsumer.doNothing;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class XcodeTargetExecTaskOutOfDateSpecTests {
	@Mock XCLoader<PBXTarget, XCTargetReference> loader;
	@Mock XcodeTargetExecTaskOutOfDateSpec.AlwaysOutOfDateShellScriptBuildPhasePredicate outOfDatePredicate;
	XcodeTargetExecTaskOutOfDateSpec subject;

	@BeforeEach
	void givenSubject() {
		subject = new XcodeTargetExecTaskOutOfDateSpec(loader, outOfDatePredicate);
	}

	@Nested
	class WhenNoTargetSpecified {
		HasXcodeTargetReference target = () -> providerFactory().provider(() -> null);

		@Test
		void isUnsatisfied() {
			assertThat(subject, unsatisfiedBy(target));
		}
	}

	@Nested
	class WhenTargetSpecified {
		XCTargetReference reference = target("Test");
		HasXcodeTargetReference target = () -> providerFactory().provider(() -> reference);

		@Nested
		class HasNoBuildPhases {
			@BeforeEach
			void givenTargetWithoutBuildPhases() {
				Mockito.when(loader.load(reference)).thenReturn(newTarget(doNothing()));
			}

			@Test
			void isSatisfied() {
				assertThat(subject, satisfiedBy(target));
			}
		}

		@Nested
		class HasNoShellScriptBuildPhases {
			@Mock
			PBXBuildPhase aBuildPhase;
			@Mock
			PBXBuildPhase anotherBuildPhase;

			@BeforeEach
			void givenTargetWithNoShellScriptBuildPhases() {
				Mockito.when(loader.load(reference)).thenReturn(newTarget(it -> it.buildPhase(aBuildPhase).buildPhase(anotherBuildPhase)));
			}

			@Test
			void isSatisfied() {
				assertThat(subject, satisfiedBy(target));
			}
		}

		@Nested
		class HasAlwaysOutOfDateShellScriptBuildPhases {
			@Mock
			PBXShellScriptBuildPhase shellScriptBuildPhase;

			@BeforeEach
			void givenTargetWithAlwaysOutOfDateShellScriptBuildPhase() {
				Mockito.when(outOfDatePredicate.test(shellScriptBuildPhase)).thenReturn(true);
				Mockito.when(loader.load(reference)).thenReturn(newTarget(it -> it.buildPhase(shellScriptBuildPhase)));
			}

			@Test
			void isUnsatisfied() {
				assertThat(subject, unsatisfiedBy(target));
			}
		}

		@Nested
		class HasIncrementalShellScriptBuildPhases {
			@Mock
			PBXShellScriptBuildPhase shellScriptBuildPhase;

			@BeforeEach
			void givenTargetWithIncrementalShellScriptBuildPhase() {
				Mockito.when(outOfDatePredicate.test(shellScriptBuildPhase)).thenReturn(false);
				Mockito.when(loader.load(reference)).thenReturn(newTarget(it -> it.buildPhase(shellScriptBuildPhase)));
			}

			@Test
			void isSatisfied() {
				assertThat(subject, satisfiedBy(target));
			}
		}
	}

	static PBXTarget newTarget(Consumer<? super BuildPhaseAwareBuilder<?>> action) {
		val builder = PBXAggregateTarget.builder().name("Test").buildConfigurations(defaultConfigurations());
		action.accept(builder);
		return builder.build();
	}

	private static XCConfigurationList defaultConfigurations() {
		return XCConfigurationList.builder().buildConfiguration(it -> it.name("Debug")).buildConfiguration(it -> it.name("Release")).build();
	}
}
