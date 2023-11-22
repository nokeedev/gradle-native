/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.testing.base.internal;

import dev.nokee.platform.base.Component;
import dev.nokee.testing.base.TestSuiteComponent;
import org.gradle.api.provider.Provider;

public interface TestSuiteComponentSpec extends TestSuiteComponent {
	@Override
	default TestSuiteComponentSpec testedComponent(Object component) {
		if (component instanceof Provider) {
			getTestedComponent().set(((Provider<?>) component).map(it -> {
				assert it instanceof Component;
				return (Component) it;
			}));
		} else if (component instanceof Component) {
			getTestedComponent().set((Component) component);
		} else {
			throw new UnsupportedOperationException("tested component must be a 'Component' type");
		}
		return this;
	}
}
