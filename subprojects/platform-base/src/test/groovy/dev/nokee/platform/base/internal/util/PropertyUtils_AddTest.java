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
package dev.nokee.platform.base.internal.util;

import lombok.val;
import org.apache.commons.lang3.mutable.MutableInt;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.SetProperty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.function.Function;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.util.PropertyUtils.add;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtils_AddTest {
	interface AddTester<T> {
		Object SELF = new Object();

		PropertyUtils.CollectionAwareProperty<T> subject();

		T someValue();

		T someOtherValue();

		Iterable<T> subjectValue();

		@Test
		default void canAddValue() {
			add(someValue()).accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue()));
		}

		@Test
		default void canAddMultipleValuesSequentially() {
			add(someValue()).andThen(add(someOtherValue())).accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue(), someOtherValue()));
		}

		@Test
		default void canMapValueToAddFromOwner() {
			add(it -> {
				assertThat(it, is(SELF));
				return someValue();
			}).accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue()));
		}

		@Test
		default void callingTheActionMultipleTimeMapTheOwnerMultipleTime() {
			val callCount = new MutableInt(0);
			val action = add(it -> {
				assertThat(it, is(SELF));
				callCount.increment();
				return someValue();
			});
			action.accept(SELF, subject());
			action.accept(SELF, subject());
			assertThat(callCount.intValue(), is(2));
		}

		@Test
		default void throwsExceptionIfMapperFunctionIsNull() {
			assertThrows(NullPointerException.class, () -> add((Function<? super Object, ?>) null));
		}
	}

	@Nested
	class GradleSetProperty implements AddTester<Object> {
		private final SetProperty<Object> target = objectFactory().setProperty(Object.class);
		private final PropertyUtils.CollectionAwareProperty<Object> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.CollectionAwareProperty<Object> subject() {
			return subject;
		}

		@Override
		public Object someValue() {
			return "jar";
		}

		@Override
		public Object someOtherValue() {
			return "other-jar";
		}

		@Override
		public Iterable<Object> subjectValue() {
			return target.get();
		}
	}

	@Nested
	class GradleListProperty implements AddTester<Object> {
		private final ListProperty<Object> target = objectFactory().listProperty(Object.class);
		private final PropertyUtils.CollectionAwareProperty<Object> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.CollectionAwareProperty<Object> subject() {
			return subject;
		}

		@Override
		public Object someValue() {
			return "kar";
		}

		@Override
		public Object someOtherValue() {
			return "other-kar";
		}

		@Override
		public Iterable<Object> subjectValue() {
			return target.get();
		}

		@Test
		void callingTheActionMultipleTimeAddsValueMultipleTime() {
			val action = add(someValue());
			action.accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue()));
			action.accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue(), someValue()));
		}

		@Test
		void callingTheActionMultipleTimeMapAddsMappedValueMultipleTime() {
			val action = add(it -> {
				assertThat(it, is(SELF));
				return someValue();
			});
			action.accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue()));
			action.accept(SELF, subject());
			assertThat(subjectValue(), contains(someValue(), someValue()));
		}
	}

	@Nested
	class GradleFileCollectionProperty implements AddTester<File> {
		private final ConfigurableFileCollection target = objectFactory().fileCollection();
		private final PropertyUtils.CollectionAwareProperty<File> subject = PropertyUtils.wrap(target);
		private final File someValue = assertDoesNotThrow(() -> File.createTempFile("dar", ".txt"));
		private final File someOtherValue = assertDoesNotThrow(() -> File.createTempFile("other-dar", ".txt"));

		@Override
		public PropertyUtils.CollectionAwareProperty<File> subject() {
			return subject;
		}

		@Override
		public File someValue() {
			return someValue;
		}

		@Override
		public File someOtherValue() {
			return someOtherValue;
		}

		@Override
		public Iterable<File> subjectValue() {
			return target.getFiles();
		}
	}
}
