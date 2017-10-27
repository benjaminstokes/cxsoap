package com.checkmarx.api.demo;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.checkmarx.api.cxportal.CxWSCrudEnum;
import com.checkmarx.api.cxportal.CxWSItemAndCRUD;
import com.checkmarx.api.cxportal.CxWSItemTypeEnum;
import com.checkmarx.api.cxportal.CxWSRoleWithUserPrivileges;
import com.checkmarx.api.cxportal.Role;
import com.checkmarx.api.cxportal.UserData;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public class CxPortalUtils {
	
	private static final Logger log = LoggerFactory.getLogger(CxPortalUtils.class);
	
	private static final String SCANNER = "Scanner";
	private static final String REVIEWER = "Reviewer";
	
	private final CxPortalClient cxPortal;
	private final String sessionId;

	private CxPortalUtils(CxPortalClient portal, String sessionId) {
		log.debug("CxPortalUtils.ctor()");
		this.cxPortal = portal;
		this.sessionId = sessionId;
	}
	
	public static CxPortalUtils factory(CxPortalClient portal, String host, String user, String password) {
		log.debug("factory(): host={}; user={}", host, user);
		final String sessionId = portal.login(host, user, password);
		if (Strings.isNullOrEmpty(sessionId)) {
			throw new RuntimeException("Cx login failed, check credentials");
		}
		return new CxPortalUtils(portal, sessionId);
	}
	
	public List<CxUser> findAllUsers() {
		log.trace("findAllUsers()");
		
		final List<UserData> allUsers = cxPortal.getAllUsers(sessionId);
		final List<CxUser> users = Lists.newArrayList();
		allUsers.forEach(user -> users.add(CxUser.from(user)));
		return users;
	}
	
	public List<CxUser> findAllScanners() {
		log.trace("findAllScanners()");
		return findUsersWithRoles(SCANNER);
	}
	
	public List<CxUser> findAllReviewers() {
		log.trace("findAllReviewers()");
		return findUsersWithRoles(REVIEWER);
	}
	
	public List<CxUser> findUsersWithSetNotExploitable() {
		log.trace("findUsersWithSetNotExploitable()");
		final List<CxUser> users = findUsersWithRoles(SCANNER, REVIEWER);
		return findUsersWithAction(users, CxWSItemTypeEnum.RESULT_STATUS, CxWSCrudEnum.UPDATE);
	}
	
	public List<CxUser> findUsersWithRoles(String... withRoles) {
		log.trace("findUsersWithRole(): roles={}", Joiner.on(",").join(withRoles));
		
		final List<UserData> allUsers = cxPortal.getAllUsers(sessionId);

		final List<CxUser> matchingUsers = Lists.newArrayList();
		final List<String> roles = Arrays.asList(withRoles);
		allUsers.forEach(user -> {
			final CxUser cxUser = CxUser.from(user);
			final Role role = user.getRoleData();
			boolean match = roles.stream().anyMatch(s -> role.getName().equals(s));
			if (match) {
				matchingUsers.add(cxUser);
			}
		});
		return matchingUsers;
	}
	
	public List<CxUser> findUsersWithAction(List<CxUser> users, CxWSItemTypeEnum type, CxWSCrudEnum withAction) {
		log.trace("findUsersWithAction() : type={}, action={}", type, withAction);
		
		final List<CxUser> matchingUsers = Lists.newArrayList();
		users.forEach(user -> {
			final CxWSRoleWithUserPrivileges role = user.getRole();
			final List<CxWSItemAndCRUD> items = role.getItemsCRUD().getCxWSItemAndCRUD();
			items.forEach(item -> {
				if (item.getType().equals(type) ) {
					item.getCRUDActionList().getCxWSEnableCRUDAction().forEach(action -> {
						if (action.getType().equals(withAction) && action.isEnable()) {
							matchingUsers.add(user);
						}
					});
				}
			});
		});
		return matchingUsers;
	}
	
	public List<CxUser> findUsersWithoutAction(List<CxUser> users, CxWSItemTypeEnum type, CxWSCrudEnum withAction) {
		log.trace("findUsersWithoutAction(): type={}, action={}", type, withAction);
		
		final List<CxUser> matchingUsers = Lists.newArrayList();
		users.forEach(user -> {
			final CxWSRoleWithUserPrivileges role = user.getRole();
			final List<CxWSItemAndCRUD> items = role.getItemsCRUD().getCxWSItemAndCRUD();
			items.forEach(item -> {
				if (item.getType().equals(type) ) {
					item.getCRUDActionList().getCxWSEnableCRUDAction().forEach(action -> {
						if (action.getType().equals(withAction) && !action.isEnable()) {
							matchingUsers.add(user);
						}
					});
				}
			});
		});
		return matchingUsers;
	}
	
	public static StringBuilder printUserData(CxUser user, boolean includeItems) {
		final StringBuilder sb = new StringBuilder();

		sb.append(user);
		if (includeItems) {
			sb.append("\n");
			user.getItemsAndCrud().forEach( (item) -> {
				sb.append(String.format("\troleType=%s :\n\t\t", item.getType()));
				item.getCRUDActionList().getCxWSEnableCRUDAction().forEach(action -> {
					sb.append(String.format("%s=%s; ", action.getType(), action.isEnable()));
				});
				sb.append("\n");
			});
		}
		return sb;
	}
	
	public static StringBuilder printUserData(UserData user, boolean includeItems) {
		return printUserData(CxUser.from(user), includeItems);
	}
	
}
