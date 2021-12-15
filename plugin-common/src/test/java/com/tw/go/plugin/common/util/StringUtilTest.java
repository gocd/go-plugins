/*************************GO-LICENSE-START*********************************
 * Copyright 2021 ThoughtWorks, Inc.
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


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StringUtilTest {
    @Test
    public void shouldCheckAStringForBlank() {
        assertThat(StringUtil.isBlank("")).isEqualTo(true);
        assertThat(StringUtil.isBlank("   ")).isEqualTo(true);
        assertThat(StringUtil.isBlank(null)).isEqualTo(true);
        assertThat(StringUtil.isBlank(" a ")).isEqualTo(false);
    }

    @Test
    public void shouldCheckIfAStringIsNotBlank() {
        assertThat(StringUtil.isNotBlank("")).isEqualTo(false);
        assertThat(StringUtil.isNotBlank("   ")).isEqualTo(false);
        assertThat(StringUtil.isNotBlank(null)).isEqualTo(false);
        assertThat(StringUtil.isNotBlank(" a ")).isEqualTo(true);
    }
}
