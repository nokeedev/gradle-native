/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.model.internal;

import com.google.common.reflect.TypeToken;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Namer;
import org.gradle.api.model.ObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class ListeningModelMapStrategy<ElementType> extends ForwardingModelMapStrategy<ElementType> {
	private final Map<String, DomainObjectSet<ModelObjectIdentity<?>>> nameToIdentities = new HashMap<>();
	private final Listener listener;
	private final ModelMapStrategy<ElementType> delegate;
	private final Function<String, DomainObjectSet<ModelObjectIdentity<?>>> factory;

	@SuppressWarnings("unchecked")
	public ListeningModelMapStrategy(Namer<ElementType> namer, ObjectFactory objects, Listener listener, ModelMapStrategy<ElementType> delegate) {
		this.listener = listener;
		this.delegate = delegate;

		this.factory = __ -> {
			return (DomainObjectSet<ModelObjectIdentity<?>>) objects.domainObjectSet(new TypeToken<ModelObjectIdentity<?>>() {}.getRawType());
		};

		delegate.configureEach(it -> {
			nameToIdentities.computeIfAbsent(namer.determineName(it), factory).all(listener::onRealizing);
		});
		delegate.whenElementFinalized(it -> {
			nameToIdentities.computeIfAbsent(namer.determineName(it), factory).all(e -> {
				listener.onRealized(e);
				listener.onFinalizing(e);
			});
		});
	}

	@Override
	public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
		listener.onRegister(identity);
		nameToIdentities.computeIfAbsent(identity.getName(), factory).add(identity);
		return super.register(identity);
	}

	@Override
	protected ModelMapStrategy<ElementType> delegate() {
		return delegate;
	}

	public interface Listener {
		void onRegister(ModelObjectIdentity<?> e);
		void onRealizing(ModelObjectIdentity<?> e);
		void onRealized(ModelObjectIdentity<?> e);
		void onFinalizing(ModelObjectIdentity<?> e);
	}
}
