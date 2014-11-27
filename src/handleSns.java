
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.PropertiesCredentials;
import com.google.gson.Gson;

/**
 * Servlet implementation class handleSns
 */
public class handleSns extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public handleSns() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String messagetype = request.getHeader("x-amz-sns-message-type");
		// If message doesn't have the message type header, don't process it.
		if (messagetype == null)
			return;
		Scanner scan = new Scanner(request.getInputStream());
		StringBuilder builder = new StringBuilder();
		while (scan.hasNextLine()) {
			builder.append(scan.nextLine());
		}
		Gson gson = new Gson();
		SnsMessage message = gson
				.fromJson(builder.toString(), SnsMessage.class);
		if (message.SignatureVersion.equals("1")) {
			// Check the signature and throw an exception if the signature
			// verification fails.
			if (isMessageSignatureValid(message))
				System.out.println(">>Signature verification succeeded");
			else {
				System.out.println(">>Signature verification failed");
				throw new SecurityException("Signature verification failed.");
			}
		} else {
			System.out
			.println(">>Unexpected signature version. Unable to verify signature.");
			throw new SecurityException(
					"Unexpected signature version. Unable to verify signature.");
		}
		// Process the message based on type.
		if (messagetype.equals("Notification")) {
			//TODO: Do something with the Message and Subject.
			//Just log the subject (if it exists) and the message.
			String logMsgAndSubject = ">>Notification received from topic " + message.TopicArn;
			if (message.Subject != null)
				logMsgAndSubject += " Subject: " + message.Subject;
			logMsgAndSubject += " Message: " + message.Message;
			System.out.println(logMsgAndSubject);
			WbHandler.broadcast(message.Message);
		}
		else if (messagetype.equals("SubscriptionConfirmation"))
		{
			//TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
			//that you want to enable to add this endpoint as a subscription.

			//Confirm the subscription by going to the subscribeURL location 
			//and capture the return value (XML message body as a string)
			Scanner sc = new Scanner(new URL(message.SubscribeURL).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			System.out.println(">>Subscription confirmation (" + message.SubscribeURL +") Return value: " + sb.toString());
			//TODO: Process the return value to ensure the endpoint is subscribed.
		}
		else if (messagetype.equals("UnsubscribeConfirmation")) {
			//TODO: Handle UnsubscribeConfirmation message. 
			//For example, take action if unsubscribing should not have occurred.
			//You can read the SubscribeURL from this message and 
			//re-subscribe the endpoint.
			System.out.println(">>Unsubscribe confirmation: " + message.Message);
			Scanner sc = new Scanner(new URL(message.SubscribeURL).openStream());
			StringBuilder sb = new StringBuilder();
			while (sc.hasNextLine()) {
				sb.append(sc.nextLine());
			}
			System.out.println(">>Subscription confirmation (" + message.SubscribeURL +") Return value: " + sb.toString());
		}
		else {
			//TODO: Handle unknown message type.
			System.out.println(">>Unknown message type.");
		}
		System.out.println(">>Done processing message: " + message.MessageId);
	}


	private boolean isMessageSignatureValid(SnsMessage msg) {
		try {
			URL url = new URL(msg.SigningCertURL);
			InputStream inStream = url.openStream();
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf
					.generateCertificate(inStream);
			inStream.close();
	
			Signature sig = Signature.getInstance("SHA1withRSA");
			sig.initVerify(cert.getPublicKey());
			sig.update(getMessageBytesToSign(msg));
			return sig.verify(Base64.decodeBase64(msg.Signature));
		} catch (Exception e) {
			throw new SecurityException("Verify method failed.", e);
		}
	}
	
	private static byte[] getMessageBytesToSign(SnsMessage msg) {
		byte[] bytesToSign = null;
		if (msg.Type.equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.Type.equals("SubscriptionConfirmation")
				|| msg.Type.equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}
	
	// Build the string to sign for Notification messages.
	public static String buildNotificationStringToSign(SnsMessage msg) {
		String stringToSign = null;
	
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.Message + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.MessageId + "\n";
		if (msg.Subject != null) {
			stringToSign += "Subject\n";
			stringToSign += msg.Subject + "\n";
		}
		stringToSign += "Timestamp\n";
		stringToSign += msg.Timestamp + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.TopicArn + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.Type + "\n";
		return stringToSign;
	}
	
	// Build the string to sign for SubscriptionConfirmation
	// and UnsubscribeConfirmation messages.
	public static String buildSubscriptionStringToSign(SnsMessage msg) {
		String stringToSign = null;
		// Build the string to sign from the values in the message.
		// Name and values separated by newline characters
		// The name value pairs are sorted by name
		// in byte sort order.
		stringToSign = "Message\n";
		stringToSign += msg.Message + "\n";
		stringToSign += "MessageId\n";
		stringToSign += msg.MessageId + "\n";
		stringToSign += "SubscribeURL\n";
		stringToSign += msg.SubscribeURL + "\n";
		stringToSign += "Timestamp\n";
		stringToSign += msg.Timestamp + "\n";
		stringToSign += "Token\n";
		stringToSign += msg.Token + "\n";
		stringToSign += "TopicArn\n";
		stringToSign += msg.TopicArn + "\n";
		stringToSign += "Type\n";
		stringToSign += msg.Type + "\n";
		return stringToSign;
	}

}
