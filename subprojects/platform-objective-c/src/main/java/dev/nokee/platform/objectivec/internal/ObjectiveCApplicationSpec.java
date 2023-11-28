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

package dev.nokee.platform.objectivec.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.nativebase.internal.PrivateHeadersMixIn;
import dev.nokee.language.objectivec.internal.ObjectiveCSourcesMixIn;
import dev.nokee.language.objectivec.internal.plugins.SupportObjectiveCSourceSetTag;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.internal.mixins.SourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponentSpec;
import dev.nokee.platform.objectivec.ObjectiveCApplication;

import javax.inject.Inject;

public  /*final*/ abstract class ObjectiveCApplicationSpec extends BaseNativeApplicationComponentSpec implements ObjectiveCApplication
	, VariantAwareComponentMixIn<NativeApplication>
	, SourceAwareComponentMixIn<SourceView<LanguageSourceSet>>
	, HasDevelopmentVariant<NativeApplication>
	, ObjectiveCSourcesMixIn
	, PrivateHeadersMixIn
{
	@Inject
	public ObjectiveCApplicationSpec(Factory<SourceView<LanguageSourceSet>> sourcesFactory) {
		getExtensions().add("sources", sourcesFactory.create());
		getExtensions().create("$objectiveCSupport", SupportObjectiveCSourceSetTag.class);
	}

	@Override
	protected String getTypeName() {
		return "Objective-C application";
	}
}
