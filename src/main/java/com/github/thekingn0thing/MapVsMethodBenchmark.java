/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.github.thekingn0thing;

import org.apache.commons.lang3.RandomStringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class MapVsMethodBenchmark {

    @Benchmark()
    public void callViaMap(final DataState dataState, final Blackhole blackhole) {
        dataState.callViaMap.run(dataState.data);
        blackhole.consume(dataState.target.getCalls());
    }

    @Benchmark()
    public void callViaMapWrapper(final DataState dataState, final Blackhole blackhole) {
        dataState.callViaMapWrapper.run(dataState.data);
        blackhole.consume(dataState.target.getCalls());
    }

    @Benchmark()
    public void directCallNotFinal(final DataState dataState, final Blackhole blackhole) {
        dataState.directCallNotFinal.run(dataState.data);
        blackhole.consume(dataState.target.getCalls());
    }

    @Benchmark()
    public void directCallFinal(final DataState dataState, final Blackhole blackhole) {
        dataState.directCallFinal.run(dataState.data);
        blackhole.consume(dataState.target.getCalls());
    }

    @Benchmark()
    public void directCallFinalNoInline(final DataState dataState, final Blackhole blackhole) {
        dataState.directCallFinalNoInline.run(dataState.data);
        blackhole.consume(dataState.target.getCalls());
    }


    @State(Scope.Thread)
    public static class DataState {
        public String[] data;
        public Map<String, Target> targets;
        public MapWrapper mapWrapper;

        public CallViaMap callViaMap;
        public CallViaMapWrapper callViaMapWrapper;
        public DirectCallNotFinal directCallNotFinal;
        public DirectCallFinal directCallFinal;
        private DirectCallFinalNoInline directCallFinalNoInline;
        private Target target;

        @Setup
        public void setUp() {
            data = new String[]{
                    RandomStringUtils.randomAlphabetic(3),
                    RandomStringUtils.randomAlphabetic(3),
                    RandomStringUtils.randomAlphabetic(3),
                    RandomStringUtils.randomAlphabetic(3),
                    RandomStringUtils.randomAlphabetic(3)
            };

            targets = new HashMap<>();

            target = new Target();
            targets.put("KEY", target);

            mapWrapper = new MapWrapper(targets);

            callViaMap = new CallViaMap(targets);
            callViaMapWrapper = new CallViaMapWrapper(mapWrapper);
            directCallNotFinal = new DirectCallNotFinal(mapWrapper);
            directCallFinal = new DirectCallFinal(mapWrapper);
            directCallFinalNoInline = new DirectCallFinalNoInline(mapWrapper);
        }
    }

    public static class CallViaMap {
        public static final String KEY = "KEY";

        private final Map<String, Target> targets;

        public CallViaMap(Map<String, Target> targets) {
            this.targets = targets;
        }

        public void run(String... data) {
            for (String datum : data) {
                targets.get(KEY).call(datum);
            }
        }
    }

    public static class CallViaMapWrapper {
        public static final String KEY = "KEY";

        private final MapWrapper targets;

        public CallViaMapWrapper(MapWrapper targets) {
            this.targets = targets;
        }

        public void run(String... data) {
            for (String datum : data) {
                targets.getTarget(KEY).call(datum);
            }
        }
    }

    public static class MapWrapper {
        private final Map<String, Target> targets;

        public MapWrapper(Map<String, Target> targets) {
            this.targets = targets;
        }

        public Target getTarget(String key) {
            return targets.get(key);
        }
    }


    public static class DirectCallNotFinal {
        public static final String KEY = "KEY";

        private Target target;

        public DirectCallNotFinal(MapWrapper targets) {
            this.target = targets.getTarget(KEY);
        }

        public void run(String... data) {
            for (String datum : data) {
                target.call(datum);
            }
        }
    }

    public static class DirectCallFinal {
        public static final String KEY = "KEY";

        private final Target target;

        public DirectCallFinal(MapWrapper targets) {
            this.target = targets.getTarget(KEY);
        }

        public void run(String... data) {
            for (String datum : data) {
                target.call(datum);
            }
        }
    }



    public static class DirectCallFinalNoInline {
        public static final String KEY = "KEY";

        private final Target target;

        public DirectCallFinalNoInline(MapWrapper targets) {
            this.target = targets.getTarget(KEY);
        }

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public void run(String... data) {
            for (String datum : data) {
                target.call(datum);
            }
        }
    }

    public static class Target {
        public static final int INITIAL_CAPACITY = 10_000;
        private final List<Object> calls;

        public Target() {
            calls = new ArrayList<>(INITIAL_CAPACITY);
        }

        public void call(final Object param) {
            if(calls.size()>INITIAL_CAPACITY){
                calls.clear();
            }
            calls.add(param);
        }

        public List<Object> getCalls() {
            return calls;
        }
    }



    /*

     * ============================== HOW TO RUN THIS TEST: ====================================

     *

     * Note the baseline is random within [0..1000] msec; and both forked runs

     * are estimating the average 500 msec with some confidence.

     *

     * You can run this test:

     *

     * a) Via the command line:

     *    $ mvn clean install

     *    $ java -jar target/benchmarks.jar MapVsMethodBenchmark -wi 3 -i 5

     *    (we requested no warmup, 3 measurement iterations; there are also other options, see -h)

     *

     * b) Via the Java API:

     *    (see the JMH homepage for possible caveats when running from IDE:

     *      http://openjdk.java.net/projects/code-tools/jmh/)

     */

    public static void main(String[] args) throws RunnerException {
        final Options opt = new OptionsBuilder()
                .include(MapVsMethodBenchmark.class.getSimpleName())
                .forks(3)
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(5))
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .build();

        new Runner(opt).run();
    }

}
