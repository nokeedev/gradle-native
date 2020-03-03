/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.docs.samples;

import java.util.List;
import java.util.Optional;

public class Command {
    private final String executable;
    private final Optional<String> executionSubdirectory;
    private final List<String> args;
    private final List<String> flags;
    private final Optional<String> expectedOutput;
    private final boolean expectFailure;
    private final boolean allowAdditionalOutput;
    private final boolean allowDisorderedOutput;
    private final List<String> userInputs;

    public Command(String executable, Optional<String> executionDirectory, List<String> args, List<String> flags, Optional<String> expectedOutput, boolean expectFailure, boolean allowAdditionalOutput, boolean allowDisorderedOutput, List<String> userInputs) {
        this.executable = executable;
        this.executionSubdirectory = executionDirectory;
        this.args = args;
        this.flags = flags;
        this.expectedOutput = expectedOutput;
        this.expectFailure = expectFailure;
        this.allowAdditionalOutput = allowAdditionalOutput;
        this.allowDisorderedOutput = allowDisorderedOutput;
        this.userInputs = userInputs;
    }

    public String getExecutable() {
        return executable;
    }

    public Optional<String> getExecutionSubdirectory() {
        return executionSubdirectory;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getFlags() {
        return flags;
    }

    public Optional<String> getExpectedOutput() {
        return expectedOutput;
    }

    /**
     * @return true if executing the scenario build is expected to fail.
     */
    public boolean isExpectFailure() {
        return expectFailure;
    }

    /**
     * @return true if output lines other than those provided are allowed.
     */
    public boolean isAllowAdditionalOutput() {
        return allowAdditionalOutput;
    }

    /**
     * @return true if actual output lines can differ in order from expected.
     */
    public boolean isAllowDisorderedOutput() {
        return allowDisorderedOutput;
    }

    /**
     * @return a list of user inputs to provide to the command
     */
    public List<String> getUserInputs() {
        return userInputs;
    }
}
