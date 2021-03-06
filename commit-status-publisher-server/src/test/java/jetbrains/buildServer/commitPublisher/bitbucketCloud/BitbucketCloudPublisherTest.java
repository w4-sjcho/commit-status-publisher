package jetbrains.buildServer.commitPublisher.bitbucketCloud;

import jetbrains.buildServer.commitPublisher.Constants;
import jetbrains.buildServer.commitPublisher.HttpPublisherServerBasedTest;
import jetbrains.buildServer.commitPublisher.PublisherException;
import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.BuildRevision;
import jetbrains.buildServer.vcs.VcsRootInstance;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;

/**
 * @author anton.zamolotskikh, 05/10/16.
 */
@Test
public class BitbucketCloudPublisherTest extends HttpPublisherServerBasedTest {

  public BitbucketCloudPublisherTest() {
    myExpectedRegExps.put(Events.QUEUED, null); // not to be tested
    myExpectedRegExps.put(Events.REMOVED, null); // not to be tested
    myExpectedRegExps.put(Events.STARTED, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*INPROGRESS.*Build started.*", REVISION));
    myExpectedRegExps.put(Events.FINISHED, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*SUCCESSFUL.*Success.*", REVISION));
    myExpectedRegExps.put(Events.FAILED, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*FAILED.*Failure.*", REVISION));
    myExpectedRegExps.put(Events.COMMENTED_SUCCESS,
            String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*SUCCESSFUL.*Success with a comment by %s:.*%s.*", REVISION, USER.toLowerCase(), COMMENT));
    myExpectedRegExps.put(Events.COMMENTED_FAILED,
            String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*FAILED.*Failure with a comment by %s:.*%s.*", REVISION, USER.toLowerCase(), COMMENT));
    myExpectedRegExps.put(Events.COMMENTED_INPROGRESS,
            String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*INPROGRESS.*Running with a comment by %s:.*%s.*", REVISION, USER.toLowerCase(), COMMENT));
    myExpectedRegExps.put(Events.COMMENTED_INPROGRESS_FAILED,
            String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*FAILED.*%s.*with a comment by %s:.*%s.*", REVISION, PROBLEM_DESCR, USER.toLowerCase(), COMMENT));
    myExpectedRegExps.put(Events.INTERRUPTED, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*FAILED.*%s.*", REVISION, PROBLEM_DESCR));
    myExpectedRegExps.put(Events.FAILURE_DETECTED, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*FAILED.*%s.*", REVISION, PROBLEM_DESCR));
    myExpectedRegExps.put(Events.MARKED_SUCCESSFUL, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*SUCCESSFUL.*Build marked as successful.*", REVISION));
    myExpectedRegExps.put(Events.MARKED_RUNNING_SUCCESSFUL, String.format(".*/2.0/repositories/owner/project/commit/%s.*ENTITY:.*INPROGRESS.*Build marked as successful.*", REVISION));
  }

  public void should_fail_with_error_on_wrong_vcs_url() throws InterruptedException {
    myVcsRoot.setProperties(Collections.singletonMap("url", "wrong://url.com"));
    VcsRootInstance vcsRootInstance = myBuildType.getVcsRootInstanceForParent(myVcsRoot);
    BuildRevision revision = new BuildRevision(vcsRootInstance, REVISION, "", REVISION);
    try {
      myPublisher.buildFinished(myFixture.createBuild(myBuildType, Status.NORMAL), revision);
      fail("PublishError exception expected");
    } catch(PublisherException ex) {
      then(ex.getMessage()).matches(".*failed to parse repository URL.*" + myVcsRoot.getName() + ".*");
    }
  }


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Map<String, String> params = new HashMap<String, String>() {{
      put(Constants.BITBUCKET_CLOUD_USERNAME, "user");
      put(Constants.BITBUCKET_CLOUD_PASSWORD, "pwd");
    }};
    BitbucketCloudPublisher publisher = new BitbucketCloudPublisher(myBuildType, FEATURE_ID, myExecServices, myWebLinks, params, myProblems);
    publisher.setBaseUrl(getServerUrl() + "/");
    myPublisher = publisher;
  }
}