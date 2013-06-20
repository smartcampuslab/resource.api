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

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author federico
 * 
 */

public class JdbcResourceServices extends JdbcTemplate {
	private static final String DEFAULT_RESOURCE_SELECT_STATEMENT = "select resourceId from Resource where resourceUri = ?";
	private String selectResourceSql = DEFAULT_RESOURCE_SELECT_STATEMENT;
	private final static Log logger = LogFactory.getLog(JdbcResourceServices.class);

	public JdbcResourceServices(DataSource dataSource) {
		super(dataSource);
	}

	public String loadResourceByResourceUri(String resourceUri) {
		String resourceId = null;

		try {
			Object[] parameters = new Object[] {resourceUri};
			resourceId = queryForObject(selectResourceSql,parameters,String.class);
		} catch (EmptyResultDataAccessException e) {
			logger.error("No resource found "+ resourceUri );
		}

		return resourceId;
	}

}
