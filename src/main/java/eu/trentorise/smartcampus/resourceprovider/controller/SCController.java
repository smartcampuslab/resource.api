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
package eu.trentorise.smartcampus.resourceprovider.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
import eu.trentorise.smartcampus.resourceprovider.model.User;

/**
 * Controller prototype with the helper methods for reading user data
 * @author raman
 *
 */
@Controller
public abstract class SCController {

	/**
	 * @return reference to {@link AuthServices} instances
	 */
	protected abstract AuthServices getAuthServices();
	
	/**
	 * Read user with the specified id
	 * @param userId
	 * @return
	 */
	protected User getUserObject(String userId) {
		return getAuthServices().loadUserByUserId(userId); 
	}
	
	/**
	 * @return UserDetails instance from security context
	 */
	protected UserDetails getUser(){
		return (UserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
	}
	
	/**
	 * @return user id from the security context
	 */
	protected String getUserId() {
		return getUser().getUsername();
	}
}
