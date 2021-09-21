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

import dev.nokee.runtime.base.CompatibilityRulesTester;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UsageAttributeSchema_CompatibilityRulesTest {
	private final CompatibilityRulesTester<Usage> tester = CompatibilityRulesTester.of(new UsageAttributeSchema.CompatibilityRules());

	@ParameterizedTest
	@ValueSource(strings = {Usage.C_PLUS_PLUS_API, Usage.NATIVE_LINK})
	void markCompatibleWhenConsumerUsageIsPartOfCompoundProducerUsage(String usage) {
		tester.whenConsuming(usage).fromProducer(Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK).assertMarkedAsCompatible();
	}

	@Test
	void doNothingWhenConsumerUsageIsSubstringOfCompoundProducerUsage() {
		tester.whenConsuming("api").fromProducer(Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK).assertDidNothing();
	}

	@Test
	void doNothingWhenConsumerUsageIsNotPartOfCompoundProducerUsage() {
		tester.whenConsuming(Usage.NATIVE_RUNTIME).fromProducer(Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK).assertDidNothing();
	}
}
