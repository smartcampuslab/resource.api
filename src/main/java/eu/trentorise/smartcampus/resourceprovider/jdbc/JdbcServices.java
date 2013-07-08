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

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.JdbcClientDetailsService;

import eu.trentorise.smartcampus.resourceprovider.model.AuthServices;
import eu.trentorise.smartcampus.resourceprovider.model.User;

/**
 * JDBC-based implementation of the {@link AuthServices} 
 * @author federico
 * 
 */

public class JdbcServices extends JdbcTemplate implements AuthServices {
	private static final String DEFAULT_RESOURCE_SELECT_STATEMENT = "select authority from resource where resourceUri = ?";
	private static final String DEFAULT_USER_SELECT_STATEMENT = "select * from user where userId = ?";
	
	private String selectResourceSql = DEFAULT_RESOURCE_SELECT_STATEMENT;
	private String selectUserSql = DEFAULT_USER_SELECT_STATEMENT;

	private final static Log logger = LogFactory.getLog(JdbcServices.class);

	ClientDetailsService clientDetailsService;
	
	public JdbcServices(DataSource dataSource) {
		super(dataSource);
		this.clientDetailsService = new JdbcClientDetailsService(dataSource);
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
	
	@Override
	public ClientDetails loadClientByClientId(String clientId) {
		return clientDetailsService.loadClientByClientId(clientId);
	}

	@Override
	public User loadUserByUserId(String userId) {
		queryForObject(selectUserSql, new RowMapper<User>(){
			@Override
			public User mapRow(ResultSet rs, int rowNum) throws SQLException {
				rs.absolute(rowNum);
				User user = new User();
				user.setId(rs.getLong("id"));
				user.setName(rs.getString("name"));
				user.setSurname(rs.getString("surname"));
				return user;
			}
			
		}, userId);
		return null;
	}

	
}
