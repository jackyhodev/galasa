/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.spi.language.gherkin.parser;

public interface LexicalScanner {
    ParseToken getNextToken();
    void pushBackToken(ParseToken token);
}


