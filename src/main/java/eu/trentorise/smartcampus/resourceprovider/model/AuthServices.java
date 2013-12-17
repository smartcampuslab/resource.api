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

package eu.trentorise.smartcampus.resourceprovider.model;

import java.util.List;

import org.springframework.security.oauth2.provider.ClientDetails;

import eu.trentorise.smartcampus.social.model.User;

/**
 * This interface is used by the resource provider components to
 * get access to resource/client/user information.
 * @author raman
 *
 */
public interface AuthServices {

	/**
	 * @param resourceUri
	 * @return the authority of the specific resource
	 */
	String loadResourceAuthorityByResourceUri(String resourceUri);
	/**
	 * @param clientId
	 * @return {@link ClientDetails} object corresponding to the specific clientId
	 */
	ClientDetails loadClientByClientId(String clientId);
	/**
	 * 
	 * @param userId
	 * @return {@link User} object with the specific Id
	 */
	User loadUserByUserId(String userId);
	/**
	 * 
	 * @param socialId
	 * @return {@link User} object with the specific social Id
	 */
	User loadUserBySocialId(String socialId);
	/**
	 * 
	 * @param userId
	 * @return {@link App} object with the specific Id
	 */
	List<String> loadAppByUserId(String userId);


}
