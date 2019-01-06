package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.OneToMany;
import java.util.List;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

public class MlsqlUser extends Model {
    @OneToMany
    private List<ScriptUserRw> scriptUserRws = list();

    @OneToMany
    private List<AccessToken> accessTokens = list();

    public static MlsqlUser createUser(String name, String password, String token) {
        MlsqlUser user = create(map("name", name, "password", password));
        AccessToken accessToken = AccessToken.create(map("name", token, "createAt", System.currentTimeMillis()));
        user.accessTokens.add(accessToken);
        user.save();
        return findByName(name);
    }

    public List<ScriptUserRw> listScriptFiles() {
        return scriptUserRws().where(map("readable", ScriptUserRw.READ, "isDelete", ScriptUserRw.UnDelete)).fetch();
    }

    public Association scriptUserRws() {
        throw new AutoGeneration();
    }

    public Association accessTokens() {
        throw new AutoGeneration();
    }

    public static MlsqlUser findByName(String name) {
        return where(map("name", name)).singleFetch();
    }

    private int id;
    private String name;
    private String password;

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
}
