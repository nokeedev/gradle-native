package dev.nokee.core.exec;

/**
 * The result of the execution of a command line tool.
 * The tool has already exited and produce no more output.
 *
 * @since 0.4
 */
public interface CommandLineToolExecutionResult {
	CommandLineToolLogContent getStandardOutput();

	/**
	 * Returns the exit value of the process.
	 *
	 * @return the integer value return when the process exited.
	 */
	int getExitValue();

	/**
	 * Throws an {@link ExecException} if the process exited with a non-zero exit value.
	 *
	 * @return this instance
	 * @throws ExecException if the process exited with a non-zero exit value
	 */
	CommandLineToolExecutionResult assertNormalExitValue() throws ExecException;

	/**
	 * Throws an {@link ExecException} if the process exited with a exit value different than what was expected.
	 *
	 * @param expectedExitValue the expected exit value of the process.
	 * @return this instance
	 * @throws ExecException if the process exited with an exit value different than what was expected.
	 */
	CommandLineToolExecutionResult assertExitValueEquals(int expectedExitValue) throws ExecException;
}
