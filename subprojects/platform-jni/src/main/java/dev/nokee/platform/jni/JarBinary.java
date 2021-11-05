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
package dev.nokee.platform.jni;

import dev.nokee.platform.base.Binary;
import org.gradle.api.Buildable;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

/**
 * Configuration for JAR binary.
 *
 * @since 0.3
 */
public interface JarBinary extends Binary, Buildable {
	/**
	 * Returns the {@link Jar} task for this binary.
	 *
	 * @return a provider to the {@link Jar} task.
	 */
	TaskProvider<Jar> getJarTask();
}
