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
package dev.nokee.buildadapter.xcode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.nokee.xcode.AsciiPropertyListReader;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.BuildFileAwareBuilder;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXFrameworksBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXHeadersBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXResourcesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXSourcesBuildPhase;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.targets.BuildPhaseAwareBuilder;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.TaskDependenciesAwareBuilder;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.PBXObjectArchiver;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProjReader;
import dev.nokee.xcode.project.PBXProjWriter;
import lombok.val;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static com.google.common.collect.MoreCollectors.onlyElement;

public final class PBXProjectTestUtils {
	public static Consumer<Path> mutateProject(Function<PBXProject, PBXProject> action) {
		return path -> {
			assert path.getFileName().toString().endsWith(".xcodeproj");
			try (val reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(path.resolve("project.pbxproj"))))) {
				val project = new PBXObjectUnarchiver().decode(reader.read());

				val newProject = action.apply(project);

				try (val writer = new PBXProjWriter(Files.newBufferedWriter(path.resolve("project.pbxproj")))) {
					writer.write(new PBXObjectArchiver().encode(newProject));
				}
			} catch (
				IOException e) {
				throw new UncheckedIOException(e);
			}
		};
	}

	public static UnaryOperator<PBXProject> mainGroup(BiFunction<? super PBXProject, ? super PBXGroup, ? extends PBXGroup> action) {
		return project -> project.toBuilder().mainGroup(action.apply(project, project.getMainGroup())).build();
	}

	public static UnaryOperator<PBXProject> productsGroup(BiFunction<? super PBXProject, ? super PBXGroup, ? extends PBXGroup> action) {
		return mainGroup(children(matching(childNameOrPath("Products"), asGroup(action))));
	}

	public static <SELF> BiFunction<SELF, PBXGroup, PBXGroup> children(BiFunction<? super SELF, ? super List<GroupChild>, ? extends List<GroupChild>> action) {
		return (project, group) -> {
			return group.toBuilder().children(action.apply(project, group.getChildren())).build();
		};
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> matching(Predicate<? super E> predicate, BiFunction<? super SELF, ? super E, ? extends E> action) {
		return (self, values) -> {
			boolean found = false;
			val builder = ImmutableList.<E>builder();
			for (E value : values) {
				if (predicate.test(value)) {
					found = true;
					builder.add(action.apply(self, value));
				} else {
					builder.add(value);
				}
			}
			assert found : "no item matched predicate";
			return builder.build();
		};
	}

	public static Predicate<GroupChild> childName(String value) {
		return it -> it.getName().map(value::equals).orElse(false);
	}

	public static Predicate<GroupChild> childPath(String value) {
		return it -> it.getPath().map(value::equals).orElse(false);
	}

	public static Predicate<GroupChild> childNameOrPath(String value) {
		return childName(value).or(childPath(value));
	}

	public static Predicate<PBXReference> referenceName(String value) {
		return it -> it.getName().map(value::equals).orElse(false);
	}

	public static Predicate<PBXReference> referencePath(String value) {
		return it -> it.getPath().map(value::equals).orElse(false);
	}

	public static Predicate<PBXReference> referenceNameOrPath(String value) {
		return referenceName(value).or(referencePath(value));
	}

	public static BiFunction<PBXProject, GroupChild, GroupChild> asGroup(BiFunction<? super PBXProject, ? super PBXGroup, ? extends PBXGroup> action) {
		return (self, e) -> {
			assert e instanceof PBXGroup;
			return action.apply(self, (PBXGroup) e);
		};
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> add(E child) {
		return (__, values) -> {
			return ImmutableList.<E>builder().addAll(values).add(child).build();
		};
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> removeFirst() {
		return (__, values) -> ImmutableList.copyOf(Iterables.skip(values, 1));
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> removeLast() {
		return (__, values) -> values.subList(0, values.size() - 1);
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> removeIf(Predicate<? super E> predicate) {
		return (__, values) -> {
			val newValues = new ArrayList<>(values);
			boolean found = newValues.removeIf(predicate);
			assert found;
			return newValues;
		};
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> clear() {
		return (__, values) -> ImmutableList.of();
	}

	public static <SELF, E> BiFunction<SELF, List<E>, List<E>> add(Function<? super SELF, ? extends E> action) {
		return (self, values) -> {
			return ImmutableList.<E>builder().addAll(values).add(action.apply(self)).build();
		};
	}

	public static Function<PBXProject, PBXTargetDependency> targetDependencyTo(String targetName) {
		return self -> {
			return PBXTargetDependency.builder().target(self.getTargets().stream().filter(targetName(targetName)).collect(onlyElement())).targetProxy(PBXContainerItemProxy.builder().containerPortal(self).proxyType(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE).remoteInfo(targetName).remoteGlobalId(((Codeable) self).globalId()).build()).build();
		};
	}

	public static Function<PBXProject, PBXBuildFile> buildFileToProduct(String productName) {
		return self -> {
			val productGroup = (PBXGroup) self.getMainGroup().getChildren().stream().filter(childNameOrPath("Products")).collect(onlyElement());
			val productFile = (PBXFileReference) productGroup.getChildren().stream().filter(childNameOrPath(productName)).collect(onlyElement());
			return PBXBuildFile.ofFile(productFile);
		};
	}

	public static Function<PBXProject, PBXBuildFile> buildFileToProduct(Function<? super PBXProject, PBXBuildFile.FileReference> action) {
		return self -> PBXBuildFile.ofFile(action.apply(self));
	}

	public static UnaryOperator<PBXProject> targets(BiFunction<? super PBXProject, ? super List<PBXTarget>, ? extends List<PBXTarget>> action) {
		return project -> project.toBuilder().targets(action.apply(project, project.getTargets())).build();
	}

	public static UnaryOperator<PBXProject> targetNamed(String name, BiFunction<? super PBXProject, ? super PBXTarget, ? extends PBXTarget> action) {
		return targets(matching(targetName(name), action));
	}

	public static Predicate<PBXTarget> targetName(String name) {
		return it -> name.equals((it.getName()));
	}

	public static BiFunction<PBXProject, PBXTarget, PBXTarget> asLegacyTarget(BiFunction<? super PBXProject, ? super PBXLegacyTarget, ? extends PBXLegacyTarget> action) {
		return (self, target) -> action.apply(self, (PBXLegacyTarget) target);
	}

	public static BiFunction<PBXProject, PBXLegacyTarget, PBXLegacyTarget> buildToolPath(String value) {
		return (self, target) -> target.toBuilder().buildToolPath(value).build();
	}

	public static BiFunction<PBXProject, PBXLegacyTarget, PBXLegacyTarget> buildArgumentsString(String value) {
		return (self, target) -> target.toBuilder().buildArguments(value).build();
	}

	public static BiFunction<PBXProject, PBXTarget, PBXTarget> buildPhases(BiFunction<? super PBXProject, ? super List<PBXBuildPhase>, ? extends List<PBXBuildPhase>> action) {
		return (self, target) -> {
			val builder = target.toBuilder();
			((BuildPhaseAwareBuilder<?>) builder).buildPhases(action.apply(self, target.getBuildPhases()));
			return builder.build();
		};
	}

	public static BiFunction<PBXProject, PBXTarget, PBXTarget> dependencies(BiFunction<? super PBXProject, ? super List<PBXTargetDependency>, ? extends List<PBXTargetDependency>> action) {
		return (self, target) -> {
			val builder = target.toBuilder();
			((TaskDependenciesAwareBuilder<?>) builder).dependencies(action.apply(self, target.getDependencies()));
			return builder.build();
		};
	}

	public static <T extends PBXBuildPhase> BiFunction<PBXProject, T, T> files(BiFunction<? super PBXProject, ? super List<PBXBuildFile>, ? extends List<PBXBuildFile>> action) {
		return (self, buildPhase) -> {
			val builder = buildPhase.toBuilder();
			((BuildFileAwareBuilder<?>) builder).files(action.apply(self, buildPhase.getFiles()));
			@SuppressWarnings("unchecked")
			val result = (T) builder.build();
			return result;
		};
	}

	public static BiFunction<PBXProject, PBXShellScriptBuildPhase, PBXShellScriptBuildPhase> scriptPhaseName(String value) {
		return (self, buildPhase) -> buildPhase.toBuilder().name(value).build();
	}

	public static BiFunction<PBXProject, PBXBuildPhase, PBXShellScriptBuildPhase> asShellScript(BiFunction<? super PBXProject, ? super PBXShellScriptBuildPhase, ? extends PBXShellScriptBuildPhase> action) {
		return (self, buildPhase) -> {
			assert buildPhase instanceof PBXShellScriptBuildPhase;
			return action.apply(self, (PBXShellScriptBuildPhase) buildPhase);
		};
	}

	public static BiFunction<PBXProject, PBXBuildPhase, PBXCopyFilesBuildPhase> asCopyFiles(BiFunction<? super PBXProject, ? super PBXCopyFilesBuildPhase, ? extends PBXCopyFilesBuildPhase> action) {
		return (self, buildPhase) -> {
			assert buildPhase instanceof PBXCopyFilesBuildPhase;
			return action.apply(self, (PBXCopyFilesBuildPhase) buildPhase);
		};
	}

	public static BiFunction<PBXProject, PBXBuildPhase, PBXFrameworksBuildPhase> asFrameworks(BiFunction<? super PBXProject, ? super PBXFrameworksBuildPhase, ? extends PBXFrameworksBuildPhase> action) {
		return (self, buildPhase) -> {
			assert buildPhase instanceof PBXFrameworksBuildPhase;
			return action.apply(self, (PBXFrameworksBuildPhase) buildPhase);
		};
	}

	public static BiFunction<PBXProject, PBXBuildPhase, PBXHeadersBuildPhase> asHeaders(BiFunction<? super PBXProject, ? super PBXHeadersBuildPhase, ? extends PBXHeadersBuildPhase> action) {
		return (self, buildPhase) -> {
			assert buildPhase instanceof PBXHeadersBuildPhase;
			return action.apply(self, (PBXHeadersBuildPhase) buildPhase);
		};
	}

	public static BiFunction<PBXProject, PBXBuildPhase, PBXResourcesBuildPhase> asResources(BiFunction<? super PBXProject, ? super PBXResourcesBuildPhase, ? extends PBXResourcesBuildPhase> action) {
		return (self, buildPhase) -> {
			assert buildPhase instanceof PBXResourcesBuildPhase;
			return action.apply(self, (PBXResourcesBuildPhase) buildPhase);
		};
	}

	public static BiFunction<PBXProject, PBXBuildPhase, PBXSourcesBuildPhase> asSources(BiFunction<? super PBXProject, ? super PBXSourcesBuildPhase, ? extends PBXSourcesBuildPhase> action) {
		return (self, buildPhase) -> {
			assert buildPhase instanceof PBXSourcesBuildPhase;
			return action.apply(self, (PBXSourcesBuildPhase) buildPhase);
		};
	}

	public static BiFunction<PBXProject, PBXShellScriptBuildPhase, PBXShellScriptBuildPhase> shellPath(String value) {
		return (self, buildPhase) -> buildPhase.toBuilder().shellPath(value).build();
	}

	public static BiFunction<PBXProject, PBXShellScriptBuildPhase, PBXShellScriptBuildPhase> shellScript(String value) {
		return (self, buildPhase) -> buildPhase.toBuilder().shellScript(value).build();
	}

	public static BiFunction<PBXProject, PBXShellScriptBuildPhase, PBXShellScriptBuildPhase> inputPaths(BiFunction<? super PBXProject, ? super List<String>, ? extends List<String>> action) {
		return (self, buildPhase) -> buildPhase.toBuilder().inputPaths(action.apply(self, buildPhase.getInputPaths())).build();
	}
}
