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
import dev.nokee.model.internal.actions.DomainObjectIdentity;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
	void returnsEmptyWhenValueDoesNotExists() {
		assertThat(subject.get(MyOtherValue.class), emptyOptional());
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
	void doesNotChangeOriginalSubject() {
		subject.with(ALTERNATE_VALUE);
		assertThat(subject.get(MyValue.class), optionalWithValue(equalTo(VALUE)));
	}

	private static final class MyValue {}
	private static final class MyOtherValue {}
}
