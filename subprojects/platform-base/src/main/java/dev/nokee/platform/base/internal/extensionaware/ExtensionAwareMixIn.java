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
package dev.nokee.platform.base.internal.extensionaware;

import com.google.common.collect.ImmutableMap;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import groovy.lang.GroovyRuntimeException;
import lombok.val;
import org.gradle.api.internal.provider.PropertyInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtensionsSchema;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.internal.metaobject.PropertyAccess;
import org.gradle.internal.metaobject.PropertyMixIn;

import java.util.Map;

@DomainObjectEntities.Tag(ExtensionAwareMixIn.Tag.class)
public interface ExtensionAwareMixIn extends ExtensionAware, PropertyMixIn {
	@Override
	default ExtensionContainer getExtensions() {
		return ModelNodes.of(this).get(ExtensionAwareComponent.class).get();
	}

	@Override
	default PropertyAccess getAdditionalProperties() {
		return new PropertyAccess() {
			@Override
			public boolean hasProperty(String name) {
				return getExtensions().findByName(name) != null;
			}

			@Override
			public DynamicInvokeResult tryGetProperty(String name) {
				val result = getExtensions().findByName(name);
				if (result == null) {
					return DynamicInvokeResult.notFound();
				} else {
					return DynamicInvokeResult.found(result);
				}
			}

			@Override
			public DynamicInvokeResult trySetProperty(String name, Object newValue) {
				for (ExtensionsSchema.ExtensionSchema element : getExtensions().getExtensionsSchema().getElements()) {
					if (element.getName().equals(name)) {
						if (element.getPublicType().equals(Property.class)) {
							((PropertyInternal<?>) element).setFromAnyValue(newValue);
						} else if (element.getPublicType().equals(ListProperty.class)) {
							((PropertyInternal<?>) element).setFromAnyValue(newValue);
						} else if (element.getPublicType().equals(SetProperty.class)) {
							((PropertyInternal<?>) element).setFromAnyValue(newValue);
						} else if (element.getPublicType().equals(MapProperty.class)) {
							((PropertyInternal<?>) element).setFromAnyValue(newValue);
						} else {
							throw new GroovyRuntimeException(String.format("Cannot set the value of read-only property '%s' for %s of type %s.", name, this, getClass().getName()));
						}
						return DynamicInvokeResult.found();
					}
				}
				return DynamicInvokeResult.notFound();
			}

			@Override
			public Map<String, ?> getProperties() {
				val builder = ImmutableMap.<String, Object>builder();
				for (ExtensionsSchema.ExtensionSchema element : getExtensions().getExtensionsSchema().getElements()) {
					builder.put(element.getName(), getExtensions().getByName(element.getName()));
				}
				return builder.build();
			}
		};
	}

	interface Tag extends ModelTag {}
}
