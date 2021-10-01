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

import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import org.gradle.api.Named;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

// TODO: Move to runtime-base fixture
public final class DisambiguationRulesTester<T extends Named> {
	@SuppressWarnings("unchecked")
	private final MultipleCandidatesDetails<T> details = Mockito.mock(MultipleCandidatesDetails.class);
	private final AttributeDisambiguationRule<T> subject;
	private final Class<T> attributeType;

	private DisambiguationRulesTester(AttributeDisambiguationRule<T> subject) {
		this.subject = subject;
		this.attributeType = guessAttributeTypeFromSubject(subject);
	}

	@SuppressWarnings("unchecked")
	private static <T> Class<T> guessAttributeTypeFromSubject(AttributeDisambiguationRule<T> subject) {
		for (Type genericInterface : subject.getClass().getGenericInterfaces()) {
			if (genericInterface instanceof ParameterizedType) {
				if (AttributeDisambiguationRule.class.isAssignableFrom((Class<?>) ((ParameterizedType) genericInterface).getRawType())) {
					return (Class<T>) ((ParameterizedType) genericInterface).getActualTypeArguments()[0];
				}
			}
		}
		throw new UnsupportedOperationException("Cannot guess attribute type");
	}

	public static <T extends Named> DisambiguationRulesTester<T> of(AttributeDisambiguationRule<T> rulesUnderTest) {
		return new DisambiguationRulesTester<>(rulesUnderTest);
	}

	public DisambiguationRulesTester<T> whenConsuming(String name) {
		if (name != null) {
			Mockito.when(details.getConsumerValue()).thenReturn(ProjectTestUtils.objectFactory().named(attributeType, name));
		}
		return this;
	}

	public DisambiguationRulesTester<T> fromCandidates(String... names) {
		Mockito.when(details.getCandidateValues()).thenReturn(Arrays.stream(names).map(name -> ProjectTestUtils.objectFactory().named(attributeType, name)).collect(ImmutableSet.toImmutableSet()));
		return this;
	}

	public void assertClosestMatch(String name) {
		subject.execute(details);
		Mockito.verify(details).closestMatch(ProjectTestUtils.objectFactory().named(attributeType, name));
	}

	public void assertNoMatch() {
		subject.execute(details);
		Mockito.verify(details, Mockito.never()).closestMatch(ArgumentMatchers.any());
	}
}
