package tech.mlsql.service;

import tech.mlsql.model.MlsqlGroupRole;
import tech.mlsql.model.MlsqlGroupRoleAuth;
import tech.mlsql.model.MlsqlRoleMember;
import tech.mlsql.model.MlsqlUser;

import java.util.List;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

/**
 * 2019-03-13 WilliamZhu(allwefantasy@gmail.com)
 */
public class TableAuthService {
    public static List<MlsqlGroupRoleAuth> fetchAuth(String userName) {
        List<MlsqlGroupRoleAuth> roleAuths = list();
        MlsqlUser mlsqlUser = MlsqlUser.findByName(userName);
        List<MlsqlRoleMember> roleMembers = MlsqlRoleMember.where(map("mlsqlUser", mlsqlUser)).fetch();
        if (roleMembers.size() == 0) return list();
        for (MlsqlRoleMember roleMember : roleMembers) {
            MlsqlGroupRole groupRole = roleMember.attr("mlsqlGroupRole", MlsqlGroupRole.class);
            if (groupRole != null) {
                List<MlsqlGroupRoleAuth> temp = MlsqlGroupRoleAuth.where(map("mlsqlGroupRole", groupRole)).fetch();
                roleAuths.addAll(temp);
            }
        }
        return roleAuths;
    }
}
