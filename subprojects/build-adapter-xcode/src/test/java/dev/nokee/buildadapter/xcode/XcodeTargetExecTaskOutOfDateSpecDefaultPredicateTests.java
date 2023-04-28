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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeTargetExecTaskOutOfDateSpec;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import lombok.val;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.hasEmptyInputFileListPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.hasEmptyOutputFileListPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.hasInputPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.hasNonEmptyInputFileListPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.hasNonEmptyOutputFileListPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.hasOutputPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.noInputFileListPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.noInputPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.noOutputFileListPaths;
import static dev.nokee.buildadapter.xcode.XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests.GivenSpec.noOutputPaths;
import static dev.nokee.util.internal.ToOnlyInstanceOfFunction.toOnlyInstanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class XcodeTargetExecTaskOutOfDateSpecDefaultPredicateTests {
	XcodeTargetExecTaskOutOfDateSpec.DefaultPredicate subject = new XcodeTargetExecTaskOutOfDateSpec.DefaultPredicate();

	@ParameterizedTest
	@ArgumentsSource(BuildPhaseProvider.class)
	void testsShellScriptBuildPhaseIncrementalConditions(boolean expectedResult, @AggregateWith(ToBuildPhase.class) PBXShellScriptBuildPhase buildPhase) {
		assertThat(subject.test(buildPhase), is(expectedResult));
	}

	static final class ToBuildPhase implements ArgumentsAggregator {
		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) throws ArgumentsAggregationException {
			val result = Mockito.mock(PBXShellScriptBuildPhase.class);

			val inputPaths = ImmutableList.<String>builder();
			val outputPaths = ImmutableList.<String>builder();
			val inputFileListPaths = ImmutableList.<String>builder();
			val outputFileListPaths = ImmutableList.<String>builder();
			accessor.toList().stream().flatMap(toOnlyInstanceOf(GivenSpec.class)).forEach(it -> {
				inputPaths.addAll(it.inputPaths());
				outputPaths.addAll(it.outputPaths());
				inputFileListPaths.addAll(it.inputFileListPaths());
				outputFileListPaths.addAll(it.outputFileListPaths());
			});

			Mockito.when(result.getInputPaths()).thenReturn(inputPaths.build());
			Mockito.when(result.getOutputPaths()).thenReturn(outputPaths.build());
			Mockito.when(result.getInputFileListPaths()).thenReturn(inputFileListPaths.build());
			Mockito.when(result.getOutputFileListPaths()).thenReturn(outputFileListPaths.build());
			return result;
		}
	}

	static final class BuildPhaseProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Sets.cartesianProduct(
				of(noInputPaths, hasInputPaths),
				of(noOutputPaths, hasOutputPaths),
				of(noInputFileListPaths, hasEmptyInputFileListPaths, hasNonEmptyInputFileListPaths),
				of(noOutputFileListPaths, hasEmptyOutputFileListPaths, hasNonEmptyOutputFileListPaths)
			).stream().map(specs -> arguments(ImmutableList.builder()
				.add(!((specs.contains(hasInputPaths) || specs.contains(hasEmptyInputFileListPaths) || specs.contains(hasNonEmptyInputFileListPaths))
					&& (specs.contains(hasOutputPaths) || specs.contains(hasEmptyOutputFileListPaths) || specs.contains(hasNonEmptyOutputFileListPaths))))
				.addAll(specs)
				.build().toArray(new Object[0])
			));
		}
	}

	enum GivenSpec {
		noInputPaths,
		hasInputPaths {
			@Override
			public List<String> inputPaths() {
				return ImmutableList.of("$(SRCROOT)/my-input.txt");
			}
		},

		noOutputPaths,
		hasOutputPaths {
			@Override
			public List<String> outputPaths() {
				return ImmutableList.of("$(DERIVED_FILES_DIR)/my-output.txt");
			}
		},

		noInputFileListPaths,
		hasEmptyInputFileListPaths {
			@Override
			public List<String> inputFileListPaths() {
				return ImmutableList.of("$(SRCROOT)/my-empty-inputs.xcfilelist");
			}
		},
		hasNonEmptyInputFileListPaths {
			@Override
			public List<String> inputFileListPaths() {
				return ImmutableList.of("$(SRCROOT)/my-non-empty-inputs.xcfilelist");
			}
		},

		noOutputFileListPaths,
		hasEmptyOutputFileListPaths {
			@Override
			public List<String> outputFileListPaths() {
				return ImmutableList.of("$(SRCROOT)/my-empty-outputs.xcfilelist");
			}
		},
		hasNonEmptyOutputFileListPaths {
			@Override
			public List<String> outputFileListPaths() {
				return ImmutableList.of("$(SRCROOT)/my-non-empty-outputs.xcfilelist");
			}
		},
		;

		public List<String> inputPaths() {
			return ImmutableList.of();
		}

		public List<String> outputPaths() {
			return ImmutableList.of();
		}

		public List<String> inputFileListPaths() {
			return ImmutableList.of();
		}

		public List<String> outputFileListPaths() {
			return ImmutableList.of();
		}
	}
}
