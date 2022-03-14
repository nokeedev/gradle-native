/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DomainObjectIdentityTest {
	private static final MyValue VALUE = new MyValue();
	private static final MyValue ALTERNATE_VALUE = new MyValue();
	private static final MyOtherValue OTHER_VALUE = new MyOtherValue();
	private final DomainObjectIdentity subject = DomainObjectIdentity.of(VALUE);

	@Test
	void returnsCurrentValueWhenExist() {
		assertThat(subject.get(MyValue.class), optionalWithValue(equalTo(VALUE)));
	}

	@Test
	void returnsSingleElementSetWhenValueExists() {
		assertThat(subject.getAll(MyValue.class), containsInAnyOrder(VALUE));
	}

	@Test
	void returnsEmptyWhenValueDoesNotExists() {
		assertThat(subject.get(MyOtherValue.class), emptyOptional());
	}

	@Test
	void returnsEmptySetWhenValueDoesNotExists() {
		assertThat(subject.getAll(MyOtherValue.class), emptyIterable());
	}

	@Test
	void canSetMultipleValuesUsingIterableAsObject() {
		assertThat(subject.with((Object) singletonList(ALTERNATE_VALUE)).getAll(MyValue.class),
			containsInAnyOrder(ALTERNATE_VALUE));
	}

	@Test
	void canSetMultipleValuesUsingIterable() {
		assertThat(subject.with(singletonList(ALTERNATE_VALUE)).getAll(MyValue.class),
			containsInAnyOrder(ALTERNATE_VALUE));
	}

	@Test
	void canReplaceCurrentValue() {
		assertThat(subject.with(ALTERNATE_VALUE).get(MyValue.class), optionalWithValue(equalTo(ALTERNATE_VALUE)));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnPublicMethods() {
		new NullPointerTester().testAllPublicInstanceMethods(subject);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNullsOnPublicStaticMethods() {
		new NullPointerTester().testAllPublicStaticMethods(DomainObjectIdentity.class);
	}

	@Test
	void canSetNonExistentValue() {
		assertThat(subject.with(OTHER_VALUE).get(MyOtherValue.class), optionalWithValue(equalTo(OTHER_VALUE)));
	}

	@Test
	void doesNotChangeOriginalSubjectWhenReplaceValue() {
		subject.with(ALTERNATE_VALUE);
		assertThat(subject.get(MyValue.class), optionalWithValue(equalTo(VALUE)));
	}

	@Test
	void canAddMoreValues() {
		assertThat(subject.plus(ALTERNATE_VALUE).getAll(MyValue.class), containsInAnyOrder(VALUE, ALTERNATE_VALUE));
	}

	@Test
	void canAddValueToNonExistentValue() {
		assertThat(subject.plus(OTHER_VALUE).get(MyOtherValue.class), optionalWithValue(equalTo(OTHER_VALUE)));
		assertThat(subject.plus(OTHER_VALUE).getAll(MyOtherValue.class), containsInAnyOrder(OTHER_VALUE));
	}

	@Test
	void doesNotChangeOriginalSubjectWhenAddMoreValues() {
		subject.plus(ALTERNATE_VALUE);
		assertThat(subject.get(MyValue.class), optionalWithValue(equalTo(VALUE)));
		assertThat(subject.getAll(MyValue.class), containsInAnyOrder(VALUE));
	}

	@Test
	void throwsExceptionWhenRequestSingleValueButHasMultipleValues() {
		assertThrows(IllegalArgumentException.class, () -> subject.plus(ALTERNATE_VALUE).get(MyValue.class));
	}

	@Test
	void doesNotDuplicateValues() {
		assertThat(subject.plus(VALUE).getAll(MyValue.class), containsInAnyOrder(VALUE));
	}

	private static final class MyValue {}
	private static final class MyOtherValue {}
}
