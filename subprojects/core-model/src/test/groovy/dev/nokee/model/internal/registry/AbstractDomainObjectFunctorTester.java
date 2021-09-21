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
package dev.nokee.model.internal.registry;

import dev.nokee.model.internal.core.ModelNode;
import org.junit.platform.commons.util.ExceptionUtils;

import java.lang.reflect.InvocationTargetException;

public abstract class AbstractDomainObjectFunctorTester<F> {
	protected abstract <T> F createSubject(Class<T> type);
	protected abstract <T> F createSubject(Class<T> type, ModelNode node);

	protected final <R> R invoke(Object target, String method, Class[] parameterTypes, Object[] arguments) {
		try {
			return (R) target.getClass().getMethod(method, parameterTypes).invoke(target, arguments);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			return (R) ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	static class MyType {
		private String value;

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}
	static class MyOtherType {}
	interface WrongType {}
}
