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

import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.runtime.nativebase.TargetLinkage;
import org.gradle.api.provider.SetProperty;

public interface ModelBackedTargetLinkageAwareComponentMixIn extends TargetLinkageAwareComponent {
	@Override
	default SetProperty<TargetLinkage> getTargetLinkages() {
		return ModelProperties.getProperty(this, "targetLinkages").as(SetProperty.class).get();
	}
}
