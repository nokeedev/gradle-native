package dev.nokee.utils;

import static dev.nokee.utils.ActionTestUtils.doSomething;

class ActionUtils_ActionTest implements ActionTester<Object> {
	@Override
	public ActionUtils.Action<Object> createSubject() {
		return ActionUtils.Action.of(doSomething());
	}
}
