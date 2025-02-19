package creditscoreservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import creditscoreservice.bootstrap.CreditScoreServiceApplication;
import creditscoreservice.it.client.ResourcesClient;
import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.Response;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class IntegrationTestBase {

  private static final String INTEGRATION_YML = resourceFilePath("integration.yml");

  @ClassRule
  public static final DropwizardAppRule<Configuration> SERVICE_RULE =
      new DropwizardAppRule<>(CreditScoreServiceApplication.class, INTEGRATION_YML);

  protected static ResourcesClient resourcesClient;

  @BeforeClass
  public static void setUpClass() throws Exception {
    resourcesClient = new ResourcesClient(SERVICE_RULE.getEnvironment(),
        SERVICE_RULE.getLocalPort());
  }

  protected void setupCreditScoreState(String email, Integer creditScore) {
    Response response = resourcesClient.putCreditScore(email, creditScoreDto(creditScore));
    response.close();
    assertThat(response.getStatus(), equalTo(200));
  }

  protected Map<String, Object> creditScoreDto(Integer creditScore) {
    return singletonMap("creditScore", creditScore);
  }
}
