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

import java.util.List;
import java.util.stream.Collectors;

public class ListingResponse implements Response {
	private final List<String> versions;

	public ListingResponse(List<String> versions) {
		this.versions = versions;
	}

	@Override
	public String getContentType() {
		return "text/html";
	}

	@Override
	public String getContent() {
		return "<html>\n" +
			"<head></head>\n" +
			"<body>\n" +
			versions.stream().map(it -> "<pre><a href=\"" + it + "/\">" + it + "/</a></pre>").collect(Collectors.joining("\n")) + "\n" +
			"</body>\n" +
			"</html>";
	}
}
