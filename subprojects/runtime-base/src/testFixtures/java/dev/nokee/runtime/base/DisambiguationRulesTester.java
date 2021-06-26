package dev.nokee.runtime.base;

import com.google.common.collect.ImmutableSet;
import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import org.gradle.api.Named;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static org.mockito.Mockito.*;

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
