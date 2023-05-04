/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode.project;

import com.google.common.collect.MoreCollectors;
import com.google.common.reflect.TypeToken;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Tests visitable API matching the specified visitor type.
 * <p>
 * A visitable API is method with the following prototype <code>ReturnType accept(VisitorType visitor)}</code>.
 * The VisitorType is expected to have a method with the following prototype <code>ReturnType visitMethod(SubjectType subject)</code>.
 * Note the {@literal visitMethod} can be named however the user want but there can only be a single method that match the {@code SubjectType}.
 * Typically, the {@literal visitMethod} is named {@literal visit}.
 * @param <VisitorType> the visitor type
 */
public interface VisitableTester<VisitorType> {
	Object newSubject();

	@Test
	default void canVisit() {
		@SuppressWarnings({"UnstableApiUsage", "unchecked"})
		val visitorType = (Class<VisitorType>) new TypeToken<VisitorType>(getClass()) {}.getRawType();
		val visitor = Mockito.mock(visitorType);
		val subject = newSubject();
		val visitMethod = Arrays.stream(visitorType.getDeclaredMethods())
			// find a method that takes only one parameter of a compatible type with the subject
			.filter(it -> it.getParameterTypes().length == 1 && it.getParameterTypes()[0].isInstance(subject))
			.collect(MoreCollectors.onlyElement());
		val acceptMethod = Arrays.stream(subject.getClass().getMethods())
			// find a method with the following prototype: void accept(VisitorType)
			.filter(it -> it.getName().equals("accept") && it.getParameterTypes().length == 1 && it.getParameterTypes()[0].equals(visitorType))
			.collect(MoreCollectors.onlyElement());

		// And: has a return type
		val expectedReturnInstanceIfHasReturnType = new Object(); // assuming generic return type
		if (!acceptMethod.getGenericReturnType().equals(Void.TYPE)) {
			assertThat("visit and accept return type must match", acceptMethod.getGenericReturnType().getTypeName(),
				equalTo(visitMethod.getGenericReturnType().getTypeName()));

			try {
				Mockito.when(visitMethod.invoke(visitor, subject)).thenReturn(expectedReturnInstanceIfHasReturnType);
			} catch (InvocationTargetException | IllegalAccessException e) {
				Assertions.fail(e);
			}
		}

		// When: accept method called with visitor
		try {
			val actualReturnInstanceIfHasReturnType = acceptMethod.invoke(subject, visitor);

			// Then: returned instance
			if (!acceptMethod.getGenericReturnType().equals(Void.TYPE)) {
				assertThat("visit method return forwarded through accept method", actualReturnInstanceIfHasReturnType,
					equalTo(expectedReturnInstanceIfHasReturnType));
			}
		} catch (InvocationTargetException | IllegalAccessException e) {
			Assertions.fail(e);
		}

		// Then: visit method called on visitor
		try {
			visitMethod.invoke(Mockito.verify(visitor), subject);
		} catch (InvocationTargetException | IllegalAccessException e) {
			Assertions.fail(e);
		}
	}
}
