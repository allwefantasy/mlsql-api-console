package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

import static net.csdn.common.collections.WowCollections.map;

public class AccessToken extends Model {

    public static AccessToken token(String name) {
        AccessToken token = AccessToken.where(map("name", name)).singleFetch();
        if (token != null) {
            if (System.currentTimeMillis() - token.createAt > 1000 * 60 * 60 * 24) {
                nativeSqlClient().execute("delete from access_token where id=?", token.id);
                return null;
            } else {
                nativeSqlClient().execute("update access_token set create_at=? where id=?", System.currentTimeMillis(), token.id);
            }

        }
        return token;
    }

    public static AccessToken loginToken(MlsqlUser user, String name) {
        AccessToken token = new AccessToken();
        token.setName(name);
        token.setCreateAt(System.currentTimeMillis());
        token.mlsqlUser().set(user);
        token.save();
        return token;
    }

    @ManyToOne
    private MlsqlUser mlsqlUser;

    public Association mlsqlUser() {
        throw new AutoGeneration();
    }


    private int id;
    private String name;
    private long createAt;

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

    public long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(long createAt) {
        this.createAt = createAt;
    }
}
