package dev.gradleplugins.exemplarkit.testers;

import dev.gradleplugins.exemplarkit.Step;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

public abstract class StepTester {
	public abstract Step getStepUnderTest();
	public abstract String getExpectedExecutable();

	public List<String> getExpectedArguments() {
		return Collections.emptyList();
	}

	public Map<String, Object> getExpectedAttributes() {
		return Collections.emptyMap();
	}

	@Nullable
	public String getExpectedOutput() {
		return null;
	}

	@Test
	void hasExecutable() {
		assertThat(getStepUnderTest().getExecutable(), equalTo(getExpectedExecutable()));
	}

	@Test
	void hasArguments() {
		assertThat(getStepUnderTest().getArguments(), contains(getExpectedArguments().toArray(new String[0])));
	}

	@Test
	void hasAttributes() {
		assertThat(getStepUnderTest().getAttributes(), equalTo(getExpectedAttributes()));
	}

	@Test
	void hasOutput() {
		assertThat(getStepUnderTest().getOutput().orElse(null), equalTo(getExpectedOutput()));
	}
}
