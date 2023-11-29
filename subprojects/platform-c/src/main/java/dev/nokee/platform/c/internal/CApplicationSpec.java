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

package dev.nokee.platform.c.internal;

import dev.nokee.language.c.internal.CSourcesMixIn;
import dev.nokee.language.c.internal.plugins.SupportCSourceSetTag;
import dev.nokee.language.nativebase.internal.PrivateHeadersMixIn;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.c.CApplication;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponentSpec;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;

public  /*final*/ abstract class CApplicationSpec extends BaseNativeApplicationComponentSpec implements Component, CApplication
	, VariantComponentSpec<DefaultNativeApplicationVariant>
	, VariantAwareComponentMixIn<NativeApplication>
	, PrivateHeadersMixIn
	, CSourcesMixIn
	, SupportCSourceSetTag
{
	@Override
	protected String getTypeName() {
		return "C application";
	}
}
