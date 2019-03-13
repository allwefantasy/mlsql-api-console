package tech.mlsql.service;

import net.csdn.jpa.model.Model;
import tech.mlsql.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

/**
 * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class TeamRoleService {
    public static String createTeam(MlsqlUser user, String teamName) {
        if (MlsqlGroup.where(map("name", teamName)).fetch().size() > 0) {
            return ReturnCode.TEAM_EXISTS;
        }
        MlsqlGroup group = MlsqlGroup.create(map("name", teamName));
        connectGroupAndUser(user, group, MlsqlGroupUser.Status.owner);
        return ReturnCode.SUCCESS;
    }

    private static String connectGroupAndUser(MlsqlUser user, MlsqlGroup team, int status) {

        MlsqlGroupUser relation = MlsqlGroupUser.create(map("status", status));
        relation.mlsqlGroup().set(team);
        relation.mlsqlUser().set(user);
        relation.save();
        return ReturnCode.SUCCESS;
    }

    public static boolean checkTeamNameValid(String teamName) {
        return MlsqlGroup.where(map("name", teamName)).fetch().size() == 0;
    }

    public static List<MlsqlGroup> teams(MlsqlUser user, int status) {
        List<MlsqlGroupUser> groupUsers = user.mlsqlGroupUsers().where(map("status", status)).fetch();
        List<MlsqlGroup> groups = new ArrayList<>();
        for (MlsqlGroupUser groupUser : groupUsers) {
            groups.add((MlsqlGroup) groupUser.mlsqlGroup().fetch().get(0));
        }
        return groups;
    }

    public static List<MlsqlGroup> teamsIn(MlsqlUser user) {
        List<MlsqlGroupUser> groupUsers = user.mlsqlGroupUsers().fetch();
        List<MlsqlGroup> groups = new ArrayList<>();
        for (MlsqlGroupUser groupUser : groupUsers) {
            Integer status = groupUser.attr("status", Integer.class);
            if (status == MlsqlGroupUser.Status.confirmed || status == MlsqlGroupUser.Status.owner) {
                groups.add((MlsqlGroup) groupUser.mlsqlGroup().fetch().get(0));
            }
        }
        return groups;
    }

    public static List<MlsqlGroupUser> members(String teamName) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        List<MlsqlGroupUser> groupUsers = MlsqlGroupUser.where(map("mlsqlGroup", group)).
                in(
                        "status",
                        list(MlsqlGroupUser.Status.owner, MlsqlGroupUser.Status.confirmed)).
                fetch();
        return groupUsers;

    }

    public static List<MlsqlGroupRole> roles(String teamName) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        List<MlsqlGroupRole> groupUsers = MlsqlGroupRole.where(map("mlsqlGroup", group)).fetch();
        return groupUsers;

    }

    public static void updateMemberStatus(MlsqlUser user, String teamName, int status) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        MlsqlGroupUser relation = MlsqlGroupUser.where(map("mlsqlGroup", group, "mlsqlUser", user)).singleFetch();
        relation.attr("status", status);
        relation.save();
    }

    public static void removeMember(String teamName, String userName) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        MlsqlUser user = MlsqlUser.findByName(userName);
        MlsqlGroupUser relation = MlsqlGroupUser.where(map("mlsqlGroup", group, "mlsqlUser", user)).singleFetch();
        relation.delete();
    }

    public static void removeRole(String teamName, String roleName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlGroupRole role = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
        role.delete();
    }

    // remove table
    public static void removeTable(String teamName, Integer tableId) {
        Model.nativeSqlClient().execute("delete from mlsql_group_table where mlsql_table_id=?", tableId);
        Model.nativeSqlClient().execute("delete from mlsql_table where id=?", tableId);
    }


    public static String addRoles(String teamName, List<String> roleNames) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        for (String roleName : roleNames) {
            if (!MlsqlGroupRole.exists(roleName, group)) {
                MlsqlGroupRole role = MlsqlGroupRole.create(map("name", roleName));
                role.mlsqlGroup().set(group);
                role.save();
            }
        }
        return ReturnCode.SUCCESS;
    }

    public static String addMember(String teamName, List<String> userNames) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        List<MlsqlUser> users = MlsqlUser.in("name", userNames).fetch();
        if (users.size() != userNames.size()) {
            return ReturnCode.USER_NOT_EXISTS;
        }
        for (MlsqlUser user : users) {
            List<Map> groupUsers = Model.nativeSqlClient().query(
                    "select * from mlsql_group_user " +
                            "where mlsql_group_id=? and mlsql_user_id=?", group.id(), user.id());
            if (groupUsers.size() == 0) {
                connectGroupAndUser(user, group, MlsqlGroupUser.Status.invited);
            }

            if (groupUsers.size() == 1) {
                int status = (Integer) groupUsers.get(0).get("status");
                if (status == MlsqlGroupUser.Status.refused) {
                    Model.nativeSqlClient().execute(
                            "update mlsql_group_user set status=?" +
                                    "where mlsql_group_id=? and mlsql_user_id=?",
                            MlsqlGroupUser.Status.invited, group.id(), user.id());
                }
                // other situations just ignore
            }

        }
        return ReturnCode.SUCCESS;


    }

    public static String addTableForTeam(String teamName, Map<String, String> params) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlTable table = MlsqlTable.create(params);
        table.save();
        MlsqlGroupTable groupTable = MlsqlGroupTable.create(map());
        groupTable.mlsqlGroup().set(group);
        groupTable.mlsqlTable().set(table);
        groupTable.save();
        return ReturnCode.SUCCESS;

    }

    public static String addTableForRole(String teamName, String roleName, List<Integer> tableIds, List<String> operateTypes) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        for (Integer tableId : tableIds) {
            MlsqlTable table = MlsqlTable.find(tableId);
            MlsqlGroupRole groupRole = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
            for (String operateType : operateTypes) {
                Map<String, Object> items = map(
                        "mlsqlTable", table,
                        "mlsqlGroupRole", groupRole
                        , "operateType", operateType
                );
                if (MlsqlGroupRoleAuth.where(items).fetch().size() == 0) {
                    MlsqlGroupRoleAuth roleAuth = MlsqlGroupRoleAuth.create(items);
                    roleAuth.save();
                }

            }

        }

        return ReturnCode.SUCCESS;

    }

    public static String addMemberForRole(String teamName, String roleName, String userName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlUser user = MlsqlUser.findByName(userName);
        MlsqlGroupRole groupRole = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
        Map<String, Object> conditions = map("mlsqlGroupRole", groupRole, "mlsqlUser", user);
        if (MlsqlRoleMember.where(conditions).fetch().size() == 0) {
            MlsqlRoleMember member = MlsqlRoleMember.create(conditions);
            member.save();
        }
        return ReturnCode.SUCCESS;

    }

    public static List<MlsqlRoleMember> roleMembers(String teamName, String roleName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlGroupRole groupRole = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
        Map<String, Object> conditions = map("mlsqlGroupRole", groupRole);
        return MlsqlRoleMember.where(conditions).fetch();

    }

    public static String removeRoleMember(String teamName, String roleName, String userName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlGroupRole groupRole = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
        MlsqlUser user = MlsqlUser.findByName(userName);
        List<MlsqlRoleMember> members = MlsqlRoleMember.where(map("mlsqlUser", user, "mlsqlGroupRole", groupRole)).fetch();

        for (MlsqlRoleMember member : members) {
            member.delete();
        }

        return ReturnCode.SUCCESS;
    }

    public static String removeRoleTable(String teamName, String roleName, Integer tableId) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlGroupRole groupRole = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
        MlsqlGroupRoleAuth.nativeSqlClient().execute("delete mlsql_group_role_auth where mlsql_table_id=? and mlsql_group_role_id=?", tableId, groupRole.id());
        return ReturnCode.SUCCESS;
    }

    public static List<MlsqlGroupRoleAuth> roleTables(String teamName, String roleName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        MlsqlGroupRole groupRole = MlsqlGroupRole.where(map("mlsqlGroup", group, "name", roleName)).singleFetch();
        List<MlsqlGroupRoleAuth> mlsqlGroupRoleAuths = MlsqlGroupRoleAuth.where(map("mlsqlGroupRole", groupRole)).fetch();
        return mlsqlGroupRoleAuths;
    }

    public static List<MlsqlBackendProxy> backends(String teamName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        return MlsqlBackendProxy.where(map("mlsqlGroup", group)).fetch();
    }

    public static List<MlsqlGroupTable> fetchTables(String teamName) {
        MlsqlGroup group = MlsqlGroup.fetchByName(teamName);
        return MlsqlGroupTable.where(map("mlsqlGroup", group)).joins("mlsqlTable").fetch();
    }

    public static class ReturnCode {
        public static String TEAM_EXISTS = "Team exists";
        public static String USER_NOT_EXISTS = "User not exists";
        public static String SUCCESS = "success";
    }

}
