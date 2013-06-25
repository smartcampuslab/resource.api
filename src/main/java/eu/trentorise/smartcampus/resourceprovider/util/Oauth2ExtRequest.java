package eu.trentorise.smartcampus.resourceprovider.util;

import java.io.Serializable;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

/**
 * 
 * @author federico
 * 
 */
public class Oauth2ExtRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6655806319501152618L;

	// private String urlRequest;
	private String oauthTokenFromRequest;
	private String oauthTokenOfService;
	// private String method;
	private String serviceId;
	private boolean requestFromAnother;

	private String resourceUri;

	/**
	 * 
	 * @param serviceid
	 *            Identifier of External Service
	 * @param resourceuri
	 *            Resource identifier for external service
	 * @param owntoken
	 *            token of service
	 * @param requesttoken
	 *            token from request to service
	 */
	public Oauth2ExtRequest(String serviceid, String resourceuri,
			String owntoken, HttpServletRequest request) {
		// method=request.getMethod();
		// urlRequest=request.getRequestURL().toString();
		oauthTokenFromRequest = parseToken(request);
		oauthTokenOfService = owntoken;
		if (owntoken.compareTo(oauthTokenFromRequest) == 0) {
			requestFromAnother = false;
		} else {
			requestFromAnother = true;
		}
		serviceId = serviceid;
		resourceUri = resourceuri;
	}

	public boolean isRequestFromAnother() {
		return requestFromAnother;
	}

	public String getOauthTokenFromRequest() {
		return oauthTokenFromRequest;
	}

	public void setOauthTokenFromRequest(String oauthTokenFromRequest) {
		this.oauthTokenFromRequest = oauthTokenFromRequest;
	}

	public String getOauthTokenOfService() {
		return oauthTokenOfService;
	}

	public void setOauthTokenOfService(String oauthTokenOfService) {
		this.oauthTokenOfService = oauthTokenOfService;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getResourceUri() {
		return resourceUri;
	}

	public void setResourceUri(String resourceUri) {
		this.resourceUri = resourceUri;
	}

	protected String parseToken(HttpServletRequest request) {
		// first check the header...
		String token = parseHeaderToken(request);

		// bearer type allows a request parameter as well
		if (token == null) {

			token = request.getParameter(OAuth2AccessToken.ACCESS_TOKEN);
			if (token == null) {

			}
		}

		return token;
	}

	protected String parseHeaderToken(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Enumeration<String> headers = request.getHeaders("Authorization");
		while (headers.hasMoreElements()) { // typically there is only one (most
											// servers enforce that)
			String value = headers.nextElement();
			if ((value.toLowerCase().startsWith(OAuth2AccessToken.BEARER_TYPE
					.toLowerCase()))) {
				String authHeaderValue = value.substring(
						OAuth2AccessToken.BEARER_TYPE.length()).trim();
				int commaIndex = authHeaderValue.indexOf(',');
				if (commaIndex > 0) {
					authHeaderValue = authHeaderValue.substring(0, commaIndex);
				}
				return authHeaderValue;
			} else {
				// todo: support additional authorization schemes for different
				// token types, e.g. "MAC" specified by
				// http://tools.ietf.org/html/draft-hammer-oauth-v2-mac-token
			}
		}

		return null;
	}

}
