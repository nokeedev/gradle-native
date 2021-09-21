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
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.junit.jupiter.api.Test;

class BinaryLinkageAttributeSchema_SelectionRulesTest {
	private final DisambiguationRulesTester<BinaryLinkage> tester = DisambiguationRulesTester.of(new BinaryLinkageAttributeSchema.SelectionRules());

	@Test
	void alwaysPreferSharedBinaryLinkageWhenAvailable() {
		tester.whenConsuming(null).fromCandidates(BinaryLinkage.STATIC, BinaryLinkage.SHARED).assertClosestMatch(BinaryLinkage.SHARED);
	}

	@Test
	void noPreferredBinaryLinkageWhenSharedBinaryLinkageIsUnavailable() {
		tester.whenConsuming(null).fromCandidates(BinaryLinkage.STATIC, "some-other-linkage").assertNoMatch();
	}
}
