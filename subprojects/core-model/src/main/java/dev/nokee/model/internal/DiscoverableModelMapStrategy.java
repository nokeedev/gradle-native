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
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static dev.nokee.utils.TransformerUtils.noOpTransformer;

final class DiscoverableModelMapStrategy<ElementType> implements ModelMapStrategy<ElementType> {
	private final Map<String, DomainObjectSet<ModelObjectIdentity<?>>> nameToIdentities = new HashMap<>();
	private final Function<String, DomainObjectSet<ModelObjectIdentity<?>>> factory;
	private final DiscoveredElements discoveredElements;
	private final ProviderFactory providers;
	private final ModelMapStrategy<ElementType> delegate;

	@SuppressWarnings("unchecked")
	DiscoverableModelMapStrategy(Namer<ElementType> namer, ObjectFactory objects, DiscoveredElements discoveredElements, ProviderFactory providers, ModelMapStrategy<ElementType> delegate) {
		this.discoveredElements = discoveredElements;
		this.providers = providers;
		this.delegate = delegate;
		this.factory = __ -> {
			return (DomainObjectSet<ModelObjectIdentity<?>>) objects.domainObjectSet(new TypeToken<ModelObjectIdentity<?>>() {}.getRawType());
		};

		// For book keeping...
		delegate.configureEach(it -> {
			nameToIdentities.computeIfAbsent(namer.determineName(it), factory).all(discoveredElements::onRealizing);
		});
		delegate.whenElementFinalized(it -> {
			nameToIdentities.computeIfAbsent(namer.determineName(it), factory).all(e -> {
				discoveredElements.onRealized(e);
				discoveredElements.onFinalizing(e);
			});
		});
	}

	@Override
	public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
		final ModelObject<RegistrableType> result = discoveredElements.discover(identity, () -> {
			nameToIdentities.computeIfAbsent(identity.getName(), factory).add(identity);
			return delegate.register(identity);
		});
		return new MObjectAdapter<>(result);
	}

	@Override
	public void configureEach(Action<? super ElementType> configureAction) {
		delegate.configureEach(discoveredElements.onRealized(configureAction));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void whenElementKnown(Action<? super KnownModelObject<? extends ElementType>> configureAction) {
		delegate.whenElementKnown(discoveredElements.onKnown(it -> configureAction.execute(new KObjectAdapter<>((KnownModelObject<ElementType>) it))));
	}

	@Override
	public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
		delegate.whenElementFinalized(discoveredElements.onFinalized(finalizeAction));
	}

	@Override
	public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
		// TODO: Discover identifier
		return new MObjectAdapter<>(delegate.getById(identifier));
	}

	@Override
	public <U> Provider<Set<U>> getElements(Class<U> type, Spec<? super ModelObjectIdentity<?>> spec) {
		return providers.provider(() -> {
			discoveredElements.discoverAll(it -> it.getType().isSubtypeOf(type) && spec.isSatisfiedBy(ModelObjectIdentity.ofIdentity(it.getIdentifier(), it.getType())));
			return delegate.getElements(type, spec);
		}).flatMap(noOpTransformer());
	}

	private final class KObjectAdapter<T> implements KnownModelObject<T> {
		private final KnownModelObject<T> delegate;

		private KObjectAdapter(KnownModelObject<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public ModelObjectIdentifier getIdentifier() {
			return delegate.getIdentifier();
		}

		@Override
		public ModelType<?> getType() {
			return delegate.getType();
		}

		@Override
		public KnownModelObject<T> configure(Action<? super T> configureAction) {
			// FIXME: Scope to identifier
			delegate.configure(discoveredElements.onRealized(/*ofIdentity(getIdentifier(), getType()), */configureAction));
			return this;
		}

		@Override
		public KnownModelObject<T> whenFinalized(Action<? super T> finalizeAction) {
			// FIXME: Scope to identifier
			delegate.whenFinalized(discoveredElements.onFinalized(/*ofIdentity(getIdentifier(), getType()), */finalizeAction));
			return this;
		}

		@Override
		public void realizeNow() {
			// FIXME(discover): Mark element as realizing/realized
			//   Need to figure out what should be captured by the discovery service
//			discoveredElements.onRealizing(delegate);
			delegate.realizeNow();
//			discoveredElements.onRealized(delegate);
		}

		@Override
		public Provider<T> asProvider() {
			return delegate.asProvider();
		}

		@Override
		public String getName() {
			return delegate.getName();
		}
	}

	private final class MObjectAdapter<T> implements ModelObject<T> {
		private final ModelObject<T> delegate;

		private MObjectAdapter(ModelObject<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public ModelObjectIdentifier getIdentifier() {
			return delegate.getIdentifier();
		}

		@Override
		public ModelType<?> getType() {
			return delegate.getType();
		}

		@Override
		public NamedDomainObjectProvider<T> asProvider() {
			return delegate.asProvider();
		}

		@Override
		public T get() {
			return delegate.get();
		}

		@Override
		public ModelObject<T> configure(Action<? super T> configureAction) {
			// FIXME: Scope to identifier
			delegate.configure(discoveredElements.onRealized(/*ofIdentity(getIdentifier(), getType()), */configureAction));
			return this;
		}

		@Override
		public ModelObject<T> whenFinalized(Action<? super T> finalizeAction) {
			// FIXME: Scope to identifier
			delegate.whenFinalized(discoveredElements.onFinalized(/*ofIdentity(getIdentifier(), getType()), */finalizeAction));
			return this;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}
	}

	public interface Listener {
		void onRealizing(ModelObjectIdentity<?> e);
		void onRealized(ModelObjectIdentity<?> e);
		void onFinalizing(ModelObjectIdentity<?> e);
	}

	public interface Instrument {
		<T> Action<KnownModelObject<? extends T>> onKnown(Action<KnownModelObject<? extends T>> action);

		<T> Action<T> onRealized(Action<T> configureAction);
		<T> Action<T> onRealized(ModelObjectIdentity<T> identity, Action<T> configureAction);

		<T> Action<T> onFinalized(Action<T> finalizeAction);
		<T> Action<T> onFinalized(ModelObjectIdentity<T> identity, Action<T> finalizeAction);
	}
}
