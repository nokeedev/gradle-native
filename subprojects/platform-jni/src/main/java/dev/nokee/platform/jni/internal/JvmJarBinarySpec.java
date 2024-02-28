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
package dev.nokee.platform.jni.internal;

import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.internal.BuildableComponentSpec;
import dev.nokee.platform.jni.JvmJarBinary;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;

public /*final*/ abstract class JvmJarBinarySpec extends ModelElementSupport implements JvmJarBinary, BuildableComponentSpec {
	@Inject
	public JvmJarBinarySpec() {
		getBuildDependencies().add(TaskDependencyUtils.of(getJarTask()));
	}

	@Override
	@NestedObject
	public abstract TaskProvider<Jar> getJarTask();

	@Override
	protected String getTypeName() {
		return "JVM JAR binary";
	}
}
