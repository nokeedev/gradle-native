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
package nokeebuild.buildscan;

import com.gradle.scan.plugin.BuildScanExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadInBackgroundOnlyOnLocalBuildEnvironmentTest {
	@Mock private UploadInBackgroundOnlyOnLocalBuildEnvironment.Parameters parameters;
	@InjectMocks private UploadInBackgroundOnlyOnLocalBuildEnvironment subject;
	@Mock private BuildScanExtension buildScan;

	@Test
	void enablesUploadInBackgroundOnLocalBuildEnvironment() {
		when(parameters.isLocalBuildEnvironment()).thenReturn(true);
		subject.execute(buildScan);
		Mockito.verify(buildScan).setUploadInBackground(true);
	}

	@Test
	void disablesUploadInBackgroundOnNonLocalBuildEnvironment() {
		when(parameters.isLocalBuildEnvironment()).thenReturn(false);
		subject.execute(buildScan);
		Mockito.verify(buildScan).setUploadInBackground(false);
	}
}
