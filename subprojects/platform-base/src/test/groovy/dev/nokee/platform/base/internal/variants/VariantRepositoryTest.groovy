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
package dev.nokee.platform.base.internal.variants

import dev.nokee.model.internal.AbstractRealizableDomainObjectRepositoryTest
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import dev.nokee.model.internal.RealizableDomainObjectRepository
import dev.nokee.platform.base.Variant

class VariantRepositoryTest extends AbstractRealizableDomainObjectRepositoryTest<Variant> implements VariantFixture {
	@Override
	protected RealizableDomainObjectRepository<Variant> newSubject(RealizableDomainObjectRealizer realizer) {
		return new VariantRepository(eventPublisher, realizer, providerFactory)
	}
}
