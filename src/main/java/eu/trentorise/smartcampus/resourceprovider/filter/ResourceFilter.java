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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;

import eu.trentorise.smartcampus.resourceprovider.util.HttpMethod;

/**
 * Implementation of the {@link OAuth2AuthenticationProcessingFilter} relying
 * on the {@link ResourceAuthenticationManager} for actual authentication.
 * @author raman
 *
 */
public class ResourceFilter extends OAuth2AuthenticationProcessingFilter {

	private final static Log logger = LogFactory.getLog(ResourceFilter.class);

	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new OAuth2AuthenticationDetailsSource();

	public ResourceFilter() {
		this.setTokenExtractor( new CustomTokenExtractor());
	}
	
	public String getFullURL(HttpServletRequest request) {
		String cp = request.getContextPath();
		if (cp == null || cp.isEmpty()) {
			return request.getRequestURI();
		}
		return request.getRequestURI().substring(cp.length());
	}

	
	@Override
	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		super.setAuthenticationDetailsSource(authenticationDetailsSource);
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

	public class CustomTokenExtractor extends BearerTokenExtractor {

		@Override
		public Authentication extract(HttpServletRequest request) {
			String tokenValue = extractToken(request);
			if (tokenValue != null) {
				ResourceCallAuthenticationToken authentication = new ResourceCallAuthenticationToken(tokenValue, "");
				authentication.setDetails(authenticationDetailsSource.buildDetails(request));
				authentication.setRequestPath(getFullURL(request));
				authentication.setHttpMethod(HttpMethod.valueOf(request.getMethod()));
				return authentication;
			}
			return null;
		}
		
	}
	
}
