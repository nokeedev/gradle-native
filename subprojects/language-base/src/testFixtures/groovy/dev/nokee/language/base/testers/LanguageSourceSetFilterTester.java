package dev.nokee.language.base.testers;

import dev.nokee.internal.testing.FileSystemWorkspace;
import dev.nokee.internal.testing.utils.ClosureTestUtils;
import dev.nokee.language.base.LanguageSourceSet;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import static dev.nokee.internal.testing.FileSystemWorkspace.newFiles;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public interface LanguageSourceSetFilterTester<T extends LanguageSourceSet> {
	T createSubject();

	@Test
	default void canConfigureFilterUsingAction() {
		Action<PatternFilterable> action = Cast.uncheckedCast(Mockito.mock(Action.class));
		val subject = createSubject().filter(action);
		verify(action, times(1)).execute(subject.getFilter());
	}

	@Test
	default void canConfigureFilterUsingClosure() {
		Consumer<PatternFilterable> action = Cast.uncheckedCast(Mockito.mock(Consumer.class));
		val subject = createSubject().filter(ClosureTestUtils.adaptToClosure(action));
		verify(action, times(1)).accept(subject.getFilter());
	}

	@Test
	default void honorsIncludeFilterChangesExistingFileTree(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().from(a.newFile("f1"), a.newFile("f2"));
		val fileTree = subject.getAsFileTree();
		assertThat(fileTree, containsInAnyOrder(a.file("f1"), a.file("f2")));
		subject.getFilter().include("f1");
		assertThat(fileTree, containsInAnyOrder(a.file("f1")));
	}

	@Test
	default void newFileTreeHonorsCurrentFilter(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		val subject = createSubject().from(a.newFile("f1"), a.newFile("f2")).filter(p -> p.exclude("f1"));
		assertThat(subject.getAsFileTree(), containsInAnyOrder(a.file("f2")));
	}

	@Test
	default void honorsFiltersFromFileTree(@TempDir File temporaryDirectory) throws IOException {
		val a = new FileSystemWorkspace(temporaryDirectory);
		assertThat(createSubject().from(a.fileTree(newFiles(a.rootDirectory())).matching(p -> p.include("f1"))).getAsFileTree(),
			containsInAnyOrder(a.file("f1")));
	}

	@Test
	default void canConfigureFiltersFluently() {
		assertThat(createSubject()
			.filter(p -> p.include("*.c"))
			.filter(ClosureTestUtils.<PatternFilterable>adaptToClosure(p -> p.include("*.conly")))
			.getFilter()
			.getIncludes(),
			containsInAnyOrder("*.c", "*.conly"));
	}
}
