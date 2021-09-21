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
package dev.nokee.buildadapter.cmake.internal;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import lombok.val;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Predicates.not;

public class GitBasedGroupIdSupplier implements Supplier<String> {
	private static final Logger LOGGER = Logging.getLogger(GitBasedGroupIdSupplier.class);
	private static final LoadingCache<File, String> GROUP_ID_CACHE = CacheBuilder.newBuilder()
		.maximumSize(10)
		.build(
			new CacheLoader<File, String>() {
				@Override
				public String load(File gitRepositoryDirectory) {
					return computeGroupIdFromGitRepository(gitRepositoryDirectory);
				}
			});
	private final File gitRepositoryDirectory;

	public GitBasedGroupIdSupplier(File gitRepositoryDirectory) {
		this.gitRepositoryDirectory = gitRepositoryDirectory;
	}

	private static String computeGroupIdFromGitRepository(File gitRepositoryDirectory) {
		try (val git = Git.open(gitRepositoryDirectory)) {
			val remotes = git.remoteList().call();
			if (remotes.isEmpty()) {
				throw new RuntimeException(String.format("Unable to compute group ID from Git repository '%s' because no remotes are available.", gitRepositoryDirectory.getAbsolutePath()));
			}
			val uris = remotes.iterator().next().getURIs();
			if (uris.isEmpty()) {
				throw new RuntimeException(String.format("Unable to compute group ID from Git repository '%s' because no remote URIs are available.", gitRepositoryDirectory.getAbsolutePath()));
			}
			val uri = uris.iterator().next();
			val groupIdSegment = new ArrayList<String>();
			groupIdSegment.addAll(ImmutableList.copyOf(uri.getHost().split("\\.")));
			Collections.reverse(groupIdSegment);
			groupIdSegment.addAll(Arrays.stream(uri.getPath().split("/")).filter(not(String::isEmpty)).collect(Collectors.toList()));
			String groupId = String.join(".", groupIdSegment);
			LOGGER.info(String.format("The group ID for Git repository '%s' is '%s'.", gitRepositoryDirectory.getAbsolutePath(), groupId));
			return groupId;
		} catch (IOException | GitAPIException e) {
			throw new RuntimeException(String.format("Unable to compute group ID from Git repository '%s' because an exception was thrown.", gitRepositoryDirectory.getAbsolutePath()), e);
		}
	}

	@Override
	public String get() {
		return GROUP_ID_CACHE.getUnchecked(gitRepositoryDirectory);
	}
}
