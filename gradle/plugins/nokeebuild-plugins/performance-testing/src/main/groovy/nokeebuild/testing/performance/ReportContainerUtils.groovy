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
package nokeebuild.testing.performance

import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.reporting.Report
import org.gradle.api.reporting.ReportContainer

class ReportContainerUtils {
	// This is not the report you are looking for...
	static <T extends Report> NamedDomainObjectProvider<T> create(ReportContainer<? extends Report> self, String name, Class<T> type, Object... args) {
		// protected is just an illusion in Groovy ;)
		self.add(type, name, *args)

		return self.named(name, type)
	}
}
