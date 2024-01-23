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

package dev.nokee.model.internal.discover;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjectIdentity;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EqualsAndHashCode
public final class CandidateElement {
	// FIXME(discovery): How to report where each elements comes from (the same would be where each rule comes from)
	@EqualsAndHashCode.Include private final ModelObjectIdentifier identifier;
	@EqualsAndHashCode.Include private final ModelType<?> type;
	@EqualsAndHashCode.Include private final List<DiscoverChain> actions;
	private final Object sourceRule;
	private final boolean isKnown;
	private final Predicate<ModelObjectIdentity<?>> realized;
	private final Predicate<ModelObjectIdentity<?>> finalized;

	public CandidateElement(ModelObjectIdentifier identifier, ModelType<?> type, boolean isKnown, Predicate<ModelObjectIdentity<?>> realized, Predicate<ModelObjectIdentity<?>> finalized, List<DiscoverChain> actions, Object sourceRule) {
		this.identifier = identifier;
		this.type = type;
		this.isKnown = isKnown;
		this.realized = realized;
		this.finalized = finalized;
		this.actions = actions;
		this.sourceRule = sourceRule;
	}

	public ModelObjectIdentifier getIdentifier() {
		return identifier;
	}

	public ModelType<?> getType() {
		return type;
	}

	public List<DiscoverChain> getActions() {
		return actions;
	}

	public boolean isRealized() {
		return realized.test(ModelObjectIdentity.ofIdentity(identifier, type));
	}

	public boolean isFinalized() {
		return finalized.test(ModelObjectIdentity.ofIdentity(identifier, type));
	}

	public boolean isKnown() {
		return isKnown;
	}

	@Override
	public String toString() {
		return ModelObjectIdentifiers.asFullyQualifiedName(identifier) + " (" + type + ") -- " + actions.stream().map(Objects::toString).collect(Collectors.toList()) + " -- " + sourceRule;
	}

	public static final class DiscoverChain {
		private final CandidateElement el;
		private final Act action;

		public DiscoverChain(CandidateElement el, Act action) {
			this.el = el;
			this.action = action;
		}

		public ModelObjectIdentity<?> getTargetIdentity() {
			return ModelObjectIdentity.ofIdentity(el.identifier, el.type);
		}

		public Act getAction() {
			return action;
		}

		public enum Act {
			REALIZE, FINALIZE
		}

		@Override
		public String toString() {
			return action + " " + ModelObjectIdentifiers.asFullyQualifiedName(el.getIdentifier()) + " (" + el.getType() + ")";
		}
	}
}
