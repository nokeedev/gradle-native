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

package dev.nokee.model.internal;

import com.google.common.collect.Iterables;
import dev.nokee.internal.Factory;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.Rule;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class DiscoveredElements {
	private final DomainObjectSet<Element> elements;
	private final Map<ModelObjectIdentity<?>, ModelObject<?>> objects = new HashMap<>();
	private final DiscoveryService service;

	public DiscoveredElements(ObjectFactory objects, DiscoveryService service) {
		this.elements = objects.domainObjectSet(Element.class);
		this.service = service;

		this.elements.all(it -> {
			discoverType(it.getType());
		});
	}

	public Rule ruleFor(Class<?> baseType) {
		return new Rule() {
			@Override
			public String getDescription() {
				return "discover elements";
			}

			@Override
			public void apply(String domainObjectName) {
				if (!discoverDirectMatch(domainObjectName, baseType)) {
					discoverPrefix(domainObjectName, baseType);
					// TODO: Maybe discover task-name inference
				}
			}
		};
	}

	public <RegistrableType> ModelObject<RegistrableType> discover(ModelObjectIdentity<RegistrableType> identity, Factory<ModelObject<RegistrableType>> factory) {
		elements.add(new Element(identity, null));
		final ModelObject<RegistrableType> result = factory.create();
		objects.put(identity, result);
		return result;
	}

	private void discoverType(ModelType<?> type) {
		for (DiscoveryService.DiscoveredEl discoveredEl : service.discover(type)) {
			elements.all(e -> {
				for (DiscoveryService.RealizedDiscoveredEl realizedDiscoveredEl : discoveredEl.execute(e.identity)) {
					elements.add(realizedDiscoveredEl.toElement(e));
				}
			});
		}
	}

	public <T> void onRealized(Action<? super T> action, Consumer<? super Action<? super T>> next) {
		discoverType(ModelType.typeOf(action));
		next.accept(action);
	}

	public <T> void onKnown(Action<? super KnownModelObject<T>> action, Consumer<? super Action<? super KnownModelObject<T>>> next) {
		discoverType(ModelType.typeOf(action));
		next.accept(action);
	}

	public <T> void onFinalized(Action<? super T> action, Consumer<? super Action<? super T>> next) {
		discoverType(ModelType.typeOf(action));
		next.accept(action);
	}

	public void discoverAll(Class<?> baseType) {
		for (int i = 0; i < elements.size(); ++i) {
			Element candidate = Iterables.get(elements, i);
			if (candidate.getType().isSubtypeOf(baseType)) {
				realize(candidate);
			}
		}
	}

	private boolean discoverDirectMatch(String name, Class<?> baseType) {
		for (int i = 0; i < elements.size(); ++i) {
			Element candidate = Iterables.get(elements, i);
			if (candidate.getType().isSubtypeOf(baseType) && ModelObjectIdentifiers.asFullyQualifiedName(candidate.getIdentifier()).toString().equals(name)) {
				realize(candidate);
				return true;
			}
		}
		return false;
	}

	private boolean discoverPrefix(String name, Class<?> baseType) {
		for (int i = 0; i < elements.size(); ++i) {
			Element candidate = Iterables.get(elements, i);
			if (candidate.getType().isSubtypeOf(baseType) && ModelObjectIdentifiers.asFullyQualifiedName(candidate.getIdentifier()).toString().startsWith(name)) {
				realize(candidate);
				return true;
			}
		}
		return false;
	}

	private void realize(Element e) {
		if (e == null) {
			return;
		}

		realize(e.parent);
		// Failure here may be because of identifier mismatch regarding main identifier vs non-main identifier
		Objects.requireNonNull(objects.get(e.identity), () -> "no model object for " + e.identity).get(); // realize
	}

	@EqualsAndHashCode
	public static final class Element {
		private final ModelObjectIdentity<?> identity;
		@Nullable private final Element parent;

		public Element(ModelObjectIdentity<?> identity, @Nullable Element parent) {
			this.identity = identity;
			this.parent = parent;
		}

		public ModelObjectIdentifier getIdentifier() {
			return identity.getIdentifier();
		}

		public ModelType<?> getType() {
			return identity.getType();
		}

		@Override
		public String toString() {
			return "Element{" +
				"identifier=" + ModelObjectIdentifiers.asFullyQualifiedName(identity.getIdentifier()) +
				", type=" + identity.getType() +
				", parent=" + parent +
				'}';
		}
	}

	public interface ModelObjectIdentifierResolver {
		ModelObjectIdentifier resolve(ModelObjectIdentifier baseIdentifier);
	}
}
