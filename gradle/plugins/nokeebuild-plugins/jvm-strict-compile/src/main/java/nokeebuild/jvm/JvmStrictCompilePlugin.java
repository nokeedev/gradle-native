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
package nokeebuild.jvm;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.compile.JavaCompile;

import java.util.function.Function;

class JvmStrictCompilePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getTasks().withType(JavaCompile.class).configureEach(registerExtension("strictCompile", StrictJavaCompileExtension::new));
	}

	private static <SELF, T> Action<SELF> registerExtension(String name, Function<? super SELF, ? extends T> instanceMapper) {
		return self -> {
			((ExtensionAware) self).getExtensions().add(name, instanceMapper.apply(self));
		};
	}
}
