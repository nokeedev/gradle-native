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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import dev.nokee.internal.Factory;
import dev.nokee.model.internal.discover.CachedDiscoveryService;
import dev.nokee.model.internal.discover.CandidateElement;
import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.discover.FinalizeRule;
import dev.nokee.model.internal.discover.KnownElementRule;
import dev.nokee.model.internal.discover.KnownRule;
import dev.nokee.model.internal.discover.RealizeRule;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.utils.Optionals;
import dev.nokee.utils.SpecUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Rule;
import org.gradle.api.specs.Spec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static dev.nokee.model.internal.type.ModelType.of;

public class DiscoveredElements implements ListeningModelMapStrategy.Listener, InstrumentModelMapStrategy.Instrument {
	private final Map<ModelObjectIdentity<?>, ModelObject<?>> objects = new HashMap<>();
	private final CachedDiscoveryService service;
	private final ProjectIdentifier rootIdentifier;
	// FIXME(discovery): Ensure no duplicated rules are added
	private final LinkedHashSet<DisRule> rules = new LinkedHashSet<>();
	private final Set<ModelObjectIdentity<?>> realizedElements = new HashSet<>();
	private final Set<ModelObjectIdentity<?>> finalizedElements = new HashSet<>();

	public DiscoveredElements(CachedDiscoveryService service, ProjectIdentifier rootIdentifier) {
		this.service = service;
		this.rootIdentifier = rootIdentifier;
	}

	public Rule ruleFor(Class<?> baseType) {
		return new Rule() {
			@Override
			public String getDescription() {
				return "discover elements";
			}

			private boolean has(Predicate<? super String> name, Class<?> baseType) {
				for (DisRule rule : rules) {
					if (rule instanceof KnownElementRule) {
						if (name.test(ModelObjectIdentifiers.asFullyQualifiedName(((KnownElementRule) rule).getIdentity().getIdentifier()).toString()) && ((KnownElementRule) rule).getIdentity().getType().isSubtypeOf(baseType)) {
							return true;
						}
					}
				}
				return false;
			}

			@Override
			public void apply(String domainObjectName) {
				while (!has(it -> it.equals(domainObjectName), baseType)) {
					List<CandidateElement> r = find(domainObjectName, ModelType.of(baseType));
					if (r.isEmpty()) {
						break; // nothing to do
					} else {
						val action = r.stream().map(CandidateElement::getActions).filter(it -> !it.isEmpty()).map(it -> it.get(0)).filter(it -> it.getAction().equals(CandidateElement.DiscoverChain.Act.REALIZE)).findFirst();
						if (action.isPresent()) {
							objects.get(action.get().getTargetIdentity()).get();
						} else {
							break;
						}
					}
				}

				while (!has(it -> it.startsWith(domainObjectName), baseType)) {
					List<CandidateElement> r = findPrefix(domainObjectName, ModelType.of(baseType));
					if (r.isEmpty()) {
						break; // nothing to do
					} else {
						val action = r.stream().map(CandidateElement::getActions).filter(it -> !it.isEmpty()).map(it -> it.get(0)).filter(it -> it.getAction().equals(CandidateElement.DiscoverChain.Act.REALIZE)).findFirst();
						if (action.isPresent()) {
							objects.get(action.get().getTargetIdentity()).get();
						} else {
							break;
						}
					}
				}

				// FIXME(discovery): Discover prefixed (request `objects` which can match `objectsDebug`)
				// FIXME(discovery): Maybe discover task-name inference
			}
		};
	}

	public <RegistrableType> ModelObject<RegistrableType> discover(ModelObjectIdentity<RegistrableType> identity, Factory<ModelObject<RegistrableType>> factory) {
		if (identity.getIdentifier() instanceof ProjectIdentifier) {
			return factory.create();
		} else {
			rules.add(new KnownElementRule(identity));
			final ModelObject<RegistrableType> result = factory.create();
			objects.put(identity, result);
			return result;
		}
	}

