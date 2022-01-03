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
package dev.nokee.internal.testing.junit.jupiter;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.util.ProjectTestUtils;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

public final class GradleTestExtension implements BeforeEachCallback {
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		for (Object testInstance : context.getRequiredTestInstances().getAllInstances()) {
			for (Field field : AnnotationUtils.findAnnotatedFields(testInstance.getClass(), GradleProject.class, it -> true)) {
				field.setAccessible(true);
				field.set(testInstance, createProject(context));
			}
		}
	}

	private static Project createProject(ExtensionContext context) {
		val project = ProjectTestUtils.rootProject();

		AnnotationUtils.findRepeatableAnnotations(context.getElement(), PluginRequirement.Require.class).forEach(it -> applyPlugin(project, it));
		val allInstances = new ArrayList<>(context.getRequiredTestInstances().getAllInstances());
		Collections.reverse(allInstances);
		for (Object testInstance : allInstances) {
			AnnotationUtils.findRepeatableAnnotations(testInstance.getClass(), PluginRequirement.Require.class).forEach(it -> applyPlugin(project, it));
		}

		return project;
	}

	private static void applyPlugin(Project project, PluginRequirement.Require require) {
		if (!require.id().isEmpty()) {
			System.out.println("Applying plugin " + require.id());
			project.getPluginManager().apply(require.id());
		} else {
			System.out.println("Applying plugin " + require.type());
			project.getPluginManager().apply(require.type());
		}
	}
}
