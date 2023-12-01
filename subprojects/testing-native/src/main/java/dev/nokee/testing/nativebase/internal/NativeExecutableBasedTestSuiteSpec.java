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

package dev.nokee.testing.nativebase.internal;

import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.nativebase.internal.NativeExecutableBinarySpec;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.nativeplatform.test.tasks.RunTestExecutable;

// TODO: May limit to variant, this seems to be very variant specific
public interface NativeExecutableBasedTestSuiteSpec extends ModelElement, ExtensionAware {
	@NestedObject
	TaskProvider<RunTestExecutable> getRunTask();

	// Note: rely on NativeComponentBasePlugin capability that create and register binaries based on linkage
	@SuppressWarnings("unchecked")
	default NamedDomainObjectProvider<NativeExecutableBinarySpec> getExecutable() {
		return (NamedDomainObjectProvider<NativeExecutableBinarySpec>) getExtensions().getByName("executable");
	}

	ConfigurableFileCollection getComponentObjects();
}
