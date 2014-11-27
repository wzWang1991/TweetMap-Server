import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;


public class Sns {
	private AmazonSNSClient sns;
	private final String topicArn = "arn:aws:sns:us-east-1:846524277299:TweetMap-sentiment";
	
	public Sns(PropertiesCredentials p){
		AWSCredentials credentials = p;
		sns = new AmazonSNSClient(credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        sns.setRegion(usEast1);
	}
	
	public void publish(String msg) {
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		PublishResult publishResult = sns.publish(publishRequest);
		//print MessageId of message published to SNS topic
		System.out.println("MessageId - " + publishResult.getMessageId());
	}
}
