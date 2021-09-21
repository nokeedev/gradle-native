/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.DisambiguationRulesTester;
import dev.nokee.runtime.nativebase.BuildType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuildTypeAttributeSchema_SelectionRulesTest {
	private final DisambiguationRulesTester<BuildType> tester = DisambiguationRulesTester.of(new BuildTypeAttributeSchema.SelectionRules());

	@ParameterizedTest(name = "select first debug alike build type when no consumer value [{arguments}]")
	@ValueSource(strings = { "debug", "Debug", "RelWithDebug", "DEBUG", "debug-optimized"})
	void selectFirstDebugAlikeBuildTypeWhenNoConsumerValue(String debugBuildType) {
		tester.whenConsuming(null).fromCandidates("release", debugBuildType, "final").assertClosestMatch(debugBuildType);
	}

	@Test
	void selectExactBuildTypeWhenAvailable() {
		tester.whenConsuming("final").fromCandidates("release", "debug", "final").assertClosestMatch("final");
	}

	@Test
	void selectOnlyCandidateRegardlessIfMatches() {
		tester.whenConsuming("release").fromCandidates("RelOptimized").assertClosestMatch("RelOptimized");
	}

	@ParameterizedTest(name = "select similar looking candidate [{arguments}]")
	@ValueSource(strings = { "Final", "FINAL", "FiNaL"})
	void selectSimilarLookingCandidate(String similarBuildType) {
		tester.whenConsuming("final").fromCandidates("debug", similarBuildType, "release").assertClosestMatch(similarBuildType);
	}

	@ParameterizedTest(name = "select first debug alike build type when specific value [{arguments}]")
	@ValueSource(strings = { "debug", "Debug", "RelWithDebug", "DEBUG", "debug-optimized"})
	void selectFirstDebugAlikeBuildTypeWhenConsumingSpecificValue(String debugBuildType) {
		tester.whenConsuming("my-build-type").fromCandidates("final", debugBuildType, "release").assertClosestMatch(debugBuildType);
	}
}
