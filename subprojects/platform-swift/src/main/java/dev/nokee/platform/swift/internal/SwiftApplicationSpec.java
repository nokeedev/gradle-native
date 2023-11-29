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
import dev.nokee.platform.nativebase.internal.BaseNativeApplicationComponentSpec;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationVariant;
import dev.nokee.platform.swift.SwiftApplication;

public /*final*/ abstract class SwiftApplicationSpec extends BaseNativeApplicationComponentSpec<DefaultNativeApplicationVariant> implements SwiftApplication
	, SwiftSourcesMixIn
	, SupportSwiftSourceSetTag
{
	@Override
	protected String getTypeName() {
		return "Swift application";
	}
}
