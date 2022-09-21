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
package dev.nokee.xcode;

import dev.nokee.samples.xcode.EmptyProject;
import dev.nokee.samples.xcode.GreeterApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class DefaultXCTargetReferenceIntegrationTests {
	@Mock XCLoader<Object, XCTargetReference> loader;
	@TempDir Path testDirectory;
	DefaultXCTargetReference subject;

	@BeforeEach
	void createSubject() {
		new GreeterApp().writeToProject(testDirectory);
		subject = new DefaultXCTargetReference(new DefaultXCProjectReference(testDirectory.resolve("GreeterApp.xcodeproj")), "GreeterApp");
	}

	@Test
	void forwardsLoadToSpecifiedLoader() {
		Mockito.when(loader.load(any())).thenReturn("loaded-value");
		assertThat(subject.load(loader), equalTo("loaded-value"));
		Mockito.verify(loader).load(subject);
	}
}
