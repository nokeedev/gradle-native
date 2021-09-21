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

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.publish.bintray.internal.BintrayPackageName.fromRepositoryDeclaration;
import static dev.nokee.publish.bintray.internal.BintrayPackageName.of;
import static dev.nokee.publish.bintray.internal.BintrayTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(BintrayPackageName.class)
class BintrayPackageNameTest {
	@Test
	void doesNotThrowExceptionWhenCreatingBintrayPackageNameFromRepositoryWithMissingExtensionProperty() {
		assertDoesNotThrow(() -> fromRepositoryDeclaration(repository(withoutPackageName())));
	}

	@Test
	void throwsExceptionWhenQueryingBintrayPackageNameFromRepositoryWithMissingExtensionProperty() {
		val ex = assertThrows(IllegalStateException.class, () -> fromRepositoryDeclaration(repository(withoutPackageName())).get());
		assertThat(ex.getMessage(), equalTo("When publishing to Bintray repositories, please specify the package name using an extra property on the repository, e.g. ext.packageName = 'foo'."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(BintrayPackageName.class);
	}

	@Test
	void canQueryBintrayPackageNameFromRepositoryWithExtensionProperty() {
		assertThat(fromRepositoryDeclaration(repository(withPackageName("foo"))).get(), equalTo("foo"));
	}

	@Test
	void canCreateBintrayPackageNameFromStringInstance() {
		assertThat(of("foo").get(), equalTo("foo"));
	}
}
