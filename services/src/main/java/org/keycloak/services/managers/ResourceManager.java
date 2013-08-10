package org.keycloak.services.managers;

import org.keycloak.representations.idm.*;
import org.keycloak.representations.idm.ApplicationRepresentation;
import org.keycloak.services.models.RealmModel;
import org.keycloak.services.models.ApplicationModel;
import org.keycloak.services.models.RoleModel;
import org.keycloak.services.models.UserCredentialModel;
import org.keycloak.services.models.UserModel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class ResourceManager {

    protected RealmManager realmManager;

    public ResourceManager(RealmManager realmManager) {
        this.realmManager = realmManager;
    }

    public ApplicationModel createResource(RealmModel realm, RoleModel loginRole, ApplicationRepresentation resourceRep) {
        ApplicationModel resource = realm.addApplication(resourceRep.getName());
        resource.setEnabled(resourceRep.isEnabled());
        resource.setManagementUrl(resourceRep.getAdminUrl());
        resource.setSurrogateAuthRequired(resourceRep.isSurrogateAuthRequired());
        resource.updateResource();

        UserModel resourceUser = resource.getResourceUser();
        if (resourceRep.getCredentials() != null) {
            for (CredentialRepresentation cred : resourceRep.getCredentials()) {
                UserCredentialModel credential = new UserCredentialModel();
                credential.setType(cred.getType());
                credential.setValue(cred.getValue());
                realm.updateCredential(resourceUser, credential);
            }
        }
        realm.grantRole(resourceUser, loginRole);


        if (resourceRep.getRoles() != null) {
            for (RoleRepresentation roleRep : resourceRep.getRoles()) {
                RoleModel role = resource.addRole(roleRep.getName());
                if (roleRep.getDescription() != null) role.setDescription(roleRep.getDescription());
            }
        }
        if (resourceRep.getRoleMappings() != null) {
            for (RoleMappingRepresentation mapping : resourceRep.getRoleMappings()) {
                UserModel user = realm.getUser(mapping.getUsername());
                for (String roleString : mapping.getRoles()) {
                    RoleModel role = resource.getRole(roleString.trim());
                    if (role == null) {
                        role = resource.addRole(roleString.trim());
                    }
                    realm.grantRole(user, role);
                }
            }
        }
        if (resourceRep.getScopeMappings() != null) {
            for (ScopeMappingRepresentation mapping : resourceRep.getScopeMappings()) {
                UserModel user = realm.getUser(mapping.getUsername());
                for (String roleString : mapping.getRoles()) {
                    RoleModel role = resource.getRole(roleString.trim());
                    if (role == null) {
                        role = resource.addRole(roleString.trim());
                    }
                    resource.addScope(user, role.getName());
                }
            }
        }
        if (resourceRep.isUseRealmMappings()) realm.addScope(resource.getResourceUser(), "*");
        return resource;
    }

    public ApplicationModel createResource(RealmModel realm, ApplicationRepresentation resourceRep) {
        RoleModel loginRole = realm.getRole(RealmManager.RESOURCE_ROLE);
        return createResource(realm, loginRole, resourceRep);
    }

    public void updateResource(ApplicationRepresentation rep, ApplicationModel resource) {
        resource.setName(rep.getName());
        resource.setEnabled(rep.isEnabled());
        resource.setManagementUrl(rep.getAdminUrl());
        resource.setSurrogateAuthRequired(rep.isSurrogateAuthRequired());
        resource.updateResource();

    }

    public ApplicationRepresentation toRepresentation(ApplicationModel applicationModel) {
        ApplicationRepresentation rep = new ApplicationRepresentation();
        rep.setId(applicationModel.getId());
        rep.setName(applicationModel.getName());
        rep.setEnabled(applicationModel.isEnabled());
        rep.setAdminUrl(applicationModel.getManagementUrl());
        rep.setSurrogateAuthRequired(applicationModel.isSurrogateAuthRequired());
        return rep;

    }
}
