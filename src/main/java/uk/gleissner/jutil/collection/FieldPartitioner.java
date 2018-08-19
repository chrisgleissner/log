/*
 * Copyright (C) 2018 Christian Gleissner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.gleissner.jutil.collection;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import static java.util.Optional.empty;

/**
 * Partitions F(ield) instances across newly created O(bject) instances.
 */
public class FieldPartitioner {

    @FunctionalInterface
    public interface ObjectBuilder<O> {
        O build();
    }

    @FunctionalInterface
    public interface FieldAdder<O, F> {
        Optional<O> tryToAdd(O o, F f);
    }

    public static <O, F> Collection<O> partition(ObjectBuilder<O> objectBuilder,
                                                 FieldAdder<O, F> fieldAdder,
                                                 Collection<F> fields) {
        Collection<O> os = new LinkedList<>();
        O o = objectBuilder.build();
        Optional<O> addResult = empty();

        for (F f : fields) {
            addResult = fieldAdder.tryToAdd(o, f);
            if (!addResult.isPresent()) {
                os.add(o);
                o = objectBuilder.build();
                addResult = fieldAdder.tryToAdd(o, f);
            }
            o = addResult.get();
        }

        if (addResult.isPresent())
            os.add(o);

        return os;
    }
}
