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

import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CodeablePBXNativeTargetTests {
	@Mock
	KeyedObject map;
	@InjectMocks
	CodeablePBXNativeTarget subject;

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXObjectNamesProvider.class)
	void checkGetName(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getName(), matchesObject(expectedValue));
		verify(map).tryDecode(name);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(XCConfigurationList.class)
	void checkGetBuildConfigurationList(XCConfigurationList expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getBuildConfigurationList(), matchesObject(expectedValue));
		verify(map).tryDecode(buildConfigurationList);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(listOf = {XCSwiftPackageProductDependency.class, XCSwiftPackageProductDependency.class, XCSwiftPackageProductDependency.class})
	void checkGetPackageProductDependencies(List<XCSwiftPackageProductDependency> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getPackageProductDependencies(), matchesIterable(expectedValue));
		verify(map).tryDecode(packageProductDependencies);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetBuildPhasesProvider.class)
	void checkGetBuildPhases(List<PBXBuildPhase> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getBuildPhases(), matchesIterable(expectedValue));
		verify(map).tryDecode(buildPhases);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetDependenciesProvider.class)
	void checkGetDependencies(List<PBXTargetDependency> expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getDependencies(), matchesIterable(expectedValue));
		verify(map).tryDecode(dependencies);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetProductNameProvider.class)
	void checkGetProductName(String expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getProductName(), matchesOptional(expectedValue));
		verify(map).tryDecode(productName);
	}

	@ParameterizedTest
	@ArgumentsSource(PBXObjectArgumentsProviders.PBXTargetProductTypesProvider.class)
	void checkGetProductType(ProductType expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getProductType(), matchesOptional(expectedValue));
		verify(map).tryDecode(productType);
	}

	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXFileReference.class)
	void checkGetProductReference(PBXFileReference expectedValue) {
		when(map.tryDecode(any())).thenReturn(expectedValue);
		assertThat(subject.getProductReference(), matchesOptional(expectedValue));
		verify(map).tryDecode(productReference);
	}

	@Test
	void forwardsEncodingToDelegate() {
		Codeable.EncodeContext context = mock(Codeable.EncodeContext.class);
		subject.encode(context);
		verify(map).encode(context);
	}

	@Test
	void forwardsIsaToDelegate() {
		subject.isa();
		verify(map).isa();
	}

	@Test
	void forwardsGlobalIdToDelegate() {
		subject.globalId();
		verify(map).globalId();
	}

	@Test
	void forwardsTryDecodeToDelegate() {
		CodingKey key = mock(CodingKey.class);
		subject.tryDecode(key);
		verify(map).tryDecode(key);
	}
}
