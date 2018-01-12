package login;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;







public class GoogleResponse extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, java.io.IOException { 
		HashMap<String,String> details=new HashMap<String,String>();
			try {
				String resp = getTokenFromGoogle(request, request.getParameter("code"), GoogleLogin.callback);
				JSONObject json = new JSONObject(resp);
				String accessToken = json.getString("access_token");
				String jwtString = json.getString("id_token");
				JSONObject jsonObject =  getJsonFromJWT(jwtString);
				JSONObject profileInfo = getUserDetails(accessToken, json.getString("token_type"));
					if(profileInfo != null && !profileInfo.has("error")) {
						jsonObject.append("profileInfo", profileInfo);
						request.getSession().setAttribute("userinfo", jsonObject);
					}
					response.sendRedirect("/OneStop/dashboard.jsp");

			} catch (Exception e) {
				// TODO Auto-generated catch block
				response.getWriter().println(e);
			}
			//.getString("access_token");
			
			/*
			*/
		

	}
	
	public static JSONObject getJsonFromJWT(String jwtString) throws java.io.UnsupportedEncodingException  {
		
			String[] pieces = jwtString.split(Pattern.quote("."));
	        byte[] decodedJsonHeader = Base64.getDecoder().decode(pieces[0]);
			String jsonHeaderStr = new String(decodedJsonHeader, "UTF-8");//No I18N
			JSONObject jsonHeader = new JSONObject(jsonHeaderStr);
			String pubKeyId;
			if(jsonHeader.has("kid")) {
				pubKeyId =  jsonHeader.getString("kid");
			} 
			byte[] decodedJsonPayLoad = Base64.getDecoder().decode(pieces[1]);
			String jsonPayloadStr = new String(decodedJsonPayLoad, "UTF-8");//No I18N
			JSONObject jsonPayLoad = new JSONObject(jsonPayloadStr);
			return jsonPayLoad;
			
	}
	
	
	   public static String connectAndPostwithException(String url, int timeout,String params,String ipaAddress, Map<String,String> headers, String contentType) throws Exception {
	    	HttpURLConnection urlConnection = null;
	        InputStream is = null;
	        OutputStream os = null;
	        BufferedReader br = null;
	        try {
	        	urlConnection = (HttpURLConnection) (new URL(url).openConnection());
	            urlConnection.setDoInput(true);
	            urlConnection.setDoOutput(true);
	            urlConnection.setRequestMethod("POST"); // No I18N
	            urlConnection.setConnectTimeout(timeout);
	            urlConnection.setReadTimeout(5000);
	            if(ipaAddress!=null){
	            	urlConnection.addRequestProperty("REMOTE_USER_IP", ipaAddress); // No I18N
	            }
	            if(contentType == null){
	            	contentType =  "application/x-www-form-urlencoded;charset=UTF-8"; //No I18N
	            }
	            urlConnection.setRequestProperty("Content-Type", contentType); //No I18N
	            if(headers != null && !headers.isEmpty()) {
	            	java.util.Set<java.util.Map.Entry<String,String>> entrySet = headers.entrySet();
	            	for(java.util.Map.Entry<String,String> header : entrySet) {
	            		urlConnection.setRequestProperty(header.getKey(), header.getValue());
	            	}
	            }
	            os = urlConnection.getOutputStream();
	            if(params!=null){
	            	os.write(params.getBytes());
	            }
	            os.flush();
	            os.close();
	            urlConnection.connect();
	            if(urlConnection.getResponseCode() != 200) {
	            	is = urlConnection.getErrorStream();
	            } else {
	            	is = urlConnection.getInputStream();
	        	}

	            if(is != null) {
	            	StringBuilder sb = new StringBuilder();
	                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	                String eachLine = br.readLine();
	                while (eachLine != null) {
	                    sb.append(eachLine);
	                    eachLine = br.readLine();
	                }
	                br.close();
	                is.close();
	                return sb.toString();
	            }
	        } catch(Exception ex) {
	        	System.out.println(ex.toString());
	        }
	        return null;
	        
	   }

	   
	   public static byte[] connectAndGetAsBytes(String url, int timeout, Map<String, String> headers) throws IOException {

	    	java.io.ByteArrayOutputStream baos = null;
	    	InputStream is = null;
	    	BufferedReader br = null;

	    	try {

	    		HttpURLConnection urlConnection = (HttpURLConnection) (new URL(url).openConnection());
	    		urlConnection.setConnectTimeout(timeout);
	    		urlConnection.setReadTimeout(timeout);
	    		if (headers != null && !headers.isEmpty()) {
	    			java.util.Set<java.util.Map.Entry<String, String>> entrySet = headers.entrySet();
	    			for (java.util.Map.Entry<String, String> entry : entrySet) {
	    				urlConnection.addRequestProperty(entry.getKey(), entry.getValue());
	    			}
	    		}

	    		urlConnection.connect();
	    		if (urlConnection.getResponseCode() < 200 || urlConnection.getResponseCode()>=300) {
	    			is = urlConnection.getErrorStream();
	    		} else {
	    			is = urlConnection.getInputStream();
	    		}
	    		baos = new java.io.ByteArrayOutputStream();
	    		int r;
	    		while ((r = is.read()) != -1) {
	    			baos.write(r);
	    		}
	    		return baos.toByteArray();
	    	} finally {
	    	}
	    }

	   
	    public static String connectAndGet(String url, int timeout, Map<String, String> headers) {
	    	try {

	    		byte[] bytes = connectAndGetAsBytes(url, timeout, headers);
	    		if (bytes != null) {
	    			try {
	    				return new String(bytes, "UTF-8"); //No I18N
	    			} catch (Exception e) {
	    				
	    			}
	    		}
	    	} catch (IOException e) {
	    	}
	    	return null;
	    }
	   
		public static JSONObject getUserDetails(String accessToken, String tokentype) {
			 try {
				 String url = "https://www.googleapis.com/plus/v1/people/me";//No I18N
//				String url = "https://www.googleapis.com/oauth2/v3/userinfo"; // No I18N
				int connectionTimeOut = 3000;
				Map<String, String> headerMap = new HashMap<String, String>();
				headerMap.put("authorization", tokentype + " " + accessToken);
				String resp = connectAndGet(url, connectionTimeOut, headerMap);
				return new JSONObject(resp);
			} catch (Exception e) {
				return null;
			}
		}
	
	public static String getTokenFromGoogle(HttpServletRequest request, String code, String redirect_uri) throws Exception{

		String googleOAuthClientId = null; 
		String client_secret= null;		
		googleOAuthClientId = GoogleLogin.CONSUMER_KEY;
		client_secret=GoogleLogin.CONSUMER_SECRET;
					
		String params =  "code="+code +"&client_id="+googleOAuthClientId+"&client_secret="+client_secret+"&redirect_uri="+redirect_uri +"&grant_type=authorization_code";//No I18N
		int connectionTimeOut = 3000;//No I118N
		return connectAndPostwithException("https://accounts.google.com/o/oauth2/token", 3000, params, null, null, null);

	}

	


}