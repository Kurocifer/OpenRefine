/*******************************************************************************
 * Copyright (C) 2018, OpenRefine contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/

package com.google.refine.expr.functions.strings;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.regex.Pattern;

import org.testng.annotations.Test;

import com.google.refine.expr.EvalError;
import com.google.refine.grel.GrelTestBase;

public class SplitTests extends GrelTestBase {

    @Test
    public void testSplit() {
        assertEquals(invoke("split", "a,,b,c,d", ","), new String[] { "a", "b", "c", "d" });
        assertEquals(invoke("split", "a,,b,c,d", ",", true), new String[] { "a", "", "b", "c", "d" });
        assertEquals(invoke("split", "", ","), new String[] {});
        assertEquals(invoke("split", ",,,", ","), new String[] {});
        assertEquals(invoke("split", " a b c ", " "), new String[] { "a", "b", "c" });
        assertEquals(invoke("split", " a b  c ", " "), new String[] { "a", "b", "c" });
        assertEquals(invoke("split", " a b  c ", " ", true), new String[] { "", "a", "b", "", "c", "" });
        // Third argument must be boolean, not a string which looks like a boolean (or anything else)
        assertTrue(invoke("split", " a b  c ", " ", "true") instanceof EvalError);

        assertEquals(invoke("split", " a b  c ", Pattern.compile("[\\W]+")), new String[] { "a", "b", "c" });
        // Pattern.split() has the unusual behavior of returning an empty token when there's a leading pattern match
        assertEquals(invoke("split", " a b  c ", Pattern.compile("[\\W]+"), true), new String[] { "", "a", "b", "c" });

    }
}
