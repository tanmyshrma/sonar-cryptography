/*
 * Sonar Cryptography Plugin
 * Copyright (C) 2024 PQCA
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ibm.plugin;

import com.ibm.engine.language.ILanguageSupport;
import com.ibm.engine.language.LanguageSupporter;
import com.ibm.mapper.model.INode;
import com.ibm.output.IAggregator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import org.sonar.plugins.java.api.JavaCheck;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.semantic.Symbol;
import org.sonar.plugins.java.api.tree.Tree;

/**
 * {@code javaLanguageSupport} is thread-local rather than a single shared instance: callers
 * (cbomkit-lib's module-level parallel scan) may run multiple module analyses concurrently on
 * different threads, and {@code ILanguageSupport}'s {@code Handler}/{@code CallStackAgent} is
 * stateful (cross-file call-stack tracking within one compilation run) and was never designed to be
 * mutated from more than one thread at a time. Giving each thread its own instance avoids a data
 * race without changing single-threaded callers' behavior (each still sees one continuous instance
 * for the calls it makes). {@code detectedNodes} is a synchronized list for the same reason: {@code
 * addNodes} is invoked from whichever thread is running the visitor for the file currently being
 * analyzed.
 */
public final class JavaAggregator implements IAggregator {

    private static final ThreadLocal<
                    ILanguageSupport<JavaCheck, Tree, Symbol, JavaFileScannerContext>>
            javaLanguageSupport = ThreadLocal.withInitial(LanguageSupporter::javaLanguageSupporter);
    private static final List<INode> detectedNodes =
            Collections.synchronizedList(new ArrayList<>());

    private JavaAggregator() {
        // nothing
    }

    public static void addNodes(@Nonnull List<INode> newNodes) {
        detectedNodes.addAll(newNodes);
        IAggregator.log(newNodes);
    }

    @Nonnull
    public static List<INode> getDetectedNodes() {
        return List.copyOf(detectedNodes);
    }

    @Nonnull
    public static ILanguageSupport<JavaCheck, Tree, Symbol, JavaFileScannerContext>
            getLanguageSupport() {
        return javaLanguageSupport.get();
    }

    public static void reset() {
        javaLanguageSupport.remove();
        detectedNodes.clear();
    }
}
