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
package dev.gradleplugins.dockit.manual;

import dev.gradleplugins.dockit.common.TaskNameFactory;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

public abstract class UserManualExtension implements Named, HasPublicType, ExtensionAware, TaskNameFactory {
	private final Names names = new Names("userManual");

	@Inject
	public UserManualExtension() {
		getExtensions().add("sourceDirectory", getManualDirectory());
	}

	public String getName() {
		return "userManual";
	}

	@Override
	public TypeOf<?> getPublicType() {
		return TypeOf.typeOf(UserManualExtension.class);
	}

	public abstract DirectoryProperty getManualDirectory();

	@Override
	public String taskName(String name) {
		return names.taskName(name);
	}

	@Override
	public String taskName(String verb, String object) {
		return names.taskName(verb, object);
	}

	public static void userManual(Project project, Action<? super UserManualExtension> action) {
		action.execute((UserManualExtension) project.getExtensions().getByName("userManual"));
	}

	public static Provider<UserManualExtension> userManual(Project project) {
		return project.provider(() -> (UserManualExtension) project.getExtensions().getByName("userManual"));
	}
}
