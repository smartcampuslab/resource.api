package eu.trentorise.smartcampus.resourceprovider.filter;

import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import eu.trentorise.smartcampus.resourceprovider.util.Oauth2ExtRequest;

public class ResourceAssistant {

	@Autowired
	private String permissionHostUrl;

	public boolean checkPermission(Oauth2ExtRequest forwardRequest) {
		WebClient client = WebClient.create(permissionHostUrl);

		Boolean resp = client.path("/resources/access")
				.accept(MediaType.APPLICATION_JSON).header(OAuth2AccessToken.ACCESS_TOKEN, forwardRequest.getOauthTokenOfService())
				.post(forwardRequest, Boolean.class)
				;
		// return resp;

		return resp;
	}

	public String getPermissionHostUrl() {
		return permissionHostUrl;
	}

	public void setPermissionHostUrl(String permissionHostUrl) {
		this.permissionHostUrl = permissionHostUrl;
	}

}
