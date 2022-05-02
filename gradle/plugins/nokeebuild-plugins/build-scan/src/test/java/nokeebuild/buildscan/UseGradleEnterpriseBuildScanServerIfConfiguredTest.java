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
import org.mockito.junit.jupiter.MockitoExtension;

import static nokeebuild.buildscan.UseGradleEnterpriseBuildScanServerIfConfigured.GRADLE_ENTERPRISE_URL_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UseGradleEnterpriseBuildScanServerIfConfiguredTest {
	@Mock private UseGradleEnterpriseBuildScanServerIfConfigured.Parameters gradleEnterprise;
	@InjectMocks private UseGradleEnterpriseBuildScanServerIfConfigured subject;
	@Mock private BuildScanExtension buildScan;

	@Test
	void usesServerUrl() {
		when(gradleEnterprise.serverUrl()).thenReturn("https://my-company.gradle.com/");
		subject.execute(buildScan);
		verify(buildScan).setServer("https://my-company.gradle.com/");
	}

	@Test
	void canUseNullServerUrl__akaPublicInstance() {
		when(gradleEnterprise.serverUrl()).thenReturn(null);
		subject.execute(buildScan);
		verify(buildScan).setServer(null);
	}

	@Test
	void hasGradleEnterpriseUrlSystemProperty() {
		assertEquals("gradle.enterprise.url", GRADLE_ENTERPRISE_URL_PROPERTY_NAME);
	}
}
