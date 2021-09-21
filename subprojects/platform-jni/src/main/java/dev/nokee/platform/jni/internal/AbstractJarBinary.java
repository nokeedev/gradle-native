/*
 * Copyright 2020 the original author or authors.
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

import com.google.common.collect.ImmutableSet;
import dev.nokee.platform.base.Binary;
import lombok.EqualsAndHashCode;
import org.gradle.api.Buildable;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

@EqualsAndHashCode
public abstract class AbstractJarBinary implements Binary, Buildable {
	private final TaskProvider<Jar> jarTask;

	public AbstractJarBinary(TaskProvider<Jar> jarTask) {
		this.jarTask = jarTask;
	}

	public TaskProvider<Jar> getJarTask() {
		return jarTask;
	}

	public Provider<RegularFile> getArchiveFile() {
		return this.jarTask.flatMap(Jar::getArchiveFile);
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return task -> ImmutableSet.of(jarTask.get());
	}
}
