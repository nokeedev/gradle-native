package dev.nokee.utils.internal;

import com.google.common.base.Preconditions;
import dev.nokee.utils.DeferredUtils;
import lombok.Value;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Task;

import java.util.function.Supplier;

import static dev.nokee.utils.DeferredUtils.unpack;

@Value
public class AssertingTaskAction implements Action<Task> {
	Supplier<Boolean> expression;
	Object errorMessage;

	@Override
	public void execute(Task task) {
		val expression = Preconditions.checkNotNull(this.expression.get());
		if (!expression) {
			throw new IllegalArgumentException(String.valueOf(unpack(errorMessage)));
		}
	}
}
