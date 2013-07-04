package eu.trentorise.smartcampus.resourceprovider.uri;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriTemplate;

import eu.trentorise.smartcampus.resourceprovider.jaxbmodel.ResourceDeclaration;
import eu.trentorise.smartcampus.resourceprovider.jaxbmodel.ResourceMapping;
import eu.trentorise.smartcampus.resourceprovider.jaxbmodel.Service;
import eu.trentorise.smartcampus.resourceprovider.util.HttpMethod;

/**
 * 
 * @author federico
 * 
 */

@Component
public class UriManager {

	private final static Log logger = LogFactory.getLog(UriManager.class);

	private String contextPath;

	private String serviceId;
	
	private File tagProviderFile;

	public String getUriFromRequest(HttpServletRequest httpServletRequest,
			Collection<GrantedAuthority> collection) {

		String url = getFullURL(httpServletRequest);
		HttpMethod method= HttpMethod.valueOf(httpServletRequest.getMethod());

		Service service = loadResourceTemplates(tagProviderFile);
		if (service != null) {
			List<ResourceMapping> listPath = service.getResourceMapping();
			Iterator<ResourceMapping> index = listPath.iterator();
			while (index.hasNext()) {
				ResourceMapping rm = index.next();
				UriTemplate pathPattern = rm.getPathTemplate();
				//System.out.println(collection.toString());
				//System.out.println(rm.getAuthority());
				//System.out.println(pattern);
			//	if (collection.toString().contains(rm.getAuthority())) {
				if (pathPattern.matches(url) && (rm.getMethod()==null || rm.getMethods().contains(method.toString()))) {
					Map<String, String> match = pathPattern.match(url);
					logger.info("Check  " + pathPattern +" " +rm.getMethod()+" ==> " + match );
					return rm.getUriTemplate().expand(match).toString();
				}
			}
		}else{
			logger.info("Service non registered ");
		}
		logger.info("Resource not exist on this service: " + url);
		return null;

	}

	public String getFullURL(HttpServletRequest request) {
		StringBuffer requestURL = request.getRequestURL();

		String queryString = request.getQueryString();

		if (queryString == null) {// TODO http://localhost:8080/profileservice/
									// crash String index out of range: -1
			return requestURL.substring(requestURL.indexOf(contextPath))
					.toString();
		} else {
			return requestURL.append('?').append(queryString).toString()
					.substring(requestURL.indexOf(contextPath));
		}
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public File getTagProviderFile() {
		return tagProviderFile;
	}

	public void setTagProviderFile(File tagProviderFile) {
		this.tagProviderFile = tagProviderFile;
	}
	
	private Service loadResourceTemplates(File tagProviderFile) {
		try {
			JAXBContext jaxb = JAXBContext.newInstance(Service.class,
					Service.class, ResourceMapping.class,
					ResourceDeclaration.class);
			Unmarshaller unm = jaxb.createUnmarshaller();
			JAXBElement<Service> element = (JAXBElement<Service>) unm
					.unmarshal(new StreamSource(new FileInputStream(
							tagProviderFile)), Service.class);
			return element.getValue();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(
					"Failed to load resource templates: " + e.getMessage(), e);
			return null;
		}
	}

}
