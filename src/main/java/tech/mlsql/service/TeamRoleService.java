package tech.mlsql.service;

import net.csdn.jpa.model.Model;
import tech.mlsql.model.MlsqlGroup;
import tech.mlsql.model.MlsqlGroupUser;
import tech.mlsql.model.MlsqlUser;

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

    public static List<MlsqlGroupUser> members(String teamName) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        List<MlsqlGroupUser> groupUsers = MlsqlGroupUser.where(map("mlsqlGroup", group)).
                in(
                        "status",
                        list(MlsqlGroupUser.Status.owner, MlsqlGroupUser.Status.confirmed)).
                fetch();
        return groupUsers;

    }

    public static void updateMemberStatus(MlsqlUser user, String teamName, int status) {
        MlsqlGroup group = MlsqlGroup.where(map("name", teamName)).singleFetch();
        MlsqlGroupUser relation = MlsqlGroupUser.where(map("mlsqlGroup", group, "mlsqlUser", user)).singleFetch();
        relation.attr("status", status);
        relation.save();
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

    public static class ReturnCode {
        public static String TEAM_EXISTS = "Team exists";
        public static String USER_NOT_EXISTS = "User not exists";
        public static String SUCCESS = "success";
    }


}
