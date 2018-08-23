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

        return getResultString(datastoreTestResult, cloudStorageTestResult);
    }

    private String getResultString(final String datastoreTestResult, final String cloudStorageTestResult) {
        final int versionNumber = 12;

        final String lineBreak = "<br/>";
        final String separator = lineBreak + "--------------------------------------------" + lineBreak;

        return "Hello Docker World, this is Spring Boot app"
                    + lineBreak
                    + "Ver. " + versionNumber
                    + " (for GCP and kubernetes, with bash installed, to update on GCP)"

                    + separator

                    + "added simple Datastore code, passing credentials via ${GOOGLE_APPLICATION_CREDENTIALS} env variable."
                    + lineBreak
                    + "<strong>Result:</strong> " + datastoreTestResult

                    + separator

                    + "added simple Cloud Storage code, passing credentials via ${GOOGLE_APPLICATION_CREDENTIALS} env variable."
                    + lineBreak
                    + "<strong>Result:</strong> " + cloudStorageTestResult

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
}