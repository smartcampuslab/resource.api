package eu.trentorise.smartcampus.resourceprovider.jdbc;

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

import java.io.IOException;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author federico
 * 
 */

public class JdbcServices extends JdbcTemplate {
	private static final String DEFAULT_RESOURCE_SELECT_STATEMENT = "select authority from resource where resourceUri = ?";
	
	private static final String DEFAULT_ADDINFO_SELECT_STATEMENT = "SELECT additional_information FROM  oauth_client_details cl where cl.client_id= ?";
	
	private String selectAddSql = DEFAULT_ADDINFO_SELECT_STATEMENT;
	
	private String selectResourceSql = DEFAULT_RESOURCE_SELECT_STATEMENT;
	private static ObjectMapper mapper = new ObjectMapper();
	private final static Log logger = LogFactory.getLog(JdbcServices.class);

	public JdbcServices(DataSource dataSource) {
		super(dataSource);
	}

	public String loadResourceAuthorityByResourceUri(String resourceUri) {
		String resourceAuthority = null;

		try {
			Object[] parameters = new Object[] {resourceUri};
			resourceAuthority = queryForObject(selectResourceSql,parameters,String.class);
		} catch (EmptyResultDataAccessException e) {
			logger.error("No resource found "+ resourceUri );
		}

		return resourceAuthority;
	}
	
	public Map<String,String> loadAddInfoByToken(String clientId) throws JsonParseException, JsonMappingException, IOException {
		String resourceId = null;

		try {
			Object[] parameters = new Object[] {clientId};
			resourceId = queryForObject(selectAddSql,parameters,String.class);
		} catch (EmptyResultDataAccessException e) {
			logger.error("No add info found "+ clientId );
		}

		return mapper.readValue(resourceId, Map.class);
	}

}
