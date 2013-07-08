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

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import eu.trentorise.smartcampus.resourceprovider.util.HttpMethod;

/**
 * Extension of the {@link PreAuthenticatedAuthenticationToken} to carry also the 
 * request path and http method necessary to identify the validiated resource. 
 * @author raman
 *
 */
public class ResourceCallAuthenticationToken extends PreAuthenticatedAuthenticationToken {


	private static final long serialVersionUID = -2338285215792655112L;

	private String requestPath;
	private HttpMethod httpMethod;
	
	public ResourceCallAuthenticationToken(Object aPrincipal, Object aCredentials) {
		super(aPrincipal, aCredentials);
	}
	public ResourceCallAuthenticationToken(Object aPrincipal, Object aCredentials, Collection<? extends GrantedAuthority> anAuthorities) {
		super(aPrincipal, aCredentials, anAuthorities);
	}
	public String getRequestPath() {
		return requestPath;
	}
	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}
	public HttpMethod getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}
}
