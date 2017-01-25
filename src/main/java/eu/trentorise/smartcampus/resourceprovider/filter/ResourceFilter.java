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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationProcessingFilter;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;

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
	private AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
	private AuthenticationManager authenticationManager;

	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		final boolean debug = logger.isDebugEnabled();
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;

		try {

			String tokenValue = parseToken(request);
			if (tokenValue == null) {
				if (debug) {
					logger.debug("No token in request, will continue chain.");
				}
				throw new OAuth2Exception("empty token");
			} else if (HttpMethod.OPTIONS.equals(HttpMethod.valueOf(request.getMethod()))) {
				chain.doFilter(request, response);
//				throw new OAuth2Exception("options");
			} else {
				ResourceCallAuthenticationToken authentication = new ResourceCallAuthenticationToken(tokenValue, "");
				request.setAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE, tokenValue);
				authentication.setDetails(authenticationDetailsSource.buildDetails(request));
				authentication.setRequestPath(getFullURL(request));
				authentication.setHttpMethod(HttpMethod.valueOf(request.getMethod()));
				Authentication authResult = authenticationManager.authenticate(authentication);

				SecurityContextHolder.getContext()
						.setAuthentication(authResult);

				chain.doFilter(request, response);

			}
		} catch (OAuth2Exception failed) {
			SecurityContextHolder.clearContext();

			if (debug) {
				logger.debug("Authentication request failed: " + failed);
			}

			authenticationEntryPoint.commence(request, response,
					new InsufficientAuthenticationException(
							failed.getMessage(), failed));

			return;
		}

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

	@Override
	public void setAuthenticationEntryPoint(AuthenticationEntryPoint authenticationEntryPoint) {
		super.setAuthenticationEntryPoint(authenticationEntryPoint);
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	@Override
	public void setAuthenticationManager(AuthenticationManager authenticationManager) {
		super.setAuthenticationManager(authenticationManager);
		this.authenticationManager = authenticationManager;
	}

	
	
}
