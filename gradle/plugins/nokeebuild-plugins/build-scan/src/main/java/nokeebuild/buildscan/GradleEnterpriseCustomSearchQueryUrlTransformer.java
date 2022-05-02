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
package nokeebuild.buildscan;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

final class GradleEnterpriseCustomSearchQueryUrlTransformer implements UnaryOperator<String> {
	private final Map<String, String> search;

	private GradleEnterpriseCustomSearchQueryUrlTransformer(Map<String, String> search) {
		this.search = search;
	}

	@Override
	public String apply(String serverUrl) {
		String query = search.entrySet()
			.stream()
			.map(entry -> String.format("search.names=%s&search.values=%s", urlEncode(entry.getKey()), urlEncode(entry.getValue())))
			.collect(Collectors.joining("&"));
		return appendUrlSeparatorIfMissing(serverUrl) + "scans?" + query;
	}

	private static String appendUrlSeparatorIfMissing(String str) {
		return str.endsWith("/") ? str : str + "/";
	}

	private static String urlEncode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static Function<String, String> toCustomSearchUrl(Map<String, String> search) {
		return new GradleEnterpriseCustomSearchQueryUrlTransformer(search);
	}
}
