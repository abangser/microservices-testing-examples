package welcomememberemailservice.it;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.github.charithe.kafka.EphemeralKafkaBroker;
import com.github.charithe.kafka.KafkaJunitRule;
import io.dropwizard.testing.junit.DropwizardAppRule;
import java.lang.invoke.MethodHandles;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.RuleChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.subethamail.wiser.WiserMessage;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceApplication;
import welcomememberemailservice.bootstrap.WelcomeMemberEmailServiceConfiguration;
import welcomememberemailservice.it.kafka.KafkaConsumerOffsets;
import welcomememberemailservice.it.smtp.SmtpServerRule;

public abstract class IntegrationTestBase {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String INTEGRATION_YML = resourceFilePath("integration.yml");
  private static final String KAFKA_HOST = "localhost";
  private static final int KAFKA_PORT = 9092;
  private static final String SPECIAL_MEMBERSHIP_TOPIC = "special-membership-topic";
  private static final String WELCOME_EMAIL_GROUP_ID = "welcome-member-email-consumer";
  private static final int SMTP_SERVER_PORT = 2525;

  private static final EphemeralKafkaBroker KAFKA_BROKER = EphemeralKafkaBroker.create(KAFKA_PORT);
  private static final KafkaJunitRule KAFKA_RULE = new KafkaJunitRule(KAFKA_BROKER)
      .waitForStartup();
  private static final SmtpServerRule SMTP_SERVER_RULE = new SmtpServerRule(SMTP_SERVER_PORT);
  private static final DropwizardAppRule<WelcomeMemberEmailServiceConfiguration> SERVICE_RULE =
      new DropwizardAppRule<>(WelcomeMemberEmailServiceApplication.class, INTEGRATION_YML);

  @ClassRule
  public static final RuleChain RULES = RuleChain
      .outerRule(KAFKA_RULE)
      .around(SMTP_SERVER_RULE)
      .around(SERVICE_RULE);

  private List<WiserMessage> emailsBeforeTest;

  @Before
  public void setUp() throws Exception {
    emailsBeforeTest = getEmails();
  }

  private List<WiserMessage> getEmails() {
    return new ArrayList<>(SMTP_SERVER_RULE.getSmtpServer().getMessages());
  }


  protected void assertNoEmailWasSent() {
    assertThat(getEmails(), hasSize(emailsBeforeTest.size()));
  }

  protected void assertAnEmailWasSent() {
    assertThat(getEmails(), hasSize(emailsBeforeTest.size() + 1));
  }

  protected WiserMessage getLastSentEmail() {
    return getEmails().get(getEmails().size() - 1);
  }

  protected void publishMembershipMessageAndWaitToBeConsumed(String message) {
    publishMessageAndWaitToBeConsumed(SPECIAL_MEMBERSHIP_TOPIC, message, WELCOME_EMAIL_GROUP_ID);
  }

  protected void publishMessageAndWaitToBeConsumed(String topic, String message, String groupId) {
    KafkaConsumerOffsets kafkaConsumerOffsets =
        new KafkaConsumerOffsets(KAFKA_HOST, KAFKA_RULE.helper().kafkaPort(), groupId);

    final long previousOffset = Math.max(kafkaConsumerOffsets.readOffset(topic), 0);

    LOG.info("Publishing message {} to topic {}", message, topic);
    KAFKA_RULE.helper().produceStrings(topic, message);

    LOG.info("Waiting for message to be consumed from topic {}", topic);
    await().atMost(Duration.ofSeconds(5))
        .until(() -> kafkaConsumerOffsets.readOffset(topic), equalTo(previousOffset + 1));
  }

  protected String generateRandomMessageSubject() {
    // Generates randomness in the test
    // Allows for failures so that we can validate observability.
    // Should result in about 75% pass rate
    String[] givenList = {
      "Welcome!",
      "Welcome!",
      "Welcome!",
      "Wrong",
    };

    return givenList[new Random().nextInt(givenList.length)];
  }
}
