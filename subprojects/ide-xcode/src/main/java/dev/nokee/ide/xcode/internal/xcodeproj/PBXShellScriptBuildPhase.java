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
package dev.nokee.ide.xcode.internal.xcodeproj;

import com.dd.plist.NSString;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Build phase which represents running a shell script.
 */
public final class PBXShellScriptBuildPhase extends PBXBuildPhase {
    private List<String> inputPaths;
    private List<String> outputPaths;
    @Nullable private String shellPath;
    @Nullable private String shellScript;

    private static final NSString DEFAULT_SHELL_PATH = new NSString("/bin/sh");
    private static final NSString DEFAULT_SHELL_SCRIPT = new NSString("");

    public PBXShellScriptBuildPhase() {
        this.inputPaths = Lists.newArrayList();
        this.outputPaths = Lists.newArrayList();
    }

    @Override
    public String isa() {
        return "PBXShellScriptBuildPhase";
    }

    /**
     * Returns the list (possibly empty) of files passed as input to the shell script.
     * May not be actual paths, because they can have variable interpolations.
     */
    public List<String> getInputPaths() {
        return inputPaths;
    }

    /**
     * Returns the list (possibly empty) of files created as output of the shell script.
     * May not be actual paths, because they can have variable interpolations.
     */
    public List<String> getOutputPaths() {
        return outputPaths;
    }

    /**
     * Returns the path to the shell under which the script is to be executed.
     * Defaults to "/bin/sh".
     */
    @Nullable
    public String getShellPath() {
        return shellPath;
    }

    /**
     * Sets the path to the shell under which the script is to be executed.
     */
    public void setShellPath(String shellPath) {
        this.shellPath = shellPath;
    }

    /**
     * Gets the contents of the shell script to execute under the shell
     * returned by {@link #getShellPath()}.
     */
    @Nullable
    public String getShellScript() {
        return shellScript;
    }

    /**
     * Sets the contents of the script to execute.
     */
    public void setShellScript(String shellScript) {
        this.shellScript = shellScript;
    }
}
