package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.CompatibilityRulesTester;
import dev.nokee.runtime.nativebase.BuildType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BuildTypeAttributeSchema_CompatibilityRulesTest {
	private final CompatibilityRulesTester<BuildType> tester = CompatibilityRulesTester.of(new BuildTypeAttributeSchema.CompatibilityRules());

	@ParameterizedTest
	@ValueSource(strings = {"debug", "release", "RelWithDebug"})
	void mark(String buildType) {
		tester.whenConsuming(buildType).fromProducer("some-build-type").assertMarkedAsCompatible();
	}
}
