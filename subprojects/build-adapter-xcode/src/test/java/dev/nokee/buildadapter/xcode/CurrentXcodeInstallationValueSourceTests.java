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
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeSelect;
import dev.nokee.buildadapter.xcode.internal.plugins.Xcodebuild;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Paths;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class CurrentXcodeInstallationValueSourceTests {
	@Mock Xcodebuild xcodebuild;
	@Mock XcodeSelect xcodeSelect;
	CurrentXcodeInstallationValueSource.Parameters parameters;
	CurrentXcodeInstallationValueSource subject;

	@BeforeEach
	void createSubject() {
		parameters = objectFactory().newInstance(CurrentXcodeInstallationValueSource.Parameters.class);
		subject = new CurrentXcodeInstallationValueSource(xcodeSelect, xcodebuild) {
			@Override
			public Parameters getParameters() {
				return parameters;
			}
		};
	}

	@Test
	void returnsXcodeInstallationWithQueriedVersionAndDeveloperDirectory() {
		Mockito.when(xcodeSelect.developerDirectory()).thenReturn(Paths.get("/opt/Xcode_13.2.1.app/Contents/Developer"));
		Mockito.when(xcodebuild.version()).thenReturn("13.2.1");
		assertThat(subject.obtain(),
			equalTo(new DefaultXcodeInstallation("13.2.1", Paths.get("/opt/Xcode_13.2.1.app/Contents/Developer"))));
	}
}
