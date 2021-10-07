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
package dev.nokee.platform.base.internal.binaries;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.AbstractDomainObjectView;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import groovy.lang.Closure;
import org.gradle.api.specs.Spec;

public final class BinaryViewImpl<T extends Binary> extends AbstractDomainObjectView<Binary, T> implements BinaryView<T>, DomainObjectView<T> {
	public BinaryViewImpl(DomainObjectIdentifier viewOwner, Class<T> viewElementType, BinaryRepository binaryRepository, BinaryConfigurer configurer, BinaryViewFactory binaryViewFactory) {
		super(viewOwner, viewElementType, binaryRepository, configurer, binaryViewFactory);
	}

	@Override
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		DomainObjectView.super.configureEach(closure);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		DomainObjectView.super.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super T> spec, @SuppressWarnings("rawtypes") Closure closure) {
		DomainObjectView.super.configureEach(spec, closure);
	}

	@Override
	public <S extends T> BinaryViewImpl<S> withType(Class<S> type) {
		return (BinaryViewImpl<S>) super.withType(type);
	}
}
