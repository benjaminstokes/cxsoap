package com.checkmarx.api.demo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.checkmarx.api.cxportal.ArrayOfCxWSItemAndCRUD;
import com.checkmarx.api.cxportal.ArrayOfGroup;
import com.checkmarx.api.cxportal.CxUserTypes;
//import com.checkmarx.api.cxportal.CxWSItemAndCRUD;
import com.checkmarx.api.cxportal.CxWSRoleWithUserPrivileges;
import com.checkmarx.api.cxportal.Group;
import com.checkmarx.api.cxportal.GroupType;
import com.checkmarx.api.cxportal.TeamData;
import com.checkmarx.api.cxportal.UserData;

public class CxPortalClientTests extends SpringUnitTest {
	
	private static final Logger log = LoggerFactory.getLogger(CxPortalClientTests.class);
	
	public static final String HOST = "http://cxlocal";
	
	@Autowired
	private CxPortalClient portal;
	private String sessionId;
	
	@Before
	public void setup() {
		assertThat(portal, is(notNullValue()));

		sessionId = portal.login(HOST, "admin@cx", "Im@hom3y!!");
		log.debug("login: session={}", sessionId);
		assertThat(sessionId, is(notNullValue()));
		assertThat(sessionId, not(isEmptyString()));
	}

	@Test
	public void testGetQueryDesc() {
		log.trace("testGetQueryDesc()");

		final String desc = portal.getQueryDescription(sessionId, 138);
		assertThat(desc, is(notNullValue()));
		assertThat(desc, not(isEmptyString()));
		
		log.debug("\n{}", desc);
	}
	
	@Test
	public void testGetAllTeams() {
		log.trace("testGetAllTeams()");
		
		final List<TeamData> teams = portal.getAllTeams(sessionId);
		assertThat(teams, is(notNullValue()));
		assertThat(teams.size(), is(greaterThan(0)));
		teams.forEach((team) -> {
			final Group group = team.getTeam();
			log.debug("team: name={}; id={}; guid={}; type={}; path={}; fullPath={}",
					group.getGroupName(),
					group.getID(),
					group.getGuid(),
					group.getType(),
					group.getPath(),
					group.getFullPath());
		});
	}
	
	@Test
	public void testGetAllUsers() {
		log.trace("testGetAllUsers()");
		
		final List<UserData> users = portal.getAllUsers(sessionId);
		assertThat(users, is(notNullValue()));
		assertThat(users.size(), is(greaterThan(0)));
		users.forEach((user) -> {
			log.debug("{}", CxPortalUtils.printUserData(user, true));
		});
	}
	
	@Test
	public void testGetUserById() {
		log.trace("testGetUserById()");
		
		final long userId = 2;
		final UserData user = portal.getUserById(sessionId, userId);
		assertThat(user, is(notNullValue()));
		log.debug("{}", CxPortalUtils.printUserData(user, true));
	}
	
	@Test
	public void testAddUser() throws Exception {
		log.trace("testGetAllUsers()");

		final UserData userData = createUserData();
		
		final long userId = portal.addUser(sessionId, userData, CxUserTypes.APPLICATION);
		log.debug("New user: id={}", userId);
		assertThat(userId, is(greaterThan(new Long(0))));
		
		portal.deleteUser(sessionId, userId);
	}
	
	private UserData createUserData() throws DatatypeConfigurationException {

		final XMLGregorianCalendar date = 
				DatatypeFactory.newInstance().newXMLGregorianCalendar("0001-01-01T00:00:00");
		
		final String email = "test@test.com"; 
		UserData userData = new UserData();
		userData.setID(0);
		userData.setFirstName("first");
		userData.setLastName("last");
		userData.setUserPreferedLanguageLCID(1033);
		userData.setPassword("Im@hom3y!!");
		userData.setJobTitle(null);
		userData.setEmail(email);
		userData.setUserName(email);
		userData.setPhone(null);
		userData.setCellPhone(null);
		userData.setSkype(null);
		userData.setWillExpireAfterDays("10");
		userData.setCountry(null);
		userData.setDateCreated(date);
		userData.setAuditUser(false);
		final ArrayOfGroup groupList = new ArrayOfGroup();
		final Group group = new Group();
		//group.setGroupName("Users");
		//group.setGuid("22222222-2222-448d-b029-989c9070eb23");
		group.setGroupName("Sub-Team1");
		group.setGuid("6d78e6bd-0f7f-4061-acdd-80fece33801f");
		group.setType(GroupType.TEAM);
		groupList.getGroup().add(group);
		userData.setGroupList(groupList);
		userData.setLastLoginDate(date);
		userData.setLimitAccessByIPAddress(false);
		userData.setIsActive(false);
		final CxWSRoleWithUserPrivileges userRole = new CxWSRoleWithUserPrivileges();
		final ArrayOfCxWSItemAndCRUD itemsCrud = new ArrayOfCxWSItemAndCRUD();
		//final CxWSItemAndCRUD itemCrud = new CxWSItemAndCRUD();
		//itemsCrud.getCxWSItemAndCRUD().add(itemCrud);
		//userRole.setItemsCRUD(itemsCrud);
		userRole.setItemsCRUD(itemsCrud);
		userRole.setID("0");  // scanner w/ default permissions
		userData.setRoleData(userRole);

		return userData;
	}

}
