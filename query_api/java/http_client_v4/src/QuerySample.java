import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateUtils;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.http.message.ParserCursor;
import org.apache.http.util.CharArrayBuffer;


/**
 * Sample code in Java for using the kooaba REST API.
 * Uses httpclient 4.x.
 * Contact: support@kooaba.com
 * 
 * @version 2010-12-02
 * @authors Joachim Fornallaz, Franco Sebregondi
 */
public class QuerySample {

	public static String accessKey = "df8d23140eb443505c0661c5b58294ef472baf64";
	public static String secretKey = "054a431c8cd9c3cf819f3bc7aba592cc84c09ff7";
	public static String apiAddress = "http://search.kooaba.com/groups/{group_id}/queries.xml";
	private String sourceFile;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		QuerySample sample = new QuerySample("lena.jpg");
		sample.run();
	}

	/**
	 * Calculates the MD5 digest of the request body for POST or PUT methods
	 * @param httpMethod
	 * @return String MD5 digest as a hex string
	 * @throws IOException
	 */
	private static String contentMD5(HttpEntityEnclosingRequestBase httpMethod) throws IOException {
		ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream();
		httpMethod.getEntity().writeTo(requestOutputStream);
		return DigestUtils.md5Hex(requestOutputStream.toByteArray());
	}

	/**
	 * Calculates the KWS signature of a HTTP request (POST or PUT)
	 * @param httpMethod
	 * @return String Signature
	 * @throws IOException
	 */
	public static String kwsSignature(HttpEntityEnclosingRequestBase httpMethod) throws IOException {
		String method = httpMethod.getMethod();
		String hexDigest = contentMD5(httpMethod);
        CharArrayBuffer buf = new CharArrayBuffer(64); 
        buf.append(httpMethod.getEntity().getContentType().getValue());
        HeaderValueParser parser = new BasicHeaderValueParser();
        ParserCursor cursor = new ParserCursor(0, buf.length());
        String contentType = parser.parseNameValuePair(buf, cursor).toString();
		String dateValue = httpMethod.getFirstHeader("Date").getValue();
		String requestPath = httpMethod.getURI().getPath();
		String signatureInput = new String(method + "\n" + hexDigest + "\n" + contentType + "\n" + dateValue + "\n" + requestPath);

		String digestInput = new String(secretKey + "\n\n" + signatureInput);
		byte[] digestBytes = DigestUtils.sha(digestInput);
		byte[] encoded = Base64.encodeBase64(digestBytes);
		return new String(encoded);
	}

	public QuerySample(String imagePath) {
		sourceFile = imagePath;
	}
	 
	private void run() {
		String targetURL = apiAddress.replaceFirst("\\{group_id\\}", "32");
		System.out.println(targetURL);
		try {
			// Prepare content body
			File targetFile = new File(sourceFile);
			FileBody imagePart = new FileBody(targetFile, "image/jpeg");
			StringBody boundingBoxPart = new StringBody("true", Charset.forName("US-ASCII"));
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("query[file]", imagePart);
	        reqEntity.addPart("query[bounding_box]", boundingBoxPart);

			// Prepare the HTTP method
	        HttpPost queryPost = new HttpPost(targetURL);
	        queryPost.setEntity(reqEntity);
	        queryPost.addHeader("Date", DateUtils.formatDate(new Date()));
	        queryPost.addHeader("Authorization", "KWS " + accessKey + ":" + kwsSignature(queryPost));

	        // Execute the method
	        ResponseHandler<String> responseHandler = new BasicResponseHandler();
			HttpClient client = new DefaultHttpClient();
	        String responseBody = client.execute(queryPost, responseHandler);
	        
	        // Debugging output
			Header[] headers = queryPost.getAllHeaders();
			System.out.println("Headers:");
			for (int i = 0; i < headers.length; i++) {
				System.out.println("  " + headers[i]);
			}

	        System.out.println("----------------------------------------");
			System.out.println(responseBody);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
