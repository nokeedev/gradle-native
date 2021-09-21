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

import dev.nokee.model.internal.core.ModelNodes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelPath.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public abstract class DomainObjectViewTester<T> {
	protected abstract TestViewGenerator<T> getSubjectGenerator();

	@Nested
	class CanConfigureEach extends DomainObjectViewConfigureEachTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanConfigureEachUsingClass extends DomainObjectViewConfigureEachUsingClassTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanConfigureEachUsingSpec extends DomainObjectViewConfigureEachUsingSpecTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanGetElements extends DomainObjectViewElementsTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanFlatMap extends DomainObjectViewFlatMapTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanMap extends DomainObjectViewMapTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanFilter extends DomainObjectViewFilterTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanGet extends DomainObjectViewGetTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanWhenElementKnown extends DomainObjectViewWhenElementKnownTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanWhenElementKnownUsingClass extends DomainObjectViewWhenElementKnownUsingClassTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}
	}

	@Nested
	class CanAccessBackingNode extends AbstractDomainObjectViewTester<T> {
		@Override
		protected TestViewGenerator<T> getSubjectGenerator() {
			return DomainObjectViewTester.this.getSubjectGenerator();
		}

		@Test
		void canGetBackingNodeFromInstance() {
			assertThat(ModelNodes.of(createSubject("myObjects")), equalTo(getModelLookup().get(path("myObjects"))));
		}
	}
}
