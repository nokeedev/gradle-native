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

import dev.nokee.internal.provider.ProviderConvertibleInternal;
import org.gradle.api.Action;

import static dev.nokee.model.internal.ModelObjectIdentity.ofIdentity;

public final class InstrumentModelMapStrategy<ElementType> extends ForwardingModelMapStrategy<ElementType> {
	private final Instrument instrument;
	private final ModelMapStrategy<ElementType> delegate;

	public InstrumentModelMapStrategy(Instrument instrument, ModelMapStrategy<ElementType> delegate) {
		this.instrument = instrument;
		this.delegate = delegate;
	}

	@Override
	protected ModelMapStrategy<ElementType> delegate() {
		return delegate;
	}

	@Override
	public <RegistrableType extends ElementType> ModelObject<RegistrableType> register(ModelObjectIdentity<RegistrableType> identity) {
		return new MObjectAdapter<>(super.register(identity));
	}

	@Override
	public void configureEach(Action<? super ElementType> configureAction) {
		super.configureEach(instrument.onRealized(configureAction));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void whenElementKnown(Action<? super KnownModelObject<? extends ElementType>> configureAction) {
		super.whenElementKnown(instrument.onKnown(new Action<KnownModelObject<? extends ElementType>>() {
			@Override
			public void execute(KnownModelObject<? extends ElementType> it) {
				configureAction.execute(new KObjectAdapter<>((KnownModelObject<ElementType>) it));
			}

			@Override
			public String toString() {
				return configureAction.toString();
			}
		}));
	}

	@Override
	public void whenElementFinalized(Action<? super ElementType> finalizeAction) {
		super.whenElementFinalized(instrument.onFinalized(finalizeAction));
	}

	@Override
	public ModelObject<ElementType> getById(ModelObjectIdentifier identifier) {
		return new MObjectAdapter<>(super.getById(identifier));
	}

	public interface Instrument {
		<T> Action<KnownModelObject<? extends T>> onKnown(Action<KnownModelObject<? extends T>> action);

		<T> Action<T> onRealized(Action<T> configureAction);
		<T> Action<T> onRealized(ModelObjectIdentity<?> identity, Action<T> configureAction);

		<T> Action<T> onFinalized(Action<T> finalizeAction);
		<T> Action<T> onFinalized(ModelObjectIdentity<?> identity, Action<T> finalizeAction);
	}

	private final class KObjectAdapter<T> extends ForwardingKnownModelObject<T> {
		private final KnownModelObject<T> delegate;
		private final ModelObjectIdentity<?> identity;

		private KObjectAdapter(KnownModelObject<T> delegate) {
			this.delegate = delegate;
			this.identity = ofIdentity(getIdentifier(), getType());
		}

		@Override
		protected KnownModelObject<T> delegate() {
			return delegate;
		}

		@Override
		public KnownModelObject<T> configure(Action<? super T> configureAction) {
			// FIXME: Scope to identifier
			super.configure(instrument.onRealized(identity, configureAction));
			return this;
		}

		@Override
		public KnownModelObject<T> whenFinalized(Action<? super T> finalizeAction) {
			// FIXME: Scope to identifier
			super.whenFinalized(instrument.onFinalized(identity, finalizeAction));
			return this;
		}

		@Override
		public void realizeNow() {
			// FIXME(discover): Mark element as realizing/realized
			//   Need to figure out what should be captured by the discovery service
//			listener.onRealizing(delegate);
			super.realizeNow();
//			listener.onRealized(delegate);
		}
	}

	private final class MObjectAdapter<T> extends ForwardingModelObject<T> implements ProviderConvertibleInternal<T> {
		private final ModelObject<T> delegate;
		private final ModelObjectIdentity<?> identity;

		private MObjectAdapter(ModelObject<T> delegate) {
			this.delegate = delegate;
			this.identity = ofIdentity(getIdentifier(), getType());
		}

		@Override
		protected ModelObject<T> delegate() {
			return delegate;
		}

		@Override
		public ModelObject<T> configure(Action<? super T> configureAction) {
			// FIXME: Scope to identifier
			super.configure(instrument.onRealized(identity, configureAction));
			return this;
		}

		@Override
		public ModelObject<T> whenFinalized(Action<? super T> finalizeAction) {
			// FIXME: Scope to identifier
			super.whenFinalized(instrument.onFinalized(identity, finalizeAction));
			return this;
		}
	}
}
