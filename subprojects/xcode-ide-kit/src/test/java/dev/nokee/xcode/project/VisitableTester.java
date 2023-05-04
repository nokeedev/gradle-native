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
import dev.nokee.xcode.objects.Visitable;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public interface VisitableTester<V> {
	Visitable<V> newSubject();

	@Test
	default void canVisit() {
		@SuppressWarnings({"UnstableApiUsage", "unchecked"})
		val visitorType = (Class<V>) new TypeToken<V>(getClass()) {}.getRawType();
		val visitor = Mockito.mock(visitorType);
		val subject = newSubject();

		subject.accept(visitor);

		val visitMethod = Arrays.stream(visitorType.getDeclaredMethods())
			// find a method that takes only one parameter of a compatible type with the subject
			.filter(it -> it.getParameterTypes().length == 1 && it.getParameterTypes()[0].isInstance(subject))
			.collect(MoreCollectors.onlyElement());
		try {
			visitMethod.invoke(Mockito.verify(visitor), subject);
		} catch (InvocationTargetException | IllegalAccessException e) {
			Assertions.fail(e);
		}
	}
}
