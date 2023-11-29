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

package dev.nokee.platform.swift.internal;

import dev.nokee.language.swift.internal.plugins.SupportSwiftSourceSetTag;
import dev.nokee.language.swift.internal.plugins.SwiftSourcesMixIn;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.VariantComponentSpec;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.internal.BaseNativeLibraryComponentSpec;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryVariant;
import dev.nokee.platform.swift.SwiftLibrary;

public /*final*/ abstract class SwiftLibrarySpec extends BaseNativeLibraryComponentSpec implements Component, SwiftLibrary
	, VariantComponentSpec<DefaultNativeLibraryVariant>
	, VariantAwareComponentMixIn<NativeLibrary>
	, SwiftSourcesMixIn
	, SupportSwiftSourceSetTag
{
	@Override
	protected String getTypeName() {
		return "Swift library";
	}
}
