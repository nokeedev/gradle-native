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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.util.PropertyUtils.set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtils_SetTest {
	interface SetTester<T> {
		Object SELF = new Object();

		PropertyUtils.SetAwareProperty<T> subject();

		T someValue();

		T subjectValue();

		@Test
		default void canSetValue() {
			set(someValue()).accept(SELF, subject());
			assertThat(subjectValue(), equalTo(someValue()));
		}

		@Test
		default void canMapValueToSetFromOwner() {
			set(it -> {
				assertThat(it, is(SELF));
				return someValue();
			}).accept(SELF, subject());
			assertThat(subjectValue(), equalTo(someValue()));
		}

		@Test
		default void throwsExceptionIfMapperFunctionIsNull() {
			assertThrows(NullPointerException.class, () -> set((Function<? super Object, ?>) null));
		}
	}

	@Nested
	class GradlePropertyTest implements SetTester<Object> {
		private final Property<Object> target = objectFactory().property(Object.class);
		private final PropertyUtils.SetAwareProperty<Object> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.SetAwareProperty<Object> subject() {
			return subject;
		}

		@Override
		public Object someValue() {
			return "foo";
		}

		@Override
		public Object subjectValue() {
			return target.get();
		}
	}

	@Nested
	class GradleSetPropertyTest implements SetTester<Iterable<?>> {
		private final SetProperty<Object> target = objectFactory().setProperty(Object.class);
		private final PropertyUtils.SetAwareProperty<Iterable<?>> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.SetAwareProperty<Iterable<?>> subject() {
			return subject;
		}

		@Override
		public Set<Object> someValue() {
			return ImmutableSet.of("bar");
		}

		@Override
		public Set<Object> subjectValue() {
			return target.get();
		}
	}

	@Nested
	class GradleListPropertyTest implements SetTester<Iterable<?>> {
		private final ListProperty<Object> target = objectFactory().listProperty(Object.class);
		private final PropertyUtils.SetAwareProperty<Iterable<?>> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.SetAwareProperty<Iterable<?>> subject() {
			return subject;
		}

		@Override
		public List<Object> someValue() {
			return ImmutableList.of("far");
		}

		@Override
		public List<Object> subjectValue() {
			return target.get();
		}
	}

	@Nested
	class GradleConfigurableFileCollectionTest implements SetTester<Iterable<File>> {
		private final ConfigurableFileCollection target = objectFactory().fileCollection();
		private final PropertyUtils.SetAwareProperty<Iterable<File>> subject = PropertyUtils.wrap(target);
		private final Iterable<File> someValue = assertDoesNotThrow(() -> ImmutableSet.of(File.createTempFile("dar", "txt")));

		@Override
		public PropertyUtils.SetAwareProperty<Iterable<File>> subject() {
			return subject;
		}

		@Override
		public Iterable<File> someValue() {
			return someValue;
		}

		@Override
		public Iterable<File> subjectValue() {
			return target.getFiles();
		}
	}

	@Nested
	class SetterMethodTest implements SetTester<Object> {
		private final SetterObject target = new SetterObject();
		private final PropertyUtils.SetAwareProperty<Object> subject = PropertyUtils.wrap(target::setValue);

		@Override
		public PropertyUtils.SetAwareProperty<Object> subject() {
			return subject;
		}

		@Override
		public Object someValue() {
			return "gar";
		}

		@Override
		public Object subjectValue() {
			return target.value;
		}
	}
	private static final class SetterObject {
		Object value = null;

		void setValue(Object value) {
			this.value = value;
		}
	}
}
