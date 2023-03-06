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

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDeveloperDirectoryLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDeveloperDirectorySystemApplicationsLocator;
import dev.nokee.internal.testing.testdoubles.TestDouble;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.XcodeTestUtils.createValidXcodeInstallation;
import static dev.nokee.internal.testing.FileSystemMatchers.absolutePath;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapper.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class XcodeDeveloperDirectorySystemApplicationsLocatorTests {
	FileSystem fileSystem = Jimfs.newFileSystem(Configuration.osX());

	@AfterEach
	void cleanupFileSystem() throws IOException {
		fileSystem.close();
	}

	@Nested
	class WhenSystemXcodeInstallationExists {
		TestDouble<XcodeDeveloperDirectoryLocator> delegate = newMock(XcodeDeveloperDirectoryLocator.class);
		XcodeDeveloperDirectorySystemApplicationsLocator subject = new XcodeDeveloperDirectorySystemApplicationsLocator(fileSystem, delegate.instance());
		Path result;

		@BeforeEach
		void givenSystemXcodeInstallation() throws IOException {
			createValidXcodeInstallation(fileSystem.getPath("/Applications/Xcode.app"));

			result = subject.locate();
		}

		@Test
		void returnsSystemXcodeApplicationPackageContentDeveloper() {
			assertThat(result, absolutePath(equalTo("/Applications/Xcode.app/Contents/Developer")));
		}
	}

	@Nested
	class WhenSystemXcodeInstallationDoesNotExists {
		@Test
		void forwardsLocateToDelegate() {
			assertThat(forwarding(XcodeDeveloperDirectoryLocator.class, this::forWrapper), forwardsToDelegate(method(XcodeDeveloperDirectoryLocator::locate)));
		}

		private XcodeDeveloperDirectoryLocator forWrapper(XcodeDeveloperDirectoryLocator delegate) {
			return new XcodeDeveloperDirectorySystemApplicationsLocator(fileSystem, delegate);
		}
	}
}
