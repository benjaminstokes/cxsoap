package com.checkmarx.api.demo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CxPortalUtilsTests extends SpringUnitTest {
	
	private static final Logger log = LoggerFactory.getLogger(CxPortalUtilsTests.class);
	
	@Autowired
	private CxPortalClient portal;
	private CxPortalUtils portalUtils;

	@Before
	public void setUp() throws Exception {
		assertThat(portal, is(notNullValue()));
		
		portalUtils = CxPortalUtils.factory(portal, CxPortalClientTests.HOST, "admin@cx", "Im@hom3y!!");
		assertThat(portalUtils, is(notNullValue()));
	}

	@Test
	public void testFindAllScanners() {
		log.trace("testFindAllScanners()");
		
		final List<CxUser> scanners = portalUtils.findAllScanners();
		assertThat(scanners, is(notNullValue()));
		assertThat(scanners.size(), is(greaterThan(0)));
		scanners.forEach((user) -> {
			log.debug("{}", CxPortalUtils.printUserData(user, true));
		});
	}
	
	@Test
	public void testFindAllReviewers() {
		log.trace("testFindAllReviewers()");
		
		final List<CxUser> reviewers = portalUtils.findAllReviewers();
		assertThat(reviewers, is(notNullValue()));
		assertThat(reviewers.size(), is(greaterThan(0)));
		reviewers.forEach((user) -> {
			log.debug("{}", CxPortalUtils.printUserData(user, true));
		});
	}
	
	@Test
	public void testFindUsersWithSetNotExploitable() {
		log.trace("testFindUsersWithSetNotExploitable()");
		
		final List<CxUser> scanners = portalUtils.findUsersWithSetNotExploitable();
		assertThat(scanners, is(notNullValue()));
		scanners.forEach((user) -> {
			log.debug("{}", CxPortalUtils.printUserData(user, false));
		});
		
	}
	
}
