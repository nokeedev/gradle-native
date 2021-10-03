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

package dev.nokee.internal.testing.file;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.util.function.Predicate;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.ReflectionUtils.isPrivate;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

public final class CleanupTestDirectoryExtension implements TestWatcher, BeforeAllCallback, BeforeEachCallback, ParameterResolver {
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(CleanupTestDirectoryExtension.class);
	private static final String KEY = "temp.dir";

	@Override
	public void beforeAll(ExtensionContext context) throws Exception {
		injectStaticFields(context, context.getRequiredTestClass());
	}

	@Override
	public void beforeEach(ExtensionContext context) throws Exception {
		context.getRequiredTestInstances().getAllInstances() //
			.forEach(instance -> injectInstanceFields(context, instance));
	}

	@Override
	public void testSuccessful(ExtensionContext context) {
		try {
			((AutoCloseable) context.getStore(NAMESPACE).get(KEY, TestDirectoryProvider.class)).close();
		} catch (Exception e) {
			ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	private void injectStaticFields(ExtensionContext context, Class<?> testClass) {
		injectFields(context, null, testClass, ReflectionUtils::isStatic);
	}

	private void injectInstanceFields(ExtensionContext context, Object instance) {
		injectFields(context, instance, instance.getClass(), ReflectionUtils::isNotStatic);
	}

	private void injectFields(ExtensionContext context, Object testInstance, Class<?> testClass,
							  Predicate<Field> predicate) {
		findAnnotatedFields(testClass, TestDirectory.class, predicate).forEach(field -> {
			assertValidFieldCandidate(field);
			try {
				makeAccessible(field).set(testInstance, getPathOrFileOrProvider(field.getType(), context));
			} catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	private void assertValidFieldCandidate(Field field) {
		assertSupportedType("field", field.getType());
		if (isPrivate(field)) {
			throw new ExtensionConfigurationException("@TestDirectory field [" + field + "] must not be private.");
		}
	}


	/**
	 * Determine if the {@link Parameter} in the supplied {@link ParameterContext}
	 * is annotated with {@link TestDirectory @TestDirectory}.
	 */
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(TestDirectory.class);
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException(
				"@TestDirectory is not supported on constructor parameters. Please use field injection instead.");
		}
		return annotated;
	}

	/**
	 * Resolve the current temporary directory for the {@link Parameter} in the
	 * supplied {@link ParameterContext}.
	 */
	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		assertSupportedType("parameter", parameterType);
		return getPathOrFileOrProvider(parameterType, extensionContext);
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != Path.class && type != File.class && type != TestDirectoryProvider.class) {
			throw new ExtensionConfigurationException("Can only resolve @TestDirectory " + target + " of type "
				+ Path.class.getName() + " or " + File.class.getName() + " or " + TestDirectoryProvider.class.getName() + " but was: " + type.getName());
		}
	}

	private Object getPathOrFileOrProvider(Class<?> type, ExtensionContext extensionContext) {
		TestDirectoryProvider provider = extensionContext.getStore(NAMESPACE) //
			.getOrComputeIfAbsent(KEY, key -> TestNameTestDirectoryProvider.newInstance(extensionContext.getRequiredTestMethod().getName(), extensionContext.getRequiredTestClass()), TestDirectoryProvider.class);

		if (type == Path.class) {
			return provider.getTestDirectory();
		} else if (type == File.class) {
			return provider.getTestDirectory().toFile();
		} else {
			return provider;
		}
	}
}
