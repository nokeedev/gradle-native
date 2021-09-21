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
package dev.nokee.model;

import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.util.ExceptionUtils;

public abstract class AbstractDomainObjectViewConfigureEachTester<T> extends AbstractDomainObjectViewTester<T> {
	protected final <U> U when(ThrowingSupplier<U> executable) {
		U result = null;
		element("e0", getElementType());
		element("e1", getElementType()).get();
		element("e2", getSubElementType());
		element("e3", getSubElementType()).get();
		try {
			result = executable.get();
			element("e4", getElementType());
			element("e5", getElementType()).get();
			element("e6", getSubElementType());
			element("e7", getSubElementType()).get();
		} catch (Throwable throwable) {
			ExceptionUtils.throwAsUncheckedException(throwable);
		}
		return result;
	}
}
