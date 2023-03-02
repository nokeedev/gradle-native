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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.plugins.CurrentXcodeInstallationValueSource;
import dev.nokee.buildadapter.xcode.internal.plugins.DefaultXcodeInstallation;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDeveloperDirectoryLocator;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeVersionFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.internal.testing.testdoubles.Answers.doReturn;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.any;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.callTo;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class CurrentXcodeInstallationValueSourceTests {
	XcodeDeveloperDirectoryLocator developerDirLocator = newMock(XcodeDeveloperDirectoryLocator.class) //
		.when(callTo(method(XcodeDeveloperDirectoryLocator::locate)).then(doReturn(Paths.get("/opt/Xcode_13.2.1.app/Contents/Developer")))) //
		.instance();
	XcodeVersionFinder versionFinder = newMock(XcodeVersionFinder.class) //
		.when(any(callTo(method(XcodeVersionFinder::find))).then(doReturn("13.2.1"))) //
		.instance();
	CurrentXcodeInstallationValueSource.Parameters parameters;
	CurrentXcodeInstallationValueSource subject;

	@BeforeEach
	void createSubject() {
		parameters = objectFactory().newInstance(CurrentXcodeInstallationValueSource.Parameters.class);
		subject = new CurrentXcodeInstallationValueSource(developerDirLocator, versionFinder) {
			@Override
			public Parameters getParameters() {
				return parameters;
			}
		};
	}

	@Test
	void returnsXcodeInstallationWithQueriedVersionAndDeveloperDirectory() {
		assertThat(subject.obtain(),
			equalTo(new DefaultXcodeInstallation("13.2.1", Paths.get("/opt/Xcode_13.2.1.app/Contents/Developer"))));
	}
}
