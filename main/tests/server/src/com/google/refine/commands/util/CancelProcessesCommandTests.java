/*

Copyright 2010, Google Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

    * Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
copyright notice, this list of conditions and the following disclaimer
in the documentation and/or other materials provided with the
distribution.
    * Neither the name of Google Inc. nor the names of its
contributors may be used to endorse or promote products derived from
this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,           
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY           
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package com.google.refine.commands.util;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertThrows;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.google.refine.ProjectManager;
import com.google.refine.RefineTest;
import com.google.refine.commands.Command;
import com.google.refine.commands.history.CancelProcessesCommand;
import com.google.refine.model.Project;
import com.google.refine.process.ProcessManager;
import com.google.refine.util.ParsingUtilities;
import com.google.refine.util.TestUtils;

public class CancelProcessesCommandTests extends RefineTest {

    @Override
    @BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }

    // System Under Test
    CancelProcessesCommand SUT = null;

    // variables
    long PROJECT_ID_LONG = 1234;
    String PROJECT_ID = "1234";

    // mocks
    HttpServletRequest request = null;
    HttpServletResponse response = null;
    ProjectManager projMan = null;
    Project proj = null;
    ProcessManager processMan = null;
    StringWriter sw = null;
    PrintWriter pw = null;

    @BeforeMethod
    public void SetUp() {
        projMan = mock(ProjectManager.class);
        ProjectManager.singleton = projMan;
        proj = mock(Project.class);
        processMan = mock(ProcessManager.class);
        sw = new StringWriter();
        pw = new PrintWriter(sw);

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        SUT = new CancelProcessesCommand();
    }

    @AfterMethod
    public void TearDown() {
        SUT = null;

        projMan = null;
        ProjectManager.singleton = null;
        proj = null;
        sw = null;
        request = null;
        response = null;
    }

    @Test
    public void doPostFailsThrowsWithNullParameters() {
        // both parameters null
        assertThrows(IllegalArgumentException.class, () -> SUT.doPost(null, null));
        // request is null
        assertThrows(IllegalArgumentException.class, () -> SUT.doPost(null, response));
        // response parameter null
        assertThrows(IllegalArgumentException.class, () -> SUT.doPost(request, null));
    }

    /**
     * Contract for a complete working post
     */
    @Test
    public void doPostRegressionTest() throws Exception {

        // mock dependencies
        when(request.getParameter("project")).thenReturn(PROJECT_ID);
        when(request.getParameter("csrf_token")).thenReturn(Command.csrfFactory.getFreshToken());
        when(projMan.getProject(anyLong())).thenReturn(proj);
        when(proj.getProcessManager()).thenReturn(processMan);
        when(response.getWriter()).thenReturn(pw);

        // run
        SUT.doPost(request, response);

        // verify
        verify(request, times(1)).getParameter("project");
        verify(projMan, times(1)).getProject(PROJECT_ID_LONG);

        verify(processMan, times(1)).cancelAll();
        verify(response, times(1)).setCharacterEncoding("UTF-8");
        verify(response, times(1))
                .setHeader("Content-Type", "application/json");
        verify(proj, times(1)).getProcessManager();
        verify(response, times(1)).getWriter();
        TestUtils.assertEqualsAsJson(sw.toString(), "{ \"code\" : \"ok\" }");
    }

    @Test
    public void doPostThrowsIfCommand_getProjectReturnsNull() throws Exception {
        // mock dependencies
        when(request.getParameter("project")).thenReturn(PROJECT_ID);
        when(request.getParameter("csrf_token")).thenReturn(Command.csrfFactory.getFreshToken());
        when(projMan.getProject(anyLong()))
                .thenReturn(null);
        when(response.getWriter()).thenReturn(pw);

        // run
        SUT.doPost(request, response);

        // verify
        verify(request, times(1)).getParameter("project");
        verify(projMan, times(1)).getProject(PROJECT_ID_LONG);
        ObjectNode jsonResponse = (ObjectNode) ParsingUtilities.mapper.readTree(sw.toString());
        assertEquals(jsonResponse.get("code").asText(), "error");
    }

    @Test
    public void doPostCatchesExceptionFromWriter() throws Exception {
        String ERROR_MESSAGE = "hello world";

        // mock dependencies
        when(request.getParameter("project")).thenReturn(PROJECT_ID);
        when(request.getParameter("csrf_token")).thenReturn(Command.csrfFactory.getFreshToken());
        when(projMan.getProject(anyLong())).thenReturn(proj);
        when(proj.getProcessManager()).thenReturn(processMan);
        when(response.getWriter()).thenThrow(new IllegalStateException(ERROR_MESSAGE))
                .thenReturn(pw);

        // run
        SUT.doPost(request, response);

        verify(request, times(1)).getParameter("project");
        verify(projMan, times(1)).getProject(PROJECT_ID_LONG);

        verify(processMan, times(1)).cancelAll();
        verify(response, times(2)).setCharacterEncoding("UTF-8");
        // omitted other verifications for brevity.
        // assumption is that expecting response.setCharacterEncoding times(3)
        // implies it has Command.respondException has been called as expected
    }
}
