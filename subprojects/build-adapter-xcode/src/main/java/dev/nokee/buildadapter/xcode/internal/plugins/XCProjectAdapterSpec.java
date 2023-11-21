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

package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCTargetReference;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

public /*final*/ abstract class XCProjectAdapterSpec extends ModelElementSupport implements Component, VariantAwareComponentMixIn<XCTargetAdapterSpec> {
	public XCProjectAdapterSpec(Factory<DefaultVariantDimensions> dimensionsFactory, VariantViewFactory variantsFactory) {
		getExtensions().add("dimensions", dimensionsFactory.create());
		getExtensions().add("variants", variantsFactory.create(XCTargetAdapterSpec.class));
	}

	public abstract Property<XCProjectReference> getProjectLocation();

	public abstract Property<XCTargetReference> getTarget();

	public abstract SetProperty<String> getConfigurations();

	@Override
	protected String getTypeName() {
		return "XCTarget";
	}
}
