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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import dev.nokee.model.internal.core.ModelAction;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelEntityId;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.specs.Spec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

@SuppressWarnings("unchecked")
public final class ModelActionSystem<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	private final ReentrantAvoidance reentrant = new ReentrantAvoidance();
	private final List<ModelNode> allActionEntities = new ArrayList<>();
	private final List<ModelNode> allConfigurableEntities = new ArrayList<>();

	@Override
	public void apply(T target) {
		val configurer = target.getExtensions().getByType(ModelConfigurer.class);

		// Rules to execute actions
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ModelActionTag.class), this::trackActions));
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), this::trackConfigurableEntities));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ActionSelectorComponent.class), this::onIdentityChanged));
		configurer.configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelSpecComponent.class), ModelComponentReference.of(ModelActionComponent.class), this::onActionAdded));

		// Rules to keep identity up-to-date
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), ModelComponentReference.of(ModelProjection.class), this::updateSelectorForProjection));
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), ModelComponentReference.of(ParentComponent.class), this::updateSelectorForParent));
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), ModelComponentReference.of(ModelState.class), this::updateSelectorForState));
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), ModelComponentReference.of(ParentComponent.class), this::updateSelectorForAncestors));
		configurer.configure(ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), this::updateSelectorForSelf));
	}

	// ComponentFromEntity<MatchingSpecificationComponent> (readonly) all
	// ComponentFromEntity<ActionComponent> (readonly) all
	// ComponentFromEntity<ExecutedActionComponent> (read-write) self
	private void onIdentityChanged(ModelNode entity, ActionSelectorComponent component) {
		allActions(reentrant.andDeferredActions(entity, filter(actionMatching(component),
			whileIgnoringExecuted(entity, executeAction(entity)))));
	}

	private static Consumer<ModelNode> executeAction(ModelNode entity) {
		return it -> it.getComponent(componentOf(ModelActionComponent.class)).get().execute(entity);
	}

	private void allActions(Consumer<? super Iterable<ModelNode>> action) {
		action.accept(ImmutableList.copyOf(allActionEntities));
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
		return entity.find(ExecutedActionComponent.class).map(ExecutedActionComponent::get).orElse(Collections.emptySet());
	}
	//endregion

	private static Predicate<ModelNode> actionMatching(ActionSelectorComponent component) {
		return it -> {
			val identity = it.findComponent(componentOf(ModelSpecComponent.class));
			return identity.map(actionIdentityComponent -> ((Spec<DomainObjectIdentity>) actionIdentityComponent.get()).isSatisfiedBy(component.get())).orElse(false);
		};
	}

	private void trackActions(ModelNode entity, ModelComponentTag<ModelActionTag> tag) {
		allActionEntities.add(entity);
	}

	private void trackConfigurableEntities(ModelNode entity, ModelComponentTag<ConfigurableTag> tag) {
		allConfigurableEntities.add(entity);
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForState(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ModelState state) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(state))
			.orElseGet(() -> DomainObjectIdentity.of(state))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForParent(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ParentComponent parent) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(new ParentRef(parent.get().getId())))
			.orElseGet(() -> DomainObjectIdentity.of(new ParentRef(parent.get().getId())))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForProjection(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ModelProjection projection) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.plus(projection.getType()))
			.orElseGet(() -> DomainObjectIdentity.of(projection.getType()))));
	}

	// ComponentFromEntity<ParentComponent> read-only all
	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForAncestors(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ParentComponent parent) {
		val ancestors = ImmutableSet.<AncestorRef>builder();
		Optional<ParentComponent> parentComponent = Optional.of(parent);
		while(parentComponent.isPresent()) {
			ancestors.add(new AncestorRef(parentComponent.get().get().getId()));
			parentComponent = parentComponent.flatMap(it -> it.get().findComponent(componentOf(ParentComponent.class)));
		}

		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(ancestors.build()))
			.orElseGet(() -> DomainObjectIdentity.of(ancestors.build()))));
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	private void updateSelectorForSelf(ModelNode entity, ModelComponentTag<ConfigurableTag> tag) {
		entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
			.map(ActionSelectorComponent::get)
			.map(it -> it.with(new SelfRef(entity.getId())))
			.orElseGet(() -> DomainObjectIdentity.of(new SelfRef(entity.getId())))));
	}

	// ComponentFromEntity<ActionSelectorComponent> (readonly) all
	// ComponentFromEntity<ExecutedActionComponent> (read-write) all
	private void onActionAdded(ModelNode entity, ModelSpecComponent identity, ModelActionComponent component) {
		allEntities(filter(onlyMatching(identity),
			it -> it.forEach(reentrant.ifPossible(entity, updateExecutedAfter(entity, executeAction(component))))));
	}

	private static Consumer<ModelNode> executeAction(ModelActionComponent component) {
		return it -> component.get().execute(it);
	}

	private void allEntities(Consumer<? super Iterable<ModelNode>> action) {
		action.accept(ImmutableList.copyOf(allConfigurableEntities));
	}

	private static Predicate<ModelNode> onlyMatching(ModelSpecComponent component) {
		return entity -> {
			val selector = entity.findComponent(componentOf(ActionSelectorComponent.class));
			return selector.map(t -> ((Spec<DomainObjectIdentity>) component.get()).isSatisfiedBy(t.get())).orElse(false);
		};
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	public static <T extends ModelComponent> ModelAction updateSelectorForTag(Class<T> componentType) {
		return ModelActionWithInputs.of(ModelTags.referenceOf(ConfigurableTag.class), ModelComponentReference.of(componentType), new ModelActionWithInputs.A2<ModelComponentTag<ConfigurableTag>, T>() {
			@Override
			public void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, T component) {
				entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
					.map(ActionSelectorComponent::get)
					.map(it -> it.with(valueOf(component)))
					.orElseGet(() -> DomainObjectIdentity.of(valueOf(component)))));
			}

			private Object valueOf(T component) {
				if (component instanceof Supplier) {
					return ((Supplier<?>) component).get();
				} else {
					return component;
				}
			}
		});
	}
}
