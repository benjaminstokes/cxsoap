package com.checkmarx.api.demo;

import java.util.List;

import com.checkmarx.api.cxportal.CxWSCrudEnum;
import com.checkmarx.api.cxportal.CxWSEnableCRUDAction;
import com.checkmarx.api.cxportal.CxWSItemAndCRUD;
import com.checkmarx.api.cxportal.CxWSRoleWithUserPrivileges;
import com.checkmarx.api.cxportal.UserData;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;

public class CxUser {
	
	private long id;
	private String username;
	private String email;
	private String roleName;
	private boolean auditor;
	private boolean applyNotExploitable;
	private boolean deleteProjectScans;
	private boolean updateResultSeverity;
	private UserData userData;
	private CxWSRoleWithUserPrivileges role;
	
	public CxUser(UserData user) {
		this.userData = user;
	}

	public long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getEmail() {
		return email;
	}

	public String getRoleName() {
		return roleName;
	}

	public boolean isAuditor() {
		return auditor;
	}

	public boolean isApplyNotExploitable() {
		return applyNotExploitable;
	}

	public boolean isDeleteProjectScans() {
		return deleteProjectScans;
	}

	public boolean isUpdateResultSeverity() {
		return updateResultSeverity;
	}

	public CxWSRoleWithUserPrivileges getRole() {
		return role;
	}
	
	public List<CxWSItemAndCRUD> getItemsAndCrud() {
		return role.getItemsCRUD().getCxWSItemAndCRUD();
	}

	public UserData getUserData() {
		return userData;
	}

	private boolean getPermission(CxWSItemAndCRUD item, CxWSCrudEnum crud) {
		final List<CxWSEnableCRUDAction> actions = item.getCRUDActionList().getCxWSEnableCRUDAction();
		return actions.stream().anyMatch(s -> s.getType().equals(crud) && s.isEnable());
	}
	
	public static CxUser from(UserData user) {
		final CxUser cxUser = new CxUser(user);
		cxUser.id = user.getID();
		cxUser.username = user.getUserName();
		cxUser.email = user.getEmail();
		
		final CxWSRoleWithUserPrivileges role = (CxWSRoleWithUserPrivileges) user.getRoleData();
		cxUser.role = role;
		cxUser.roleName = role.getName();
		
		role.getItemsCRUD().getCxWSItemAndCRUD().forEach( (item) -> {
			switch (item.getType()) {
				case AUDIT_USER :
					cxUser.auditor = cxUser.getPermission(item, CxWSCrudEnum.RUN);
					break;
				
				case RESULT_SEVERITY : 
					cxUser.updateResultSeverity = cxUser.getPermission(item, CxWSCrudEnum.UPDATE);
					break;
	
				case RESULT_STATUS : 
					cxUser.applyNotExploitable = cxUser.getPermission(item, CxWSCrudEnum.UPDATE);
					break;

				case SCAN : 
					cxUser.deleteProjectScans = cxUser.getPermission(item, CxWSCrudEnum.DELETE);
					break;

				default:
					break;
			}
		
		});
		return cxUser;
	}
	
	public static String csvHeader() { 
		return "id,username,email,role,auditor,applyNotExploitable,deleteProjectsScans,updateResultSeverity";
	}
	
	public String toCsv() {
		return String.format("%d,%s,%s,%s,%d,%d,%d,%d", id, username, email, roleName, 
				auditor ? 1 : 0,
				applyNotExploitable ? 1 : 0, 
				deleteProjectScans ? 1 : 0, 
				updateResultSeverity ? 1 : 0);
	}

	public String toString(boolean includePermissions) {
		final ToStringHelper helper = MoreObjects.toStringHelper(this)
				.add("id", id)
				.add("username", username)
				.add("email", email)
				.add("role", roleName);
		if (includePermissions) {
			helper.add("isAuditor", auditor)
			.add("applyNotExploitable", applyNotExploitable)
			.add("deleteProjectScans", deleteProjectScans)
			.add("updateResultSeverity", updateResultSeverity);
		}
		return helper.toString();
	}

	@Override
	public String toString() {
		return toString(false);
	}

}