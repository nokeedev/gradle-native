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
import dev.nokee.utils.DeferredUtils;
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
		configurer.configure(/*trackActions*/new ModelActionWithInputs.ModelAction1<ModelComponentTag<ModelActionTag>>() {
			protected void execute(ModelNode entity, ModelComponentTag<ModelActionTag> tag) {
				allActionEntities.add(entity);
			}
		});
		configurer.configure(/*trackConfigurableEntities*/new ModelActionWithInputs.ModelAction1<ModelComponentTag<ConfigurableTag>>() {
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag) {
				allConfigurableEntities.add(entity);
			}
		});
		configurer.configure(/*onIdentityChanged*/new ModelActionWithInputs.ModelAction1<ActionSelectorComponent>() {
			// ComponentFromEntity<MatchingSpecificationComponent> (readonly) all
			// ComponentFromEntity<ActionComponent> (readonly) all
			// ComponentFromEntity<ExecutedActionComponent> (read-write) self
			protected void execute(ModelNode entity, ActionSelectorComponent component) {
				allActions(reentrant.andDeferredActions(entity, filter(actionMatching(component),
					whileIgnoringExecuted(entity, executeAction(entity)))));
			}
		});
		configurer.configure(/*onActionAdded*/new ModelActionWithInputs.ModelAction2<ModelSpecComponent, ModelActionComponent>() {
			// ComponentFromEntity<ActionSelectorComponent> (readonly) all
			// ComponentFromEntity<ExecutedActionComponent> (read-write) all
			protected void execute(ModelNode entity, ModelSpecComponent identity, ModelActionComponent component) {
				allEntities(filter(onlyMatching(identity),
					it -> it.forEach(reentrant.ifPossible(entity, updateExecutedAfter(entity, executeAction(component))))));
			}
		});

		// Rules to keep identity up-to-date
		configurer.configure(/*updateSelectorForProjection*/new ModelActionWithInputs.ModelAction2<ModelComponentTag<ConfigurableTag>, ModelProjection>() {
			// ComponentFromEntity<ActionSelectorComponent> read-write self
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ModelProjection projection) {
				entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
					.map(ActionSelectorComponent::get)
					.map(it -> it.plus(projection.getType()))
					.orElseGet(() -> DomainObjectIdentity.of(ImmutableSet.of(projection.getType())))));
			}
		});
		configurer.configure(/*updateSelectorForParent*/new ModelActionWithInputs.ModelAction2<ModelComponentTag<ConfigurableTag>, ParentComponent>() {
			// ComponentFromEntity<ActionSelectorComponent> read-write self
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ParentComponent parent) {
				entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
					.map(ActionSelectorComponent::get)
					.map(it -> it.with(new ParentRef(parent.get().getId())))
					.orElseGet(() -> DomainObjectIdentity.of(new ParentRef(parent.get().getId())))));
			}
		});
		configurer.configure(/*updateSelectorForState*/new ModelActionWithInputs.ModelAction2<ModelComponentTag<ConfigurableTag>, ModelState>() {
			// ComponentFromEntity<ActionSelectorComponent> read-write self
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ModelState state) {
				entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
					.map(ActionSelectorComponent::get)
					.map(it -> it.with(state))
					.orElseGet(() -> DomainObjectIdentity.of(state))));
			}
		});
		configurer.configure(/*updateSelectorForAncestors*/new ModelActionWithInputs.ModelAction2<ModelComponentTag<ConfigurableTag>, ParentComponent>() {
			// ComponentFromEntity<ParentComponent> read-only all
			// ComponentFromEntity<ActionSelectorComponent> read-write self
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, ParentComponent parent) {
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
		});
		configurer.configure(/*updateSelectorForSelf*/new ModelActionWithInputs.ModelAction1<ModelComponentTag<ConfigurableTag>>() {
			// ComponentFromEntity<ActionSelectorComponent> read-write self
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag) {
				entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
					.map(ActionSelectorComponent::get)
					.map(it -> it.with(new SelfRef(entity.getId())))
					.orElseGet(() -> DomainObjectIdentity.of(new SelfRef(entity.getId())))));
			}
		});
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
			val identity = it.findComponentNullable(componentOf(ModelSpecComponent.class));
			if (identity == null) {
				return false;
			} else {
				return ((Spec<DomainObjectIdentity>) identity.get()).isSatisfiedBy(component.get());
			}
		};
	}

	private static Consumer<ModelNode> executeAction(ModelActionComponent component) {
		return it -> component.get().execute(it);
	}

	private void allEntities(Consumer<? super Iterable<ModelNode>> action) {
		action.accept(ImmutableList.copyOf(allConfigurableEntities));
	}

	private static Predicate<ModelNode> onlyMatching(ModelSpecComponent component) {
		return entity -> {
			val selector = entity.findComponentNullable(componentOf(ActionSelectorComponent.class));
			if (selector == null) {
				return false;
			} else {
				return ((Spec<DomainObjectIdentity>) component.get()).isSatisfiedBy(selector.get());
			}
		};
	}

	// ComponentFromEntity<ActionSelectorComponent> read-write self
	public static <T extends ModelComponent> ModelAction updateSelectorForTag(Class<T> componentType) {
		return new ModelActionWithInputs.ModelAction2<ModelComponentTag<ConfigurableTag>, T>(ModelTags.referenceOf(ConfigurableTag.class), ModelComponentReference.of(componentType)) {
			protected void execute(ModelNode entity, ModelComponentTag<ConfigurableTag> tag, T component) {
				entity.addComponent(new ActionSelectorComponent(entity.findComponent(componentOf(ActionSelectorComponent.class))
					.map(ActionSelectorComponent::get)
					.map(it -> it.with(valueOf(component)))
					.orElseGet(() -> DomainObjectIdentity.of(valueOf(component)))));
			}

			private Object valueOf(T component) {
				val unpacked = DeferredUtils.nestableDeferred().unpack(component);
				if (unpacked instanceof Iterable) {
					return ImmutableSet.copyOf((Iterable<?>) unpacked);
				} else {
					return unpacked;
				}
			}
		};
	}
}
