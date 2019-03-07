package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.OneToMany;
import java.util.List;

import static net.csdn.common.collections.WowCollections.list;

/**
 * 2019-03-06 WilliamZhu(allwefantasy@gmail.com)
 * Also called Team
 */
public class MlsqlGroup extends Model {
    @OneToMany
    private List<MlsqlGroupUser> mlsqlGroupUsers = list();

    @OneToMany
    private List<MlsqlGroupRole> mlsqlGroupRoles = list();

    public Association mlsqlGroupUsers() {
        throw new AutoGeneration();
    }

    public Association mlsqlGroupRoles() {
        throw new AutoGeneration();
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
