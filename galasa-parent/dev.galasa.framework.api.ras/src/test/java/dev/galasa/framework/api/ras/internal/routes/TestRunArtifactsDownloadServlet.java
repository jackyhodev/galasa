/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.framework.api.ras.internal.routes;

import org.junit.Test;

import dev.galasa.framework.api.ras.internal.RasServlet;
import dev.galasa.framework.api.ras.internal.RasServletTest;
import dev.galasa.framework.api.ras.internal.mocks.MockRasServletEnvironment;
import dev.galasa.framework.api.common.mocks.MockHttpServletRequest;
import dev.galasa.framework.mocks.MockFileSystem;
import dev.galasa.framework.mocks.MockPath;
import dev.galasa.framework.spi.IRunResult;

import org.junit.Before;
import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TestRunArtifactsDownloadServlet extends RasServletTest {

	@Before
	public void setUp() {
		mockFileSystem = new MockFileSystem();
	}

	/*
     * Regex Path
     */

	@Test
	public void TestPathRegexExpectedLocalPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/lcl-abcd-1234.run/files/run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/lcl-abcd-1234.run/files/artifacts/image123.png";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedCouchDBPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-efgh-5678.run/files/run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexLowerCasePathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdbstoredrun/files/run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void TestPathRegexExpectedPathWithCapitalLeadingLetterReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/ABC-DEFG-5678.run/files/run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}
	
	@Test
	public void TestPathRegexUpperCasePathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/FILES/run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}
 
	@Test
	public void TestPathRegexExpectedPathWithLeadingNumberReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/files/1run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexExpectedPathWithTrailingForwardSlashReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/files/run.log/";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexNumberPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/files/run1.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	}

	@Test
	public void TestPathRegexUnexpectedPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/file/run.log";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexEmptyPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexSpecialCharacterPathReturnsFalse(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/files/run.log?";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isFalse();
	}

	@Test
	public void TestPathRegexMultipleForwardSlashPathReturnsTrue(){
		//Given...
		String expectedPath = RunArtifactsDownloadRoute.path;
		String inputPath = "/runs/cdb-EFGH-5678.run/files/run.log//////";

		//When...
		boolean matches = Pattern.compile(expectedPath).matcher(inputPath).matches();

		//Then...
		assertThat(matches).isTrue();
	} 

	/*
	 * GET Requests
	 */

    @Test
	public void testBadArtifactPathInRequestGivesNotFoundError() throws Exception {
		//Given..
		String runId = "xx12345xx";
        String runName = "U123";
        String artifactPath = "bad/artifact/path";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5008,
		//   "error_message" : "GAL5008E: Error locating artifact '/bad/artifact/path' for run with identifier 'U123'."
		// }
		assertThat(resp.getStatus()).isEqualTo(500);
		checkErrorStructure(outStream.toString(), 5008, "GAL5008E", artifactPath, runName);

		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testGoodRunIdAndBadArtifactPathGivesNotFoundError() throws Exception {
		//Given..
		String runId = "xx12345xx";
        String runName = "U123";
        String artifactPath = "/bad/artifact/path";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/artifacts" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5009,
		//   "error_message" : "GAL5009E: Error retrieving artifact '/bad/artifact/path' for run with identifier 'U123'."
		// }
		assertThat(resp.getStatus()).isEqualTo(500);
		checkErrorStructure(outStream.toString(), 5009, "GAL5009E", artifactPath, runName);

		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
	public void testGoodRunIdAndIncompleteArtifactPathGivesNotFoundError() throws Exception {
		//Given..
		String runId = "xx12345xx";
        String runName = "U123";
        String artifactPath = "/path";
		List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/artifacts" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5009,
		//   "error_message" : "GAL5009E: Error retrieving artifact '/bad/artifact/path' for run with identifier 'U123'."
		// }
		assertThat(resp.getStatus()).isEqualTo(500);
		checkErrorStructure(outStream.toString(), 5009, "GAL5009E", artifactPath, runName);

		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

	@Test
	public void testBadRunIdAndBadArtifactIdGivesNotFoundError() throws Exception {
		//Given..
		String runId = "badRunId";
        String artifactPath = "badArtifactId";
		List<IRunResult> mockInputRunResults = new ArrayList<>();

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/artifacts" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting this json:
		// {
		//   "error_code" : 5002,
		//   "error_message" : "GAL5002E: Error retrieving ras run from identifier 'badRunId'.""
		// }
		assertThat(resp.getStatus()).isEqualTo(404);
		checkErrorStructure(outStream.toString() , 5002 , "GAL5002E", "badRunId" );

		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
    public void testGoodRunIdAndRunLogReturnsOKAndFile() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        String artifactPath = "run.log";
        String runlog = "very detailed run log";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runlog);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(runlog);
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment");
	}

    @Test
    public void testGoodRunIdAndGoodArtifactPathButNoFileReturnsError() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        String artifactPath = "/nonexistent.file";
        String runlog = "You have a terminal image to access.";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, runlog);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/artifacts" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(500);
		checkErrorStructure(outStream.toString(), 5009, "GAL5009E", artifactPath, runName);

		assertThat( resp.getContentType()).isEqualTo("application/json");
	}

    @Test
    public void testGoodRunIdAndGoodArtifactReturnsOKAndFile() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        MockPath artifactPath = new MockPath("/term002.gz", mockFileSystem);
		String fileContent = "dummy content";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);
		mockFileSystem.createFile(artifactPath);
		mockFileSystem.setFileContents(artifactPath, fileContent);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/artifacts" + artifactPath.toString());
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(fileContent);
		assertThat(resp.getContentType()).isEqualTo("application/x-gzip");
		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment");
	}

    @Test
    public void testDownloadArtifactWithUnknownContentTypeDefaultsToBinaryFile() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        MockPath artifactPath = new MockPath("/term002.artifact", mockFileSystem);
		String fileContent = "dummy content";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);
		mockFileSystem.createFile(artifactPath);
		mockFileSystem.setFileContents(artifactPath, fileContent);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files/artifacts" + artifactPath.toString());
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo(fileContent);
		assertThat(resp.getContentType()).isEqualTo("application/octet-stream");
		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment");
	}

    @Test
    public void testGoodRunIdAndArtifactsPropertiesReturnsOKAndFile() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        String artifactPath = "/artifacts.properties";
        MockPath existingPath = new MockPath("/testA/term002.gz", mockFileSystem);
		String fileContent = "dummy content";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);
		mockFileSystem.createFile(existingPath);
		mockFileSystem.setFileContents(existingPath, fileContent);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo("/artifacts" + existingPath + "=" + "application/x-gzip");
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment");
	}

    @Test
    public void testUnknownContentTypeAndArtifactsPropertiesDefaultsToBinaryType() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        String artifactPath = "/artifacts.properties";
        MockPath existingPath = new MockPath("/testA/unknown.artifact", mockFileSystem);
		String fileContent = "dummy content";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);
		mockFileSystem.createFile(existingPath);
		mockFileSystem.setFileContents(existingPath, fileContent);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEqualTo("/artifacts" + existingPath.toString() + "=application/octet-stream");
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment");
	}

    @Test
    public void testGoodRunIdAndEmptyRunLogReturnsOKEmptyFile() throws Exception {
		//Given..
		String runId = "12345";
		String runName = "testA";
        String artifactPath = "/run.log";
        List<IRunResult> mockInputRunResults = generateTestData(runId, runName, null);

		Map<String, String[]> parameterMap = new HashMap<String,String[]>();
		MockHttpServletRequest mockRequest = new MockHttpServletRequest(parameterMap, "/runs/" + runId + "/files" + artifactPath);
		MockRasServletEnvironment mockServletEnvironment = new MockRasServletEnvironment(mockInputRunResults, mockRequest, mockFileSystem);

		RasServlet servlet = mockServletEnvironment.getServlet();
		HttpServletRequest req = mockServletEnvironment.getRequest();
		HttpServletResponse resp = mockServletEnvironment.getResponse();
		ServletOutputStream outStream = resp.getOutputStream();

		//When...
		servlet.init();
		servlet.doGet(req,resp);

		// Then...
		// Expecting:
		assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(outStream.toString()).isEmpty();
		assertThat(resp.getContentType()).isEqualTo("text/plain");
		assertThat(resp.getHeader("Content-Disposition")).isEqualTo("attachment");
	}
}