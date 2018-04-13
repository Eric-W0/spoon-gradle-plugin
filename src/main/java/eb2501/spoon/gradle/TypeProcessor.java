/*
 * Copyright 2018 eb2501@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eb2501.spoon.gradle;

import spoon.processing.AbstractProcessor;
import spoon.reflect.declaration.CtType;

public class TypeProcessor extends AbstractProcessor<CtType> {

    @Override
    public void process(final CtType type) {
        final String name = type.getSimpleName();
        if (name.endsWith(Constants.SPOON_SUFFIX)) {
            type.setSimpleName(name.substring(0, name.length() - Constants.SPOON_SUFFIX.length()));
        }
    }
}
