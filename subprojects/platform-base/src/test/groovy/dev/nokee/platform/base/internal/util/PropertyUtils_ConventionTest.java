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
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.platform.base.internal.util.PropertyUtils.convention;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyUtils_ConventionTest {
	interface ConventionTester<T> {
		Object SELF = new Object();

		PropertyUtils.ConventionAwareProperty<T> subject();

		T someValue();

		T someOtherValue();

		T subjectValue();

		void setValue(T value);

		@Test
		default void canSetConventionValue() {
			convention(someValue()).accept(SELF, subject());
			assertThat(subjectValue(), equalTo(someValue()));
			setValue(someOtherValue());
			assertThat(subjectValue(), equalTo(someOtherValue()));
			setValue(null);
			assertThat(subjectValue(), equalTo(someValue()));
		}

		@Test
		default void canMapValueToSetAsConventionFromOwner() {
			convention(it -> {
				assertThat(it, is(SELF));
				return someValue();
			}).accept(SELF, subject());
			assertThat(subjectValue(), equalTo(someValue()));
			setValue(someOtherValue());
			assertThat(subjectValue(), equalTo(someOtherValue()));
			setValue(null);
			assertThat(subjectValue(), equalTo(someValue()));
		}

		@Test
		default void throwsExceptionIfMapperFunctionIsNull() {
			assertThrows(NullPointerException.class, () -> convention((Function<? super Object, ?>) null));
		}
	}

	@Nested
	class GradleProperty implements ConventionTester<Object> {
		private final Property<Object> target = objectFactory().property(Object.class);
		private final PropertyUtils.ConventionAwareProperty<Object> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.ConventionAwareProperty<Object> subject() {
			return subject;
		}

		@Override
		public Object someValue() {
			return "har";
		}

		@Override
		public Object someOtherValue() {
			return "other-har";
		}

		@Override
		public Object subjectValue() {
			return target.get();
		}

		@Override
		public void setValue(Object value) {
			target.set(value);
		}
	}

	@Nested
	class GradleSetProperty implements ConventionTester<Iterable<? extends Object>> {
		private final SetProperty<Object> target = objectFactory().setProperty(Object.class);
		private final PropertyUtils.ConventionAwareProperty<Iterable<? extends Object>> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.ConventionAwareProperty<Iterable<? extends Object>> subject() {
			return subject;
		}

		@Override
		public Iterable<Object> someValue() {
			return ImmutableSet.of("jar");
		}

		@Override
		public Iterable<? extends Object> someOtherValue() {
			return ImmutableSet.of("other-jar");
		}

		@Override
		public Iterable<Object> subjectValue() {
			return target.get();
		}

		@Override
		public void setValue(Iterable<?> value) {
			target.set(value);
		}
	}

	@Nested
	class GradleListProperty implements ConventionTester<Iterable<? extends Object>> {
		private final ListProperty<Object> target = objectFactory().listProperty(Object.class);
		private final PropertyUtils.ConventionAwareProperty<Iterable<? extends Object>> subject = PropertyUtils.wrap(target);

		@Override
		public PropertyUtils.ConventionAwareProperty<Iterable<? extends Object>> subject() {
			return subject;
		}

		@Override
		public Iterable<Object> someValue() {
			return ImmutableList.of("kar");
		}

		@Override
		public Iterable<? extends Object> someOtherValue() {
			return ImmutableList.of("other-kar");
		}

		@Override
		public Iterable<Object> subjectValue() {
			return target.get();
		}

		@Override
		public void setValue(Iterable<?> value) {
			target.set(value);
		}
	}
}
