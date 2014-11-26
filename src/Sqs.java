/*
 * Copyright 2010-2014 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * This sample demonstrates how to make basic requests to Amazon SQS using the
 * AWS SDK for Java.
 * <p>
 * <b>Prerequisites:</b> You must have a valid Amazon Web
 * Services developer account, and be signed up to use Amazon SQS. For more
 * information on Amazon SQS, see http://aws.amazon.com/sqs.
 * <p>
 * WANRNING:</b> To avoid accidental leakage of your credentials, DO NOT keep
 * the credentials file in your source directory.
 */
public class Sqs {
	private AmazonSQS sqs;
	
	public Sqs(){
		AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (/Users/daniel/.aws/credentials), and is in valid format.",
                    e);
        }

        sqs = new AmazonSQSClient(credentials);
        Region usEast1 = Region.getRegion(Regions.US_EAST_1);
        sqs.setRegion(usEast1);
        System.out.println("Finish initializing sqs service");
	}
	
	public String createQueue(String name){
		String myQueueUrl = null;
		try{
			System.out.println("Creating a new SQS queue called "+ name);
			CreateQueueRequest createQueueRequest = new CreateQueueRequest(name);
	        myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	        
		} catch (AmazonServiceException ase) {
	        System.out.println("Caught an AmazonServiceException, which means your request made it " +
	                "to Amazon SQS, but was rejected with an error response for some reason.");
	        System.out.println("Error Message:    " + ase.getMessage());
	        System.out.println("HTTP Status Code: " + ase.getStatusCode());
	        System.out.println("AWS Error Code:   " + ase.getErrorCode());
	        System.out.println("Error Type:       " + ase.getErrorType());
	        System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
	        System.out.println("Caught an AmazonClientException, which means the client encountered " +
	                "a serious internal problem while trying to communicate with SQS, such as not " +
	                "being able to access the network.");
	        System.out.println("Error Message: " + ace.getMessage());
		}
		return myQueueUrl;
	}
	
	public void sendMessage(String myQueueUrl, String message){
		try{
			System.out.println("Sending a message");
	        sqs.sendMessage(new SendMessageRequest(myQueueUrl, message));
		} catch (AmazonServiceException ase) {
	        System.out.println("Caught an AmazonServiceException, which means your request made it " +
	                "to Amazon SQS, but was rejected with an error response for some reason.");
	        System.out.println("Error Message:    " + ase.getMessage());
	        System.out.println("HTTP Status Code: " + ase.getStatusCode());
	        System.out.println("AWS Error Code:   " + ase.getErrorCode());
	        System.out.println("Error Type:       " + ase.getErrorType());
	        System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
	        System.out.println("Caught an AmazonClientException, which means the client encountered " +
	                "a serious internal problem while trying to communicate with SQS, such as not " +
	                "being able to access the network.");
	        System.out.println("Error Message: " + ace.getMessage());
		}

	}
	
	public void receiveMessage(String myQueueUrl){
		try{
			 System.out.println("Receiving messages");
	         ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
	         List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
	         for (Message message : messages) {
	             System.out.println("  Message");
	             System.out.println("    MessageId:     " + message.getMessageId());
	             System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
	             System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
	             System.out.println("    Body:          " + message.getBody());
	             for (Entry<String, String> entry : message.getAttributes().entrySet()) {
	                 System.out.println("  Attribute");
	                 System.out.println("    Name:  " + entry.getKey());
	                 System.out.println("    Value: " + entry.getValue());
	             }
	         }
		} catch (AmazonServiceException ase) {
	        System.out.println("Caught an AmazonServiceException, which means your request made it " +
	                "to Amazon SQS, but was rejected with an error response for some reason.");
	        System.out.println("Error Message:    " + ase.getMessage());
	        System.out.println("HTTP Status Code: " + ase.getStatusCode());
	        System.out.println("AWS Error Code:   " + ase.getErrorCode());
	        System.out.println("Error Type:       " + ase.getErrorType());
	        System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
	        System.out.println("Caught an AmazonClientException, which means the client encountered " +
	                "a serious internal problem while trying to communicate with SQS, such as not " +
	                "being able to access the network.");
	        System.out.println("Error Message: " + ace.getMessage());
		}
		
	}
	
	public void deleteQueue(String myQueueUrl){
		try{
			System.out.println("Deleting the queue");
			sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
		} catch (AmazonServiceException ase) {
	        System.out.println("Caught an AmazonServiceException, which means your request made it " +
	                "to Amazon SQS, but was rejected with an error response for some reason.");
	        System.out.println("Error Message:    " + ase.getMessage());
	        System.out.println("HTTP Status Code: " + ase.getStatusCode());
	        System.out.println("AWS Error Code:   " + ase.getErrorCode());
	        System.out.println("Error Type:       " + ase.getErrorType());
	        System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
	        System.out.println("Caught an AmazonClientException, which means the client encountered " +
	                "a serious internal problem while trying to communicate with SQS, such as not " +
	                "being able to access the network.");
	        System.out.println("Error Message: " + ace.getMessage());
		}
	}
	
}
