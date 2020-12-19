package dev.nokee.model.internal.core;

import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;

@Subject(ModelPath.class)
class ModelPath_GetNameTest {
	@Test
	void canGetPathNameOfNonRootPath() {
		assertThat(path("po.ta.to").getName(), equalTo("to"));
	}

	@Test
	void canGetPathNameOfRootPath() {
		assertThat(root().getName(), emptyString());
	}
}
