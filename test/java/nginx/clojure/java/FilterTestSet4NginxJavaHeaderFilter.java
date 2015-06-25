package nginx.clojure.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.security.cert.X509Certificate;

import nginx.clojure.wave.SuspendMethodTracer;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
//import sun.security.util.ObjectIdentifier;


public class FilterTestSet4NginxJavaHeaderFilter {
	
	public static class AddMoreHeaders implements NginxJavaHeaderFilter {
		@Override
		public Object[] doFilter(int status, Map<String, Object> request, Map<String, Object> responseHeaders) {
			responseHeaders.put("Xfeep-Header", "Hello!");
			return Constants.PHASE_DONE;
		}
	}
	
	public static class RemoveAndAddMoreHeaders implements NginxJavaHeaderFilter {
		@Override
		public Object[] doFilter(int status, Map<String, Object> request, Map<String, Object> responseHeaders) {
			responseHeaders.remove("Content-Type");
			responseHeaders.put("Content-Type", "text/html");
			responseHeaders.put("Xfeep-Header", "Hello2!");
			responseHeaders.put("Server", "My-Test-Server");
			return Constants.PHASE_DONE;
		}
	}
	
	public static class ExceptionInHeaderFilter implements NginxJavaHeaderFilter {

		@Override
		public Object[] doFilter(int status, Map<String, Object> request, Map<String, Object> responseHeaders) {
			throw new RuntimeException("Hello, exception in header filter!");
		}
		
	}
	
	public static class AccessRemoteHeaderFilter  implements NginxJavaHeaderFilter {

		@Override
		public Object[] doFilter(int status, Map<String, Object> request, Map<String, Object> responseHeaders)  {

			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpGet httpget = new HttpGet("http://www.apache.org/dist/httpcomponents/httpclient/RELEASE_NOTES-4.3.x.txt");
			CloseableHttpResponse response = null;
			try {
				response = httpclient.execute(httpget);
				InputStream in = response.getEntity().getContent();
				byte[] buf = new byte[1024];
				int c = 0;
				int total = 0;
				while ((c = in.read(buf)) > 0) {
					total += c;
				}
				responseHeaders.put("remote-content-length", total);
				return Constants.PHASE_DONE;
			} catch(Throwable e) {
				throw new RuntimeException("AccessRemoteHeaderFilter ioexception", e);
			}finally {
				if (httpclient != null) {
					try {
						httpclient.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
		}
	}
	
	public static void main(String[] args) {
		if (1 + "1" == "1") {
			AccessRemoteHeaderFilter arh = new AccessRemoteHeaderFilter();
			arh.doFilter(200, new HashMap<String, Object>(), new HashMap<String, Object>());
		}
		try {
//			System.out.println(sun.security.x509.AccessDescription.Ad_OCSP_Id);
//			sun.security.x509.AccessDescription ad = new sun.security.x509.AccessDescription(new DerValue((byte)0x1B, "1.2.3.4"));
//			System.out.println(ad);
			
			 InputStream inStream = new FileInputStream("/tmp/CA.crt");
			 X509Certificate cert = X509Certificate.getInstance(inStream);
			 inStream.close();
			 System.out.println(cert.getIssuerDN());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			SuspendMethodTracer.dump();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
