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
