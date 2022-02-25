/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.model.internal.actions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import dev.nokee.model.internal.FullyQualifiedNameComponent;
import dev.nokee.model.internal.ElementNameComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelEntityId;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.state.ModelState;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.specs.Spec;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

@SuppressWarnings("unchecked")
public final class ModelActionSystem implements Action<Project> {
	private final ReentrantAvoidance reentrant = new ReentrantAvoidance();
	private final ModelLookup lookup;

	public ModelActionSystem(ModelLookup lookup) {
		this.lookup = lookup;
	}

	@Override
	public void execute(Project project) {
		val configurer = project.getExtensions().getByType(ModelConfigurer.class);

		// Rules to execute actions
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ActionSelectorComponent.class), this::onIdentityChanged));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelSpecComponent.class), ModelComponentReference.of(ModelActionComponent.class), this::onActionAdded));

		// Rules to keep identity up-to-date
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.ofAny(componentOf(ModelProjection.class)), this::updateSelectorForProjection));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ElementNameComponent.class), this::updateSelectorForElementName));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(FullyQualifiedNameComponent.class), this::updateSelectorForFullyQualifiedName));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), this::updateSelectorForParent));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelState.class), this::updateSelectorForState));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), this::updateSelectorForAncestors));
	}

	// ComponentFromEntity<MatchingSpecificationComponent> (readonly) all
	// ComponentFromEntity<ActionComponent> (readonly) all
	// ComponentFromEntity<ExecutedActionComponent> (read-write) self
	private void onIdentityChanged(ModelNode entity, ActionSelectorComponent component) {
		allActions(lookup, reentrant.andDeferredActions(entity, filter(actionMatching(component),
			whileIgnoringExecuted(entity, executeAction(entity)))));
	}

	private static Consumer<ModelNode> executeAction(ModelNode entity) {
		return it -> it.getComponent(componentOf(ModelActionComponent.class)).get().execute(entity);
	}

	private static void allActions(ModelLookup lookup, Consumer<? super Iterable<ModelNode>> action) {
		action.accept(lookup.query(it -> it.hasComponent(componentOf(ModelActionTag.class))).get());
	}

	private static Consumer<Iterable<ModelNode>> filter(Predicate<? super ModelNode> filter, Consumer<? super Iterable<ModelNode>> action) {
		return it -> action.accept(Iterables.filter(it, filter::test));
	}

	//region Sync ExecutedActionComponent
	private static Consumer<Iterable<ModelNode>> whileIgnoringExecuted(ModelNode entity, Consumer<? super ModelNode> action) {
		return actionEntities -> {
			// Retrieve all previously executed actions
			val ids = new HashSet<>(executedActions(entity));

			// Pass only action entities that were not previously executed
			for (ModelNode actionEntity : actionEntities) {
				if (ids.add(actionEntity.getId())) {
					action.accept(actionEntity);
				}
			}

			// Saves executed action list
			entity.addComponent(new ExecutedActionComponent(ids));
		};
	}

	private static Consumer<ModelNode> updateExecutedAfter(ModelNode actionEntity, Consumer<? super ModelNode> action) {
		return entity -> {
			// Execute action
			action.accept(entity);

			// Update executed actions
			entity.addComponent(new ExecutedActionComponent(ImmutableSet.<ModelEntityId>builder().addAll(executedActions(entity)).add(actionEntity.getId()).build()));
		};
	}

	private static Set<ModelEntityId> executedActions(ModelNode entity) {
		return entity.findComponent(ExecutedActionComponent.class).map(ExecutedActionComponent::get).orElse(Collections.emptySet());
	}
	//endregion

	private static Predicate<ModelNode> actionMatching(ActionSelectorComponent component) {
		return it -> {
			val identity = it.findComponent(componentOf(ModelSpecComponent.class));
			return identity.map(actionIdentityComponent -> ((Spec<DomainObjectIdentity>) actionIdentityComponent.get()).isSatisfiedBy(component.get())).orElse(false);
		};
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForElementName(ModelNode entity, ElementNameComponent elementName) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(elementName.get()))
			.orElseGet(() -> DomainObjectIdentity.of(elementName.get()))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForFullyQualifiedName(ModelNode entity, FullyQualifiedNameComponent fullyQualifiedName) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(fullyQualifiedName.get()))
			.orElseGet(() -> DomainObjectIdentity.of(fullyQualifiedName.get()))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForState(ModelNode entity, ModelState state) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(state))
			.orElseGet(() -> DomainObjectIdentity.of(state))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForParent(ModelNode entity, ParentComponent parent) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(new ParentRef(parent.get().getId())))
			.orElseGet(() -> DomainObjectIdentity.of(new ParentRef(parent.get().getId())))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForProjection(ModelNode entity, ModelProjection projection) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(it.get(ProjectionTypes.class).map(t -> t.plus(projection.getType())).orElseGet(() -> ProjectionTypes.of(projection.getType()))))
			.orElseGet(() -> DomainObjectIdentity.of(ProjectionTypes.of(projection.getType())))));
	}

	// ComponentFromEntity<ParentComponent> read-only all
	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForAncestors(ModelNode entity, ParentComponent parent) {
		val ancestors = ImmutableSet.<ModelEntityId>builder();
		Optional<ParentComponent> parentComponent = Optional.of(parent);
		while(parentComponent.isPresent()) {
			ancestors.add(parentComponent.get().get().getId());
			parentComponent = parentComponent.flatMap(it -> it.get().findComponent(componentOf(ParentComponent.class)));
		}

		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(new Ancestors(ancestors.build())))
			.orElseGet(() -> DomainObjectIdentity.of(new Ancestors(ancestors.build())))));
	}

	// ComponentFromEntity<ActionSelectorComponent> (readonly) all
	// ComponentFromEntity<ExecutedActionComponent> (read-write) all
	private void onActionAdded(ModelNode entity, ModelSpecComponent identity, ModelActionComponent component) {
		allEntities(lookup, filter(onlyMatching(identity),
			it -> it.forEach(reentrant.ifPossible(entity, updateExecutedAfter(entity, executeAction(component))))));
	}

	private static Consumer<ModelNode> executeAction(ModelActionComponent component) {
		return it -> component.get().execute(it);
	}

	private static void allEntities(ModelLookup lookup, Consumer<? super Iterable<ModelNode>> action) {
		action.accept(lookup.query(it -> it.hasComponent(componentOf(ActionSelectorComponent.class))).get());
	}

	private static Predicate<ModelNode> onlyMatching(ModelSpecComponent component) {
		return entity -> {
			val selector = entity.findComponent(componentOf(ActionSelectorComponent.class));
			return selector.map(t -> ((Spec<DomainObjectIdentity>) component.get()).isSatisfiedBy(t.get())).orElse(false);
		};
	}
}
