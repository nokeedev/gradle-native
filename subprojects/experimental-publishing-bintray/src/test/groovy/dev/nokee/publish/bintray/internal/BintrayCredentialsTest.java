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
package dev.nokee.publish.bintray.internal;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.publish.bintray.internal.BintrayCredentials.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(BintrayCredentials.class)
class BintrayCredentialsTest {
	@Test
	void invalidCredentialsAlwaysThrowsExceptionWhenGettingUser() {
		val ex = assertThrows(IllegalStateException.class, () -> invalidBintrayCredentials().getBintrayUser());
		assertThat(ex.getMessage(), equalTo("When publishing to Bintray repositories, please specify the credentials using the credentials DSL on the repository, e.g. credentials { }."));
	}

	@Test
	void invalidCredentialsAlwaysThrowsExceptionWhenGettingKey() {
		val ex = assertThrows(IllegalStateException.class, () -> invalidBintrayCredentials().getBintrayKey());
		assertThat(ex.getMessage(), equalTo("When publishing to Bintray repositories, please specify the credentials using the credentials DSL on the repository, e.g. credentials { }."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(invalidBintrayCredentials(), invalidBintrayCredentials())
			.addEqualityGroup(of("foo", "bar"))
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(BintrayCredentials.class);
	}

	@Test
	void canCreateCredentialsUsingBuilder() {
		val credentials = builder().withUser(ofInstance("a")).withKey(ofInstance("b")).withParent(invalidBintrayCredentials()).build();
		assertThat(credentials.getBintrayUser(), equalTo("a"));
		assertThat(credentials.getBintrayKey(), equalTo("b"));
	}
}
