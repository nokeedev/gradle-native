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
package dev.nokee.platform.base.testers;

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.testing.testers.ConfigureMethodTester;
import dev.nokee.platform.base.*;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

public interface VariantAwareComponentTester<T extends ComponentVariants> {
	VariantAwareComponent<? extends Variant> subject();

	@SuppressWarnings({"unchecked", "UnstableApiUsage"})
	default Class<? extends ComponentVariants> getComponentVariantsType() {
		return (Class<? extends ComponentVariants>) new TypeToken<T>(getClass()) {}.getRawType();
	}

	@Test
	default void hasComponentVariants() {
		assertThat("component variants should be of the correct type",
			subject().getVariants(), isA(getComponentVariantsType()));
	}

	@Test
	default void canConfigureComponentVariants() {
		ConfigureMethodTester.of(subject(), VariantAwareComponent::getVariants)
			.testAction(VariantAwareComponent::variants)
			.testClosure(VariantAwareComponent::variants);
	}

	@Test
	default void hasVariantDimensions() {
		assertThat(subject().getDimensions(), notNullValue(VariantDimensions.class));
	}
}
