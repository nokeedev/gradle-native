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
package dev.nokee.gradle.internal.testers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

import static dev.nokee.gradle.internal.util.ClassTestUtils.createInstance;
import static dev.nokee.gradle.internal.util.ClassTestUtils.createInstances;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MethodCallForwardingTester {
	protected static final CapturingInvocationHandler handler = new CapturingInvocationHandler();

	public abstract Object subject();

	public abstract Stream<Method> allMethodsUnderTest();

	@BeforeEach
	void resetHandler() {
		handler.resetCapture();
	}

	@ParameterizedTest
	@MethodSource("allMethodsUnderTest")
	void forwardsAllMethodsToDelegate(Method method) throws InvocationTargetException, IllegalAccessException {
		Object returnObject = createInstance(method.getReturnType());
		Object[] args = createInstances(method.getParameterTypes());
		handler.returnWhenInvoked(returnObject);
		Object actualReturnValue = method.invoke(subject(), args);
		assertEquals(1, handler.getCapturedInvocations().size());
		assertEquals(method.getName(), handler.getCapturedInvocations().get(0).getTarget().getName());
		assertArrayEquals(method.getParameterTypes(), handler.getCapturedInvocations().get(0).getTarget().getParameterTypes());
		assertArrayEquals(args, handler.getCapturedInvocations().get(0).getArgs());
		if (!method.getReturnType().equals(void.class)) {
			if (Proxy.class.isAssignableFrom(returnObject.getClass())) {
				assertSame(returnObject, actualReturnValue);
			} else {
				assertEquals(returnObject, actualReturnValue);
			}
		}
	}
}
