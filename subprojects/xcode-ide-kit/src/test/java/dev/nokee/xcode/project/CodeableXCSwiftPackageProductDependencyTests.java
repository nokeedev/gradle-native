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
package dev.nokee.xcode.project;

import dev.nokee.internal.testing.testdoubles.MockitoBuilder;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference;
import static dev.nokee.xcode.project.CodeableXCSwiftPackageProductDependency.CodingKeys.productName;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeableXCSwiftPackageProductDependencyTests extends CodeableAdapterTester<CodeableXCSwiftPackageProductDependency> {
	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetProductNameProvider.class)
	void checkGetProductName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(productName)).then(doReturn(expectedValue))));

		assertThat(subject.getProductName(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCRemoteSwiftPackageReference.class)
	void checkGetPackageReference(XCRemoteSwiftPackageReference expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(packageReference)).then(doReturn(expectedValue))));

		assertThat(subject.getPackageReference(), matchesObject(expectedValue));
	}

	@Test
	void encodesIsaCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeIsaCodingKeys(delegate));
	}
}
