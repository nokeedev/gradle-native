/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public final class PluginRequirement {
	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@ExtendWith(Extension.class)
	@Repeatable(Requires.class)
	public @interface Require {
		Class<? extends Plugin<? extends Project>> type() default NONE.class;
		String id() default "";

		interface NONE extends Plugin<Project> {}
	}

	@Target({ElementType.TYPE, ElementType.METHOD})
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public @interface Requires {
		Require[] value();
	}

	static final class Extension implements BeforeEachCallback {
		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			AnnotationUtils.findRepeatableAnnotations(context.getElement(), PluginRequirement.Require.class).forEach(it -> applyPlugin(context, it));
			val allInstances = new ArrayList<>(context.getRequiredTestInstances().getAllInstances());
			Collections.reverse(allInstances);
			for (Object testInstance : allInstances) {
				AnnotationUtils.findRepeatableAnnotations(testInstance.getClass(), PluginRequirement.Require.class).forEach(it -> applyPlugin(context, it));
			}
		}

		private void applyPlugin(ExtensionContext context, Require require) {
			val project = (Project) context.getRequiredTestInstances().getAllInstances().stream().flatMap(it -> {
				val method = ReflectionUtils.findMethod(it.getClass(), "project");
				if (method.isPresent()) {
					return Stream.of(ReflectionUtils.invokeMethod(method.get(), it));
				} else {
					return Stream.empty();
				}
			}).findFirst().orElseThrow(RuntimeException::new);

			if (!require.id().isEmpty()) {
				System.out.println("Applying plugin " + require.id());
				project.getPluginManager().apply(require.id());
			} else {
				System.out.println("Applying plugin " + require.type());
				project.getPluginManager().apply(require.type());
			}
		}
	}
}