	private Stream<DisRule> discoverType(Action<?> action) {
		return Stream.of(action).flatMap(it -> {
			Stream<Action<?>> nestedRules = Stream.empty();
			Spec<ModelObjectIdentity<?>> spec = SpecUtils.satisfyAll();
			if (it instanceof TypeFilteringAction) {
				nestedRules = Optionals.stream(((TypeFilteringAction<?, ?>) it).getDelegate());
				spec = ((TypeFilteringAction<?, ?>) it).getSpec();
			} else if (it instanceof IdentifierFilteringAction) {
				nestedRules = Stream.of(((IdentifierFilteringAction<?>) it).getDelegate());
				spec = ((IdentifierFilteringAction<?>) it).getSpec();
			}

			final Spec<ModelObjectIdentity<?>> theSpec = spec;
			return Stream.concat(Stream.of(it).flatMap(t -> service.discover(ModelType.typeOf(t)).stream()), nestedRules.flatMap(this::discoverType)).map(delegate -> {
				return new DisRule() {
					@Override
					public void execute(Details details) {
						if (theSpec.isSatisfiedBy(ModelObjectIdentity.ofIdentity(details.getCandidate().getIdentifier(), details.getCandidate().getType()))) {
							delegate.execute(details);
						}
					}

					@Override
					public String toString() {
						return "using spec " + theSpec + " " + delegate;
					}
				};
			});
		});
	}

	@Override
	public <T> Action<T> onRealized(Action<T> action) {
		// FIXME(discovery): extract type filter from action
		discoverType(action).map(RealizeRule::new).forEach(rules::add);
		return action;
	}

	@Override
	public <T> Action<T> onRealized(ModelObjectIdentity<?> identity, Action<T> configureAction) {
		// FIXME(discovery): Account for identity
		return onRealized(configureAction);
	}

	@Override
	public void onRegister(ModelObjectIdentity<?> e) {
		// FIXME(discovery): Maybe use this API to discover the registering entity
	}

	@Override
	public void onRealizing(ModelObjectIdentity<?> e) {
		// FIXME(discover): mark element as realizing...
		realizedElements.add(e);
	}

	@Override
	public void onRealized(ModelObjectIdentity<?> e) {
		// FIXME(discover): mark element as realized...
	}

	@Override
	public void onFinalizing(ModelObjectIdentity<?> e) {
		finalizedElements.add(e);
	}

	@Override
	public <T> Action<KnownModelObject<? extends T>> onKnown(Action<KnownModelObject<? extends T>> action) {
		// FIXME(discovery): extract type filter from action
		discoverType(action).map(KnownRule::new).forEach(rules::add);
		return action;
	}

	@Override
	public <T> Action<T> onFinalized(Action<T> action) {
		// FIXME(discovery): extract type filter from action
		discoverType(action).map(FinalizeRule::new).forEach(rules::add);
		return action;
	}

	@Override
	public <T> Action<T> onFinalized(ModelObjectIdentity<?> identity, Action<T> finalizeAction) {
		// FIXME(discovery): Account for identity
		return onFinalized(finalizeAction);
	}

	//region FIXME(discovery): streamline discovery
	private boolean discoverableElements(Class<?> baseType) {
		return findAll(ModelType.of(baseType)).stream().anyMatch(it -> !it.getActions().isEmpty() && it.getActions().get(0).getAction().equals(CandidateElement.DiscoverChain.Act.REALIZE));
	}

	public void discoverAll(Class<?> baseType) {
		while (discoverableElements(baseType)) {
			for (CandidateElement candidateElement : findAll(of(baseType))) {
				if (!candidateElement.getActions().isEmpty()) {
					objects.get(candidateElement.getActions().get(0).getTargetIdentity()).get();
				}
			}
		}
	}

	private boolean discoverableElements(Spec<? super ModelObjectIdentity<?>> spec) {
		return findAll(ModelType.of(Object.class)).stream().anyMatch(it -> spec.isSatisfiedBy(ModelObjectIdentity.ofIdentity(it.getIdentifier(), it.getType())) && !it.getActions().isEmpty() && it.getActions().get(0).getAction().equals(CandidateElement.DiscoverChain.Act.REALIZE));
	}

	public void discoverAll(Spec<? super ModelObjectIdentity<?>> spec) {
		while (discoverableElements(spec)) {
			for (CandidateElement candidateElement : findAll(of(Object.class))) {
				if (!candidateElement.getActions().isEmpty()) {
					objects.get(candidateElement.getActions().get(0).getTargetIdentity()).get();
				}
			}
		}
	}

