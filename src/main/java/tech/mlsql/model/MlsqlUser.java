package tech.mlsql.model;

import net.csdn.common.collections.WowCollections;
import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.JPQL;
import net.csdn.jpa.model.Model;
import tech.mlsql.service.TeamRoleService;

import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

public class MlsqlUser extends Model {
    @OneToMany
    private List<ScriptUserRw> scriptUserRws = list();

    @OneToMany
    private List<AccessToken> accessTokens = list();

    @OneToMany
    private List<AliyunClusterProcess> aliyunClusterProcesses = list();

    @OneToMany
    private List<MlsqlGroupUser> mlsqlGroupUsers = list();

    @OneToMany
    private List<MlsqlRoleMember> mlsqlRoleMembers = list();

    public static MlsqlUser createUser(String name, String password, String token) {
        MlsqlUser user = MlsqlUser.create(map("name", name, "password", password));
        AccessToken accessToken = AccessToken.create(map("name", token, "createAt", System.currentTimeMillis()));
        user.accessTokens.add(accessToken);

        Map<Object, Object> items = Model.nativeSqlClient().single_query("select count(*) as c from mlsql_user");
        Long count = (Long) (items.get("c"));
        if (count == 0) {
            user.setRole(ROLE_ADMIN);
        } else {
            user.setRole(ROLE_DEVELOPER);
        }
        user.save();
        return findByName(name);
    }

    public List<ScriptUserRw> listScriptFiles() {
        return scriptUserRws().where(map("readable", ScriptUserRw.READ, "isDelete", ScriptUserRw.UnDelete)).fetch();
    }

    public Association scriptUserRws() {
        throw new AutoGeneration();
    }

    public Association mlsqlRoleMembers() {
        throw new AutoGeneration();
    }

    public static List<MlsqlUser> items(String fields) {
        JPQL query = MlsqlUser.where(WowCollections.map("role", ROLE_DEVELOPER));
        if (fields == null) {
            return query.fetch();
        }
        return query.select(fields).fetch();
    }

    public Association aliyunClusterProcesses() {
        throw new AutoGeneration();
    }

    public Association accessTokens() {
        throw new AutoGeneration();
    }

    public Association mlsqlGroupUsers() {
        throw new AutoGeneration();
    }

    public static MlsqlUser findByName(String name) {
        return where(map("name", name)).singleFetch();
    }

    public static String ROLE_ADMIN = "admin";
    public static String ROLE_DEVELOPER = "developer";

    private int id;
    private String name;
    private String password;
    private String backendTags;
    private String role;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static String STATUS_NORMAL = "normal"; // null or normal means ok
    public static String STATUS_PAUSE = "pause"; // shutdown all write/update function
    public static String STATUS_LOCK = "lock";   // disable login

    public static String SCHEDULER_TAG_TYPE = "scheduler";
    public static String NORMAL_TAG_TYPE = "normal";

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setBackendTags(String backendTags) {
        this.backendTags = backendTags;
    }

    public String getBackendTags() {
        return backendTags;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void createDefaultTeamAndRole() {
        String uuid = UUID.randomUUID().toString();
        TeamRoleService.createTeam(this, uuid);
        List<String> roles = new ArrayList<>();
        roles.add("admin");
        TeamRoleService.addRoles(uuid, roles);
        TeamRoleService.addMemberForRole(uuid, "admin", this.getName());
    }
}
