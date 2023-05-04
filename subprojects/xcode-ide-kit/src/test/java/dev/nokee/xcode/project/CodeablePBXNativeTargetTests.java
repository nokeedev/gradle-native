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
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;

import java.util.List;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.buildConfigurationList;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.buildPhases;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.dependencies;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.packageProductDependencies;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.productName;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.productReference;
import static dev.nokee.xcode.project.CodeablePBXNativeTarget.CodingKeys.productType;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesIterable;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXNativeTargetTests extends CodeableAdapterTester<CodeablePBXNativeTarget> implements VisitableTester<PBXTarget.Visitor<?>> {
	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(name)).then(doReturn(expectedValue))));

		assertThat(subject.getName(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCConfigurationList.class)
	void checkGetBuildConfigurationList(XCConfigurationList expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(buildConfigurationList)).then(doReturn(expectedValue))));

		assertThat(subject.getBuildConfigurationList(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {XCSwiftPackageProductDependency.class, XCSwiftPackageProductDependency.class, XCSwiftPackageProductDependency.class})
	void checkGetPackageProductDependencies(List<XCSwiftPackageProductDependency> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(packageProductDependencies)).then(doReturn(expectedValue))));

		assertThat(subject.getPackageProductDependencies(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetBuildPhasesProvider.class)
	void checkGetBuildPhases(List<PBXBuildPhase> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(buildPhases)).then(doReturn(expectedValue))));

		assertThat(subject.getBuildPhases(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetDependenciesProvider.class)
	void checkGetDependencies(List<PBXTargetDependency> expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(dependencies)).then(doReturn(expectedValue))));

		assertThat(subject.getDependencies(), matchesIterable(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetProductNameProvider.class)
	void checkGetProductName(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(productName)).then(doReturn(expectedValue))));

		assertThat(subject.getProductName(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetProductTypesProvider.class)
	void checkGetProductType(ProductType expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(productType)).then(doReturn(expectedValue))));

		assertThat(subject.getProductType(), matchesOptional(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXFileReference.class)
	void checkGetProductReference(PBXFileReference expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(productReference)).then(doReturn(expectedValue))));

		assertThat(subject.getProductReference(), matchesOptional(expectedValue));
	}

	@Test
	void encodesIsaCodingKeyOnNewInstance() {
		val delegate = MockitoBuilder.newAlwaysThrowingMock(KeyedObject.class);
		assertThat(newSubjectInstance(delegate), encodeIsaCodingKeys(delegate));
	}
}
