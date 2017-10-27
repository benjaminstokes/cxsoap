package com.checkmarx.api.demo;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.client.core.SoapActionCallback;

import com.checkmarx.api.cxportal.AddNewUser;
import com.checkmarx.api.cxportal.AddNewUserResponse;
import com.checkmarx.api.cxportal.Credentials;
import com.checkmarx.api.cxportal.CxUserTypes;
import com.checkmarx.api.cxportal.CxWSBasicRepsonse;
import com.checkmarx.api.cxportal.DeleteUser;
import com.checkmarx.api.cxportal.DeleteUserResponse;
import com.checkmarx.api.cxportal.GetAllTeams;
import com.checkmarx.api.cxportal.GetAllTeamsResponse;
import com.checkmarx.api.cxportal.GetAllUsers;
import com.checkmarx.api.cxportal.GetAllUsersResponse;
import com.checkmarx.api.cxportal.GetQueryDescription;
import com.checkmarx.api.cxportal.GetQueryDescriptionResponse;
import com.checkmarx.api.cxportal.GetUserById;
import com.checkmarx.api.cxportal.GetUserByIdResponse;
import com.checkmarx.api.cxportal.Login;
import com.checkmarx.api.cxportal.LoginResponse;
import com.checkmarx.api.cxportal.ObjectFactory;
import com.checkmarx.api.cxportal.TeamData;
import com.checkmarx.api.cxportal.UserData;
import com.google.common.base.Stopwatch;

public class CxPortalClient extends WebServiceGatewaySupport {
	
	private static final Logger log = LoggerFactory.getLogger(CxPortalClient.class);
	
	private static final String PORTAL_URI = "/CxWebInterface/Portal/CxWebService.asmx";

	private final ObjectFactory objectFactory = new ObjectFactory();
	
	@FunctionalInterface
	public interface BasicResponse<T> {
		public CxWSBasicRepsonse getBasicResponse(T response);
	}

	public String login(String host, String user, String password) {
		log.trace("login() : host={}, user={}", host, user);

 		this.setDefaultUri(host + PORTAL_URI);
 		
		final String ACTION = "http://Checkmarx.com/Login";
		
		final Credentials credentials = objectFactory.createCredentials();
		credentials.setUser(user);
		credentials.setPass(password);
		
		final Login login = objectFactory.createLogin();
		login.setApplicationCredentials(credentials);
		login.setLcid(1033);
		
		final LoginResponse response = send(login, LoginResponse.class, ACTION, null);
		return response.getLoginResult().getSessionId();
	}
	
	public List<TeamData> getAllTeams(String sessionId) {
		log.trace("getAllTeams()");
		
		final String ACTION = "http://Checkmarx.com/GetAllTeams";
		
		final GetAllTeams request = objectFactory.createGetAllTeams();
		request.setSessionID(sessionId);
		
		final GetAllTeamsResponse response = send(request, GetAllTeamsResponse.class, ACTION,
				(r) -> r.getGetAllTeamsResult());
		return response.getGetAllTeamsResult().getTeamDataList().getTeamData();
	}
	
	public List<UserData> getAllUsers(String sessionId) {
		log.trace("getAllUsers()");
		
		final String ACTION = "http://Checkmarx.com/GetAllUsers";
		
		final GetAllUsers request = objectFactory.createGetAllUsers();
		request.setSessionID(sessionId);
		
		final GetAllUsersResponse response = send(request, GetAllUsersResponse.class, ACTION,
				(r) -> r.getGetAllUsersResult());
		return response.getGetAllUsersResult().getUserDataList().getUserData();
	}
	
	public UserData getUserById(String sessionId, long id) {
		log.trace("getUserById(): id={}", id);
		
		final String ACTION = "http://Checkmarx.com/GetUserById";
		
		final GetUserById request = objectFactory.createGetUserById();
		request.setSessionID(sessionId);
		request.setUserId(id);
		
		final GetUserByIdResponse response = send(request, GetUserByIdResponse.class, ACTION,
				(r) -> r.getGetUserByIdResult());
		return response.getGetUserByIdResult().getUserData();
	}
	
	public long addUser(String sessionId, UserData userData, CxUserTypes userType) {
		log.trace("addUser(): userName={}; userType={}", userData.getUserName(), userType);
		
		final String ACTION = "http://Checkmarx.com/AddNewUser";
		
		final AddNewUser request = objectFactory.createAddNewUser();
		request.setSessionID(sessionId);
		request.setUserData(userData);
		request.setUserType(userType);
		
		send(request, AddNewUserResponse.class, ACTION,  (r) -> r.getAddNewUserResult());
		
		return findNewUser(sessionId, userData.getUserName());
	}
	
	public void deleteUser(String sessionId, long userId) {
		log.trace("deleteUser() : userId={}", userId);

		final String ACTION = "http://Checkmarx.com/DeleteUser";
		
		final DeleteUser request = objectFactory.createDeleteUser();
		request.setSessionID(sessionId);
		final int id = (new Long(userId)).intValue();
		request.setUserID(id);
		
		send(request, DeleteUserResponse.class, ACTION,  (r) -> r.getDeleteUserResult());
	}

	private long findNewUser(String sessionId, String userName) {
		log.trace("findNewUser() : userName={}", userName);
		
		final List<UserData> allUsers = getAllUsers(sessionId);
		for (UserData user : allUsers) {
			if (user.getUserName().equals(userName)) return user.getID();
		}
		return 0;
	}
	
	public String getQueryDescription(String sessionId, int cweId) {
		log.trace("getQueryDescription() : cweId={}", cweId);
		
		final String ACTION = "http://Checkmarx.com/GetQueryDescription";

		final GetQueryDescription request = objectFactory.createGetQueryDescription();
		request.setSessionId(sessionId);
		request.setCweID(cweId);
		
		final GetQueryDescriptionResponse response = send(request, GetQueryDescriptionResponse.class, ACTION,  (r) -> r.getGetQueryDescriptionResult());
		return response.getGetQueryDescriptionResult().getQueryDescription();
	}

	private <T,R> T send(R request, Class<T> clazz, String soapAction, BasicResponse<T> responseParser) {
		boolean success = false;
		String errorMessage = null;
		final Stopwatch timer = Stopwatch.createStarted();
		try {
			final Object response = getWebServiceTemplate().marshalSendAndReceive(request,
					new SoapActionCallback(soapAction));
			final T object = clazz.cast(response);
			if (responseParser != null) {
				final CxWSBasicRepsonse basicResponse = responseParser.getBasicResponse(object);
				success = basicResponse.isIsSuccesfull(); 
				errorMessage = basicResponse.getErrorMessage();
			} else {
				success = true;
			}
			if (success) return object;

			final String msg = String.format("API call failed: action=%s; message=%s", 
				soapAction, errorMessage);
			throw new RuntimeException(msg);
		} finally {
			log.debug("source=CxPortal; action={}; success={}; executionTime={}ms", 
					soapAction, success, timer.elapsed(TimeUnit.MILLISECONDS));
		}
	}

}
