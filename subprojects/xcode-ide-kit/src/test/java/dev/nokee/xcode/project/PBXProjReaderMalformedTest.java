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
package dev.nokee.xcode.project;

import dev.nokee.xcode.AsciiPropertyListReader;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PBXProjReaderMalformedTest {

	private static PBXProjReader newReader(String... lines) {
		return new PBXProjReader(new AsciiPropertyListReader(new InputStreamReader(new ByteArrayInputStream(content(lines)))));
	}

	private static byte[] content(String... lines) {
		return Arrays.stream(lines).collect(Collectors.joining(System.lineSeparator())).getBytes(StandardCharsets.UTF_8);
	}

	private static String[] withUTF8Header(String... lines) {
		return Stream.concat(Stream.of("// !$*UTF8*$!"), Arrays.stream(lines)).toArray(String[]::new);
	}

	private static String objectsWithRootObject(BiFunction<String, GlobalIDs, String> generator) {
		final GlobalIDs gids = new GlobalIDs();
		val rootObjectGid = gids.nextGlobalID();
		return "objects={" + generator.apply(rootObjectGid, gids) + ";};rootObject=" + rootObjectGid;
	}

	private static String objects(BiFunction<String, GlobalIDs, String> generator) {
		final GlobalIDs gids = new GlobalIDs();
		val rootObjectGid = gids.nextGlobalID();
		return "objects={" + generator.apply(rootObjectGid, gids) + ";}";
	}

	private static String classes() {
		return "classes={}";
	}

	private static String archiveVersion() {
		return archiveVersion(1);
	}

	private static String archiveVersion(int v) {
		return "archiveVersion=" + v;
	}

	private static String objectVersion() {
		return objectVersion(42);
	}

	private static String objectVersion(int v) {
		return "objectVersion=" + v;
	}

	private static String dict(Object... keys) {
		return "{" + Arrays.stream(keys).map(it -> it + ";").collect(Collectors.joining()) + "}";
	}

	private static BiFunction<String, GlobalIDs, String> emptyProject() {
		return (rootId, gids) -> rootId + "={isa=PBXProject;targets=();}";
	}

	@Test
	void doesNotFailOnMissing_archiveVersion() {
		newReader(withUTF8Header(dict(classes(), objectVersion(), objectsWithRootObject(emptyProject()))));
	}

	@Test
	void doesNotFailOnMissing_classes() {
		newReader(withUTF8Header(dict(archiveVersion(), objectVersion(), objectsWithRootObject(emptyProject()))));
	}

	@Test
	void doesNotFailOnMissing_objectVersion() {
		newReader(withUTF8Header(dict(archiveVersion(), classes(), objectsWithRootObject(emptyProject()))));
	}

	@Test
	void failsOnMissing_rootObject() {
		newReader(withUTF8Header(dict(archiveVersion(), classes(), objects(emptyProject()))));
	}

	@Test
	void failsOnMissing_objects() {
		newReader(withUTF8Header(dict(archiveVersion(), classes(), "rootObject=0003")));
	}
}
