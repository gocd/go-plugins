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

import java.util.Collection;
import java.util.Iterator;

public class ListUtil {
    public static String join(Collection c) {
        return join(c, ", ");
    }

    public static String join(Collection c, String join) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<Object> iterator = c.iterator(); iterator.hasNext(); ) {
            sb.append(iterator.next());
            if (iterator.hasNext()) {
                sb.append(join);
            }
        }
        return sb.toString();
    }
}
