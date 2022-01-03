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

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class TestSubjectsExtension implements BeforeEachCallback {
	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		for (Object testInstance : context.getRequiredTestInstances().getAllInstances()) {
			for (Field subjectField : AnnotationUtils.findAnnotatedFields(testInstance.getClass(), Subject.class, it -> true)) {
				Method m = ReflectionUtils.findMethods(testInstance.getClass(), it -> it.getName().equals("createSubject") && it.getParameterCount() == 0 && it.getGenericReturnType().equals(subjectField.getGenericType())).iterator().next();
				m.setAccessible(true);
				subjectField.setAccessible(true);
				subjectField.set(testInstance, m.invoke(testInstance));
			}
		}
	}
}