	private List<CandidateElement> find(String fullyQualifiedName, ModelType<?> type) {
		List<CandidateElement> r = new ArrayList<>();

		final CandidateElement current = new CandidateElement(rootIdentifier, of(Project.class), true, realizedElements::contains, finalizedElements::contains, Collections.emptyList(), null);
		List<CandidateElement> result = new ArrayList<>();
		list(result, current, rules);

		for (CandidateElement candidateElement : result) {
			String candidateName = ModelObjectIdentifiers.asFullyQualifiedName(candidateElement.getIdentifier()).toString();
			if (candidateName.contains("*")) {
				Pattern p = Pattern.compile("^" + candidateName.replace("*", ".*") + "$");
				if (p.matcher(fullyQualifiedName).matches() && candidateElement.getType().isSubtypeOf(type)) {
					r.add(candidateElement);
				}
			} else if (candidateName.equals(fullyQualifiedName) && candidateElement.getType().isSubtypeOf(type)) {
				r.add(candidateElement);
			}
		}

		return r;
	}

	private List<CandidateElement> findPrefix(String prefixName, ModelType<?> type) {
		List<CandidateElement> r = new ArrayList<>();

		final CandidateElement current = new CandidateElement(rootIdentifier, of(Project.class), true, realizedElements::contains, finalizedElements::contains, Collections.emptyList(), null);
		List<CandidateElement> result = new ArrayList<>();
		list(result, current, rules);

		for (CandidateElement candidateElement : result) {
			String candidateName = ModelObjectIdentifiers.asFullyQualifiedName(candidateElement.getIdentifier()).toString();
			if (candidateName.contains("*")) {
				Pattern p = Pattern.compile("^" + candidateName.replace("*", ".*"));
				if (p.matcher(prefixName).matches() && candidateElement.getType().isSubtypeOf(type)) {
					r.add(candidateElement);
				}
			} else if (candidateName.startsWith(prefixName) && candidateElement.getType().isSubtypeOf(type)) {
				r.add(candidateElement);
			}
		}

		return r;
	}

	private List<CandidateElement> findAll(ModelType<?> type) {
		List<CandidateElement> r = new ArrayList<>();

		CandidateElement current = new CandidateElement(rootIdentifier, of(Project.class), true, realizedElements::contains, finalizedElements::contains, Collections.emptyList(), null);
		Set<CandidateElement> result = new LinkedHashSet<>();
		list(result, current, rules);

		for (CandidateElement candidateElement : result) {
			if (candidateElement.getType().isSubtypeOf(type)) {
				r.add(candidateElement);
			}
		}

		return r;
	}

	private void list(Collection<CandidateElement> result, CandidateElement current, Set<DisRule> rules) {
		result.add(current);

		for (final DisRule rule : rules) {
			rule.execute(new DisRule.Details() {
				@Override
				public CandidateElement getCandidate() {
					return current;
				}

				@Override
				public void newCandidate(ModelObjectIdentity<?> knownIdentity) {
					list(result, new CandidateElement(knownIdentity.getIdentifier(), knownIdentity.getType(), true, realizedElements::contains, finalizedElements::contains, current.getActions(), rule), ImmutableSet.<DisRule>builder().addAll(rules).addAll(service.discover(knownIdentity.getType())).build());
				}

				@Override
				public void newCandidate(ElementName elementName, ModelType<?> produceType) {
					ModelObjectIdentifier identifier = current.getIdentifier();
					if (elementName != null) {
						identifier = current.getIdentifier().child(elementName);
					}
					list(result, new CandidateElement(identifier, produceType, false, realizedElements::contains, finalizedElements::contains, current.getActions(), rule), ImmutableSet.<DisRule>builder().addAll(rules).addAll(service.discover(produceType)).build());
				}

				@Override
				public void newCandidate(ElementName elementName, ModelType<?> produceType, CandidateElement.DiscoverChain.Act action) {
					ModelObjectIdentifier identifier = current.getIdentifier();
					if (elementName != null) {
						identifier = current.getIdentifier().child(elementName);
					}
					list(result, new CandidateElement(identifier, produceType, false, realizedElements::contains, finalizedElements::contains, ImmutableList.<CandidateElement.DiscoverChain>builder().addAll(current.getActions()).add(new CandidateElement.DiscoverChain(current, action)).build(), rule), ImmutableSet.<DisRule>builder().addAll(rules).addAll(service.discover(produceType)).build());
				}
			});
		}
	}
	//endregion
}
