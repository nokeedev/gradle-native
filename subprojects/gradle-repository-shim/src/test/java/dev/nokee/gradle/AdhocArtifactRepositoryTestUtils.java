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
package dev.nokee.gradle;

import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static dev.nokee.utils.ConfigurationUtils.configureAsResolvable;
import static org.hamcrest.Matchers.equalTo;

public final class AdhocArtifactRepositoryTestUtils {
	private static int sequenceNumber = 1;

	public static FileCollection queryAndIgnoreFailures(Project project, Object notation) {
		val result = project.getConfigurations().create("test" + sequenceNumber++, configureAsResolvable());
		result.getDependencies().add(project.getDependencies().create(notation));
		try {
			result.resolve();
		} catch (RuntimeException e) {
			// ignore failures
		}
		return result;
	}

	public static Matcher<AdhocComponentSupplierDetails> forId(String coordinate) {
		return new FeatureMatcher<AdhocComponentSupplierDetails, String>(equalTo(coordinate), "", "") {
			@Override
			protected String featureValueOf(AdhocComponentSupplierDetails actual) {
				return actual.getId().getGroup() + ":" + actual.getId().getModule() + ":" + actual.getId().getVersion();
			}
		};
	}

	public static Matcher<AdhocComponentListerDetails> forModule(String coordinate) {
		return new FeatureMatcher<AdhocComponentListerDetails, String>(equalTo(coordinate), "", "") {
			@Override
			protected String featureValueOf(AdhocComponentListerDetails actual) {
				return actual.getModuleIdentifier().getGroup() + ":" + actual.getModuleIdentifier().getName();
			}
		};
	}
}
