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
package dev.nokee.runtime.base;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import org.gradle.api.Named;
import org.gradle.api.attributes.AttributeCompatibilityRule;
import org.gradle.api.attributes.CompatibilityCheckDetails;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


// TODO: Move to runtime-base fixtures
public final class CompatibilityRulesTester<T extends Named> {
	@SuppressWarnings("unchecked")
	private final CompatibilityCheckDetails<T> details = Mockito.mock(CompatibilityCheckDetails.class);
	private final AttributeCompatibilityRule<T> subject;
	private final Class<T> attributeType;

	private CompatibilityRulesTester(AttributeCompatibilityRule<T> subject) {
		this.subject = subject;
		this.attributeType = guessAttributeTypeFromSubject(subject);
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> guessAttributeTypeFromSubject(AttributeCompatibilityRule<T> subject) {
		for (Type genericInterface : subject.getClass().getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType) {
				if (AttributeCompatibilityRule.class.isAssignableFrom((Class<?>) ((ParameterizedType) genericInterface).getRawType())) {
					return (Class<T>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
				}
			}
		}
		throw new UnsupportedOperationException("Cannot guess attribute type");
	}

	public static <T extends Named> CompatibilityRulesTester<T> of(AttributeCompatibilityRule<T> rulesUnderTest) {
		return new CompatibilityRulesTester<>(rulesUnderTest);
	}

	public CompatibilityRulesTester<T> fromProducer(String name) {
		Mockito.when(details.getProducerValue()).thenReturn(ProjectTestUtils.objectFactory().named(attributeType, name));
		return this;
	}

	public CompatibilityRulesTester<T> whenConsuming(String name) {
		Mockito.when(details.getConsumerValue()).thenReturn(ProjectTestUtils.objectFactory().named(attributeType, name));
		return this;
	}

	public void assertDidNothing() {
		subject.execute(details);
		Mockito.verify(details, Mockito.never()).compatible();
		Mockito.verify(details, Mockito.never()).incompatible();
	}

	public void assertMarkedAsCompatible() {
		subject.execute(details);
		Mockito.verify(details).compatible();
		Mockito.verify(details, Mockito.never()).incompatible();
	}
}
