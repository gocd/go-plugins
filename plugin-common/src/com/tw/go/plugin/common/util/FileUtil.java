/*************************GO-LICENSE-START*********************************
 * Copyright 2014 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *************************GO-LICENSE-END***********************************/

package com.tw.go.plugin.common.util;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static boolean isFolderInsideSandbox(String path) throws IOException {
        File fileAtPath = new File(path);
        if (fileAtPath.isAbsolute() || !isSubdirectoryOf(new File("."), fileAtPath)) {
            return false;
        }
        return true;
    }

    private static boolean isSubdirectoryOf(File parent, File subdirectory) throws IOException {
        File parentFile = parent.getCanonicalFile();
        File current = subdirectory.getCanonicalFile();
        while (current != null) {
            if (current.equals(parentFile)) {
                return true;
            }
            current = current.getParentFile();
        }
        return false;
    }
}
