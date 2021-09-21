/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase;

import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetLinkageFactory;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import org.gradle.api.provider.SetProperty;

/**
 * Represents a component that targets different library linkage.
 *
 * @since 0.5
 */
public interface TargetLinkageAwareComponent {
	/**
	 * Specifies the target linkage this component should be built for.
	 * The {@link #getLinkages()} property (see {@link TargetLinkageFactory}) can be used to construct common linkages.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetLinkages = [linkages.shared, linkages.static]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetLinkage}, never null.
	 */
	SetProperty<TargetLinkage> getTargetLinkages();

	/**
	 * Returns a factory to create target linkages when configuring {@link #getTargetLinkages()}.
	 *
	 * @return a {@link TargetLinkageFactory} for creating {@link TargetLinkage} instance, never null.
	 */
	default TargetLinkageFactory getLinkages() {
		return NativeRuntimeBasePlugin.TARGET_LINKAGE_FACTORY;
	}
}
