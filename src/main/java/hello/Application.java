package hello;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;

@SpringBootApplication
@RestController
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }


    @RequestMapping("/")
    public String home() {
        final String datastoreTestResult = datastoreTest();
        final String cloudStorageTestResult = cloudStorageTest();
        final String cloudSqlTestResult = cloudSqlTest();

        return getResultString(datastoreTestResult, cloudStorageTestResult, cloudSqlTestResult);
    }

    private String getResultString(final String datastoreTestResult, final String cloudStorageTestResult, final String cloudSqlTestResult) {
        final int versionNumber = 13;

        final String lineBreak = "<br/>";
        final String separator = lineBreak + "--------------------------------------------" + lineBreak;

        return "Hello Docker World, this is Spring Boot app"
                    + lineBreak
                    + "Ver. " + versionNumber
                    + " (for GCP and kubernetes, with bash installed, to update on GCP)"

                    + separator

                    + "added simple Datastore code, passing credentials via <code>${GOOGLE_APPLICATION_CREDENTIALS}</code> env variable."
                    + lineBreak
                    + "<strong>Result:</strong> " + datastoreTestResult

                    + separator

                    + "added simple Cloud Storage code, passing credentials via <code>${GOOGLE_APPLICATION_CREDENTIALS}</code> env variable."
                    + lineBreak
                    + "<strong>Result:</strong> " + cloudStorageTestResult

                    + separator

                    + "added simple Cloud SQL code working via <code>com.google.cloud.sql.postgres.SocketFactory</code>, <b>without</b> Cloud SQL Proxy."
                    + lineBreak
                    + "<strong>Result:</strong> " + cloudSqlTestResult

                    + separator
        ;
    }

    // https://cloud.google.com/datastore/docs/reference/libraries
    public String datastoreTest() {
        System.out.println("================== datastoreTest start =====================");

        final String projectIdFromDatastoreOptions1 = DatastoreOptions.getDefaultInstance().getProjectId();
        System.out.printf("ProjectId from DatastoreOptions (NO manual setting): %s \n", projectIdFromDatastoreOptions1);

        // Instantiates a client
        final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        final String descriptionField = "description";

        // The kind for the new entity
        final String kind = "Task";
        // The name/ID for the new entity
        final String name = "sampletask1";
        // The Cloud Datastore key for the new entity
        final Key taskKey = datastore.newKeyFactory().setKind(kind).newKey(name);

        // Prepares the new entity
        final Entity task = Entity.newBuilder(taskKey)
                .set(descriptionField, "Buy milk")
                .build();

        // Saves the entity
        datastore.put(task);

        System.out.printf("Saved %s: %s%n", task.getKey().getName(), task.getString(descriptionField));

        // Retrieve entity
        final Entity retrieved = datastore.get(taskKey);

        System.out.printf("Retrieved %s: %s%n", taskKey.getName(), retrieved.getString(descriptionField));

        System.out.println("Datastore test executed successfully.");
        System.out.println("================== datastoreTest end =====================");

        return String.format("Save entity of kind \"%s\", key/name: \"%s\", description: \"%s\".", taskKey.getKind(), taskKey.getName(), retrieved.getString(descriptionField));
    }

    // https://cloud.google.com/storage/docs/reference/libraries
    public String cloudStorageTest() {
        System.out.println("================== cloudStorageTest start =====================");

        // Instantiates a client
        final Storage storage = StorageOptions.getDefaultInstance().getService();

        // The name for the new bucket
        // bucket name must be lowercase and globally unique, @see  https://cloud.google.com/storage/docs/naming#requirements
//        final String bucketName = "myTestBucket"; // this will fail with "Invalid bucket name: 'myTestBucket"
//        final String bucketName = "my-test-bucket"; // this will fail with "Sorry, that name is not available. Please try a different one."
        final String projectId = StorageOptions.getDefaultInstance().getProjectId();
        final String bucketName = projectId + "-my-test-bucket";

        // duplicate bucket creation will fail with "You already own this bucket. Please select another name."

        final String result;

        // Creates the new bucket or deletes the existing bucket
        final Bucket existingBucket = storage.get(bucketName);
        if (existingBucket != null) { // bucket exists -> delete it
            System.out.printf("Bucket %s exists. Deleting it...\n", bucketName);
            storage.delete(bucketName);
            System.out.printf("Bucket %s deleted.%n", bucketName);

            result = String.format("Bucket \"%s\" has been deleted.", bucketName);
        }
        else { // bucket does not exist -> create it
            System.out.printf("Bucket %s does not exist. Creating it...\n", bucketName);
            final Bucket bucket = storage.create(BucketInfo.of(bucketName));
            System.out.printf("Bucket %s created.%n", bucket.getName());

            result = String.format("Bucket \"%s\" has been created.", bucketName);
        }

        System.out.println("================== cloudStorageTest end =====================");

        return result;
    }


    public static String cloudSqlTest() {
        System.out.println("================== cloudSqlTest start =====================");


        // The instance connection name can be obtained from the instance overview page in Cloud Console
        // or by running "gcloud sql instances describe <instance> | grep connectionName".
        final String instanceConnectionName = "dpopov-gcp-spring-boot:us-central1:dpopov-postgres";

        // The database from which to list tables.
        final String databaseName = "postgres";

        final String username = "postgres";

        // This is the password that was set via the Cloud Console or empty if never set
        // (not recommended).
        final String password = "123QWEasd";

        if (instanceConnectionName.equals("<insert_connection_name>")) {
            System.err.println("Please update the sample to specify the instance connection name.");
            System.exit(1);
        }

        if (password.equals("<insert_password>")) {
            System.err.println("Please update the sample to specify the postgres password.");
            System.exit(1);
        }

        final String jdbcUrl = String.format(
              "jdbc:postgresql://google/%s?socketFactory=com.google.cloud.sql.postgres.SocketFactory&socketFactoryArg=%s"
            , databaseName
            , instanceConnectionName
        );

        System.out.printf("jdbcUrl: %s \n", jdbcUrl);

        String result;

        try {
            final Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            try (final Statement statement = connection.createStatement()) {
                final ResultSet resultSet = statement.executeQuery("select schemaname, tablename from pg_catalog.pg_tables");

                final StringBuilder sb = new StringBuilder();
                sb.append("Table names: <br/>");

                while (resultSet.next()) {
                    final String tableName = resultSet.getString(1) + "." + resultSet.getString(2);
                    sb.append(tableName).append("<br/>");

                    System.out.println(tableName);
                }

                result = sb.toString();
            }
        }
        catch (final SQLException e) {
            e.printStackTrace();
            result = "SQLException: " + e.getMessage();
        }

        System.out.println("================== cloudSqlTest end =====================");

        return result;
    }
}