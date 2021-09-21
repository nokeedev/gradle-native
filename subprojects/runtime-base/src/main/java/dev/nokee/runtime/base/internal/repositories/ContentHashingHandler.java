/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.runtime.base.internal.repositories;

import com.google.common.hash.Hashing;
import org.apache.commons.io.FilenameUtils;

import java.nio.charset.Charset;
import java.util.Optional;

public class ContentHashingHandler implements RouteHandler {
	private final RouteHandler delegate;

	ContentHashingHandler(RouteHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public Optional<Response> handle(String target) {
		if (FilenameUtils.isExtension(target, "sha1")) {
			return delegate.handle(FilenameUtils.removeExtension(target)).map(ContentHashingHandler::sha1);
		}
		return delegate.handle(target);
	}

	private static Response sha1(Response content) {
		return new StringResponse(Hashing.sha1().hashString(content.getContent(), Charset.defaultCharset()).toString());
	}
}
