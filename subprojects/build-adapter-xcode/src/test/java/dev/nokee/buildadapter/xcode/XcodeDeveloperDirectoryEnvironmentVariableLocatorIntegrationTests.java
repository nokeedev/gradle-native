/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.DeveloperDirEnvironmentVariable;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDeveloperDirectoryEnvironmentVariableLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDeveloperDirectoryLocator;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.XcodeTestUtils.createValidXcodeInstallation;
import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapper.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doAlwaysReturnNull;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static java.nio.file.FileSystems.getDefault;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(TestDirectoryExtension.class)
class XcodeDeveloperDirectoryEnvironmentVariableLocatorIntegrationTests {
	@Nested
	class WhenNoDeveloperDirEnvironmentVariable {
		TestDouble<DeveloperDirEnvironmentVariable> developerDirEnvVar = newMock(DeveloperDirEnvironmentVariable.class)
			.when(callTo(method(DeveloperDirEnvironmentVariable::get)).then(doAlwaysReturnNull()));

		@Test
		void forwardsLocateToDelegate() {
			assertThat(forwarding(XcodeDeveloperDirectoryLocator.class, this::forWrapper),
				forwardsToDelegate(method(XcodeDeveloperDirectoryLocator::locate)));
		}

		private XcodeDeveloperDirectoryLocator forWrapper(XcodeDeveloperDirectoryLocator delegate) {
			return new XcodeDeveloperDirectoryEnvironmentVariableLocator(getDefault(), developerDirEnvVar.instance(), delegate);
		}
	}

	@Nested
	class WhenDeveloperDirEnvironmentVariablePointsToXcodeApplicationPackage {
		@TestDirectory Path testDirectory;
		TestDouble<XcodeDeveloperDirectoryLocator> delegate = newMock(XcodeDeveloperDirectoryLocator.class);
		TestDouble<DeveloperDirEnvironmentVariable> developerDirEnvVar = newMock(DeveloperDirEnvironmentVariable.class)
			.when(callTo(method(DeveloperDirEnvironmentVariable::get)).then(doReturn(() -> testDirectory.resolve("Xcode_13.2.app").toString())));
		XcodeDeveloperDirectoryEnvironmentVariableLocator subject = new XcodeDeveloperDirectoryEnvironmentVariableLocator(getDefault(), developerDirEnvVar.instance(), delegate.instance());
		Path result;

		@Nested
		class WhenXcodeInstallationExists {
			@BeforeEach
			void givenXcodeApplication() throws IOException {
				createValidXcodeInstallation(testDirectory.resolve("Xcode_13.2.app"));
				result = subject.locate();
			}

			@Test
			void returnsDeveloperDirectoryFrom() {
				assertThat(result, aFile(withAbsolutePath(endsWith("/Xcode_13.2.app/Content/Developer"))));
			}
		}

		@Nested
		class WhenXcodeInstallationDoesNotExists {
			@Test
			void forwardsLocateToDelegate() {
				assertThat(forwarding(XcodeDeveloperDirectoryLocator.class, this::forWrapper),
					forwardsToDelegate(method(XcodeDeveloperDirectoryLocator::locate)));
			}

			private XcodeDeveloperDirectoryLocator forWrapper(XcodeDeveloperDirectoryLocator delegate) {
				return new XcodeDeveloperDirectoryEnvironmentVariableLocator(getDefault(), developerDirEnvVar.instance(), delegate);
			}
		}
	}

	@Nested
	class WhenDeveloperDirEnvironmentVariablePointsToXcodeApplicationPackageContentDeveloperDirectory {
		@TestDirectory Path testDirectory;
		TestDouble<XcodeDeveloperDirectoryLocator> delegate = newMock(XcodeDeveloperDirectoryLocator.class);
		TestDouble<DeveloperDirEnvironmentVariable> developerDirEnvVar = newMock(DeveloperDirEnvironmentVariable.class)
			.when(callTo(method(DeveloperDirEnvironmentVariable::get)).then(doReturn(() -> testDirectory.resolve("Xcode_12.5.app/Content/Developer").toString())));
		XcodeDeveloperDirectoryEnvironmentVariableLocator subject = new XcodeDeveloperDirectoryEnvironmentVariableLocator(getDefault(), developerDirEnvVar.instance(), delegate.instance());
		Path result;

		@Nested
		class WhenXcodeInstallationExists {
			@BeforeEach
			void givenXcodeApplication() throws IOException {
				createValidXcodeInstallation(testDirectory.resolve("Xcode_12.5.app"));
				result = subject.locate();
			}

			@Test
			void returnsDeveloperDirectoryFrom() {
				assertThat(result, aFile(withAbsolutePath(endsWith("/Xcode_12.5.app/Content/Developer"))));
			}
		}

		@Nested
		class WhenXcodeInstallationDoesNotExists {
			@Test
			void forwardsLocateToDelegate() {
				assertThat(forwarding(XcodeDeveloperDirectoryLocator.class, this::forWrapper),
					forwardsToDelegate(method(XcodeDeveloperDirectoryLocator::locate)));
			}

			private XcodeDeveloperDirectoryLocator forWrapper(XcodeDeveloperDirectoryLocator delegate) {
				return new XcodeDeveloperDirectoryEnvironmentVariableLocator(getDefault(), developerDirEnvVar.instance(), delegate);
			}
		}
	}
}
