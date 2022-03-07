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
package nokeedocs;

import net.nokeedev.jbake.JBakeExtension;
import net.nokeedev.jbake.JBakeExtensionFactory;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

final class RegisterJBakeExtension<T extends ExtensionAware & Named> implements Action<T> {
	private final JBakeExtensionFactory factory;

	public RegisterJBakeExtension(Project project) {
		this.factory = JBakeExtensionFactory.forProject(project);
	}

	@Override
	public void execute(T target) {
		target.getExtensions().add(JBakeExtension.JBAKE_EXTENSION_NAME, factory.create(target.getName()));
	}
}
