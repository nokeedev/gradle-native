/*
 * Copyright 2021 the original author or authors.
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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelComponentType;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.nativebase.internal.dependencies.ConfigurationUtilsEx;
import jdk.nashorn.internal.runtime.regexp.joni.Config;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;

@AutoFactory
public final class AttachAttributesToConfigurationRule extends ModelActionWithInputs.ModelAction3<DomainObjectIdentifier, Configurable<Configuration>, BuildVariant> {
	private final DomainObjectIdentifier identifier;
	private final ObjectFactory objects;

	public AttachAttributesToConfigurationRule(DomainObjectIdentifier identifier, Class<? extends Configurable<Configuration>> configurationType, @Provided ObjectFactory objects) {
		super(ModelComponentReference.of((Class<DomainObjectIdentifier>) identifier.getClass()), ModelComponentReference.of((Class<Configurable<Configuration>>) configurationType), ModelComponentReference.ofAny(ModelComponentType.componentOf(BuildVariant.class)));
		this.identifier = identifier;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, DomainObjectIdentifier identifier, Configurable<Configuration> configuration, BuildVariant buildVariant) {
		if (identifier.equals(this.identifier)) {
			configuration.configure(ConfigurationUtilsEx.configureIncomingAttributes((BuildVariantInternal) buildVariant, objects));
			configuration.configure(ConfigurationUtilsEx::configureAsGradleDebugCompatible);
		}
	}
}
