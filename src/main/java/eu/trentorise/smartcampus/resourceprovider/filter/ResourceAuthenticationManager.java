/**
 *    Copyright 2012-2013 Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package eu.trentorise.smartcampus.resourceprovider.filter;

import java.io.IOException;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;

import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
import eu.trentorise.smartcampus.resourceprovider.uri.UriManager;

/**
 * Authentication manager used to check the access for the specific resource. 
 * The resource is identified based on the requested resource path.
 * 
 * @author raman
 *
 */
public class ResourceAuthenticationManager implements AuthenticationManager {

	/**
	 * 
	 */
	private static final String ROLE_USER = "ROLE_USER";

	private static final String ROLE_CLIENT = "ROLE_CLIENT";

	private Log logger = LogFactory.getLog(getClass());

	private AuthServices authServices = null;
	private TokenStore tokenStore = null;
	private UriManager uriManager = null;

	private Resource resourceDescriptor;
	/**
	 * Instance of the uri manager based on the resource descriptor file.
	 * @return
	 * @throws IOException
	 */
	protected UriManager getUriManager() throws IOException {
		if (uriManager == null) {
			try {
				if (resourceDescriptor != null) {
					uriManager = new UriManager(resourceDescriptor.getInputStream());
				} else {
					uriManager = new UriManager(getClass().getClassLoader().getResourceAsStream("/resourceList.xml"));
				}
			} catch (IOException e) {
				logger .error("Failed to load resources: "+e.getMessage(),e);
				throw e;
			}
		}
		return uriManager;
	}
	/**
	 * Check whether the access to the specific resource is granted. The The resource is identified
	 * from the {@link ResourceCallAuthenticationToken} fields {@link ResourceCallAuthenticationToken#getRequestPath()}
	 * and {@link ResourceCallAuthenticationToken#getHttpMethod()}. 
	 * @param authentication the authentication token object as instance of {@link ResourceCallAuthenticationToken}.
	 */
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {

		assert authentication instanceof ResourceCallAuthenticationToken;
		ResourceCallAuthenticationToken rcAuth = (ResourceCallAuthenticationToken) authentication;
		
		String token = (String) rcAuth.getPrincipal();
		OAuth2Authentication auth = loadAuthentication(token);
		
		
		if (auth == null) {
			throw new InvalidTokenException("Invalid token: " + token);
		}

		String resourceUri;
		try {
			resourceUri = getUriManager().getUriFromRequest(rcAuth.getRequestPath(), rcAuth.getHttpMethod(), auth.getAuthorities());
		} catch (IOException e) {
			throw new OAuth2Exception("Problem accessing resource descriptor");
		}

		String resourceID = resourceUri;// resourceStore.loadResourceByResourceUri(resourceUri);
										// test senza lettura db

		Collection<String> resourceIds = auth.getAuthorizationRequest()
				.getScope();

		if (resourceID == null || resourceIds.isEmpty()
				|| !resourceIds.contains(resourceID)) {
			throw new OAuth2AccessDeniedException(
					"Invalid token does not contain resource id ("
							+ resourceUri + ")");
		}
		
		if (ROLE_USER.equals(authServices.loadResourceAuthorityByResourceUri(resourceUri)) && auth.isClientOnly()) {
			throw new OAuth2Exception("Incorrect access method");
		} 
		if (ROLE_CLIENT.equals(authServices.loadResourceAuthorityByResourceUri(resourceUri)) && !auth.isClientOnly()) {
			throw new OAuth2Exception("Incorrect access method");
		} 
		
		auth.setDetails(authentication.getDetails());
		
		return auth;
	}

	private OAuth2Authentication loadAuthentication(String token) {
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(token);
		if (accessToken == null) {
			throw new InvalidTokenException("Invalid access token: " + token);
		} else if (accessToken.isExpired()) {
			tokenStore.removeAccessToken(accessToken);
			throw new InvalidTokenException("Access token expired: " + token);
		}

		OAuth2Authentication result = tokenStore
				.readAuthentication(accessToken);
		return result;
	}

	public AuthServices getAuthServices() {
		return authServices;
	}

	public void setAuthServices(AuthServices authServices) {
		this.authServices = authServices;
	}

	public TokenStore getTokenStore() {
		return tokenStore;
	}

	public void setTokenStore(TokenStore tokenStore) {
		this.tokenStore = tokenStore;
	}

	public Resource getResourceDescriptor() {
		return resourceDescriptor;
	}

	public void setResourceDescriptor(Resource resourceDescriptor) {
		this.resourceDescriptor = resourceDescriptor;
	}
	
	
}
