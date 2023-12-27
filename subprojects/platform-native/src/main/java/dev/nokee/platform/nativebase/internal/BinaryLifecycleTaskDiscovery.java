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

package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.internal.DiscoveredElements;
import dev.nokee.model.internal.DiscoveryService;
import dev.nokee.model.internal.ModelObjectIdentity;
import dev.nokee.model.internal.discover.Discovery;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.Task;

import java.util.Collections;
import java.util.List;

public /*final*/ class BinaryLifecycleTaskDiscovery implements Discovery {
	@Override
	public <T> List<DiscoveryService.DiscoveredEl> discover(ModelType<T> discoveringType) {
		return ImmutableList.of(
			new DiscoveryService.DiscoveredEl() {
				@Override
				public ElementName getName() {
					return TaskName.of("executable");
				}

				@Override
				public ModelType<?> getType() {
					return ModelType.of(Task.class);
				}

				@Override
				public DiscoveryService.Scope getTarget() {
					return DiscoveryService.Scope.Registered;
				}

				@Override
				public List<DiscoveryService.RealizedDiscoveredEl> execute(ModelObjectIdentity<?> identity) {
					if (identity.getType().isSubtypeOf(NativeVariantSpec.class) && identity.getIdentifier() instanceof VariantIdentifier) {
						final VariantIdentifier variantIdentifier = (VariantIdentifier) identity.getIdentifier();
						final BuildVariantInternal buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
						return buildVariant.findAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).filter(BinaryLinkage::isExecutable).map(__ -> {
							return Collections.<DiscoveryService.RealizedDiscoveredEl>singletonList(new DiscoveryService.RealizedDiscoveredEl() {
								@Override
								public DiscoveredElements.Element toElement(DiscoveredElements.Element parent) {
									return new DiscoveredElements.Element(ModelObjectIdentity.ofIdentity(variantIdentifier.child(getName()), getType()), parent);
								}
							});
						}).orElse(Collections.emptyList());
					} else {
						return Collections.emptyList();
					}
				}
			},
			new DiscoveryService.DiscoveredEl() {
				@Override
				public ElementName getName() {
					return TaskName.of("sharedLibrary");
				}

				@Override
				public ModelType<?> getType() {
					return ModelType.of(Task.class);
				}

				@Override
				public DiscoveryService.Scope getTarget() {
					return DiscoveryService.Scope.Registered;
				}

				@Override
				public List<DiscoveryService.RealizedDiscoveredEl> execute(ModelObjectIdentity<?> identity) {
					if (identity.getType().isSubtypeOf(NativeVariantSpec.class) && identity.getIdentifier() instanceof VariantIdentifier) {
						final VariantIdentifier variantIdentifier = (VariantIdentifier) identity.getIdentifier();
						final BuildVariantInternal buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
						return buildVariant.findAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).filter(BinaryLinkage::isShared).map(__ -> {
							return Collections.<DiscoveryService.RealizedDiscoveredEl>singletonList(new DiscoveryService.RealizedDiscoveredEl() {
								@Override
								public DiscoveredElements.Element toElement(DiscoveredElements.Element parent) {
									return new DiscoveredElements.Element(ModelObjectIdentity.ofIdentity(variantIdentifier.child(getName()), getType()), parent);
								}
							});
						}).orElse(Collections.emptyList());
					} else {
						return Collections.emptyList();
					}
				}
			},
			new DiscoveryService.DiscoveredEl() {
				@Override
				public ElementName getName() {
					return TaskName.of("staticLibrary");
				}

				@Override
				public ModelType<?> getType() {
					return ModelType.of(Task.class);
				}

				@Override
				public DiscoveryService.Scope getTarget() {
					return DiscoveryService.Scope.Registered;
				}

				@Override
				public List<DiscoveryService.RealizedDiscoveredEl> execute(ModelObjectIdentity<?> identity) {
					if (identity.getType().isSubtypeOf(NativeVariantSpec.class) && identity.getIdentifier() instanceof VariantIdentifier) {
						final VariantIdentifier variantIdentifier = (VariantIdentifier) identity.getIdentifier();
						final BuildVariantInternal buildVariant = (BuildVariantInternal) variantIdentifier.getBuildVariant();
						return buildVariant.findAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS).filter(BinaryLinkage::isStatic).map(__ -> {
							return Collections.<DiscoveryService.RealizedDiscoveredEl>singletonList(new DiscoveryService.RealizedDiscoveredEl() {
								@Override
								public DiscoveredElements.Element toElement(DiscoveredElements.Element parent) {
									return new DiscoveredElements.Element(ModelObjectIdentity.ofIdentity(variantIdentifier.child(getName()), getType()), parent);
								}
							});
						}).orElse(Collections.emptyList());
					} else {
						return Collections.emptyList();
					}
				}
			}
		);
	}
}
