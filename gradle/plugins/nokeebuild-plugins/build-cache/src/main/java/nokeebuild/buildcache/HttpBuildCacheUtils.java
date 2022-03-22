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
package nokeebuild.buildcache;

import org.gradle.api.Action;
import org.gradle.caching.configuration.BuildCache;
import org.gradle.caching.http.HttpBuildCache;
import org.gradle.caching.http.HttpBuildCacheCredentials;

import java.util.function.BiConsumer;

final class HttpBuildCacheUtils {
	public static <SELF extends HttpBuildCache> Action<SELF> url(BiConsumer<? super SELF, ? super Property<String>> action) {
		return it -> action.accept(it, new Property<>(it::setUrl));
	}

	public static Action<HttpBuildCache> credentials(Action<? super HttpBuildCacheCredentials> action) {
		return it -> it.credentials(action);
	}

	public static <SELF extends BuildCache> Action<SELF> enabled(BiConsumer<? super SELF, ? super Property<Boolean>> action) {
		return it -> action.accept(it, new Property<>(it::setEnabled));
	}

	public static <SELF extends BuildCache> Action<SELF> push(BiConsumer<? super SELF, ? super Property<Boolean>> action) {
		return it -> action.accept(it, new Property<>(it::setPush));
	}
}
