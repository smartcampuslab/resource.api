package eu.trentorise.smartcampus.resourceprovider.filter;

import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetailsSource;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.JdbcTokenStore;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

import eu.trentorise.smartcampus.resourceprovider.jdbc.JdbcResourceServices;
import eu.trentorise.smartcampus.resourceprovider.uri.UriManager;

public class ResourceFilter implements Filter, InitializingBean {

	private final static Log logger = LogFactory.getLog(ResourceFilter.class);

	private AuthenticationEntryPoint authenticationEntryPoint = new OAuth2AuthenticationEntryPoint();

	private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new OAuth2AuthenticationDetailsSource();

	private UriManager uriManager;

	private TokenStore tokenStore;
	private JdbcResourceServices resourceStore;
	private DataSource dataSource;

	/**
	 * @param authenticationEntryPoint
	 *            the authentication entry point to set
	 */
	public void setAuthenticationEntryPoint(
			AuthenticationEntryPoint authenticationEntryPoint) {
		this.authenticationEntryPoint = authenticationEntryPoint;
	}

	// TODO jdbc su risorsa
	// todo elimina resourctoken service e usa jdbc di raman

	/**
	 * @param authenticationDetailsSource
	 *            The AuthenticationDetailsSource to use
	 */
	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		Assert.notNull(authenticationDetailsSource,
				"AuthenticationDetailsSource required");
		this.authenticationDetailsSource = authenticationDetailsSource;
	}

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
			} else {
				PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
						tokenValue, "");
				request.setAttribute(
						OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE,
						tokenValue);
				authentication.setDetails(authenticationDetailsSource
						.buildDetails(request));
				Authentication authResult = authenticate(authentication,
						((HttpServletRequest) request));

				
					

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

	protected String parseToken(HttpServletRequest request) {
		// first check the header...
		String token = parseHeaderToken(request);

		// bearer type allows a request parameter as well
		if (token == null) {
			logger.debug("Token not found in headers. Trying request parameters.");
			token = request.getParameter(OAuth2AccessToken.ACCESS_TOKEN);
			if (token == null) {
				logger.debug("Token not found in request parameters.  Not an OAuth2 request.");
			}
		}

		return token;
	}

	/**
	 * Parse the OAuth header parameters. The parameters will be oauth-decoded.
	 * 
	 * @param request
	 *            The request.
	 * @return The parsed parameters, or null if no OAuth authorization header
	 *         was supplied.
	 */
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

	public void init(FilterConfig filterConfig) throws ServletException {

	}

	public void destroy() {
	}

	public Authentication authenticate(Authentication authentication,
			HttpServletRequest request) throws AuthenticationException {

		setResourceStore(new JdbcResourceServices(dataSource));
		
		String token = (String) authentication.getPrincipal();
		OAuth2Authentication auth = loadAuthentication(token);
		

		if (auth == null) {
			throw new InvalidTokenException("Invalid token: " + token);
		}
		
		String resourceUri = uriManager
				.getUriFromRequest(((HttpServletRequest) request),auth.getAuthorities());

		

		String resourceID =   resourceUri ;//resourceStore.loadResourceByResourceUri(resourceUri); test senza lettura db

		Collection<String> resourceIds = auth.getAuthorizationRequest().getScope();
				
		
		
		if (resourceID == null  ||  resourceIds.isEmpty()
				|| !resourceIds.contains(resourceID)) {
			throw new OAuth2AccessDeniedException(
					"Invalid token does not contain resource id ("
							+ resourceUri + ")");
		}

		

		auth.setDetails(authentication.getDetails());
		return auth;

	}

	private OAuth2Authentication loadAuthentication(String token) {
		tokenStore = new JdbcTokenStore(dataSource);

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

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	public TokenStore getTokenStore() {
		return tokenStore;
	}

	public void setTokenStore(TokenStore tokenStore) {
		this.tokenStore = tokenStore;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public JdbcResourceServices getResourceStore() {
		return resourceStore;
	}

	public void setResourceStore(JdbcResourceServices resourceStore) {
		this.resourceStore = resourceStore;
	}

	public UriManager getUriManager() {
		return uriManager;
	}

	public void setUriManager(UriManager uriManager) {
		this.uriManager = uriManager;
	}
	
	

}
