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

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.StubBuilder.WithArguments.args;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.xcode.project.CodeablePBXContainerItemProxy.CodingKeys.containerPortal;
import static dev.nokee.xcode.project.CodeablePBXContainerItemProxy.CodingKeys.proxyType;
import static dev.nokee.xcode.project.CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString;
import static dev.nokee.xcode.project.CodeablePBXContainerItemProxy.CodingKeys.remoteInfo;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesObject;
import static dev.nokee.xcode.project.PBXObjectMatchers.matchesOptional;
import static org.hamcrest.MatcherAssert.assertThat;

class CodeablePBXContainerItemProxyTests extends CodeableAdapterTester<CodeablePBXContainerItemProxy> {
	@ParameterizedTest
	@NullSource
	@MockitoSource(PBXContainerItemProxy.ContainerPortal.class)
	void checkGetContainerPortal(PBXContainerItemProxy.ContainerPortal expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(containerPortal)).then(doReturn(expectedValue))));

		assertThat(subject.getContainerPortal(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"407EB587257A418900686B1F"})
	void checkGetRemoteGlobalIDString(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(remoteGlobalIDString)).then(doReturn(expectedValue))));

		assertThat(subject.getRemoteGlobalIDString(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@EnumSource(PBXContainerItemProxy.ProxyType.class)
	void checkGetProxyType(PBXContainerItemProxy.ProxyType expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(proxyType)).then(doReturn(expectedValue))));

		assertThat(subject.getProxyType(), matchesObject(expectedValue));
	}

	@ParameterizedTest
	@NullSource
	@ValueSource(strings = {"FooBar"})
	void checkGetRemoteInfo(String expectedValue) {
		val subject = newSubject(it -> it.when(callTo(method(KeyedObject_tryDecode())).with(args(remoteInfo)).then(doReturn(expectedValue))));

		assertThat(subject.getRemoteInfo(), matchesOptional(expectedValue));
	}
}
