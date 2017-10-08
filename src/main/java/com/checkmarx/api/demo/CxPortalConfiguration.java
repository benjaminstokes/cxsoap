package com.checkmarx.api.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class CxPortalConfiguration {

	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		// this package(s) must match the package in the <generatePackage> specified in pom.xml
		marshaller.setPackagesToScan(
				//"com.checkmarx.api.cxresolver", 
				"com.checkmarx.api.cxportal");
		return marshaller;
	}

	@Bean
	public CxPortalClient cxPortalClient(Jaxb2Marshaller marshaller) {
		final CxPortalClient client = new CxPortalClient();
		client.setDefaultUri("http://cxlocal/CxWebInterface/Portal/CxWebService.asmx");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		return client;
	}
	
}
