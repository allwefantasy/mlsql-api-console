package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.OneToMany;
import java.util.List;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

/**
 * 2019-03-06 WilliamZhu(allwefantasy@gmail.com)
 * Also called Team
 */
public class MlsqlGroup extends Model {
    @OneToMany
    private List<MlsqlGroupUser> mlsqlGroupUsers = list();

    @OneToMany
    private List<MlsqlGroupRole> mlsqlGroupRoles = list();

    @OneToMany
    private List<MlsqlGroupTable> mlsqlGroupTables = list();

    public Association mlsqlGroupUsers() {
        throw new AutoGeneration();
    }

    public Association mlsqlGroupRoles() {
        throw new AutoGeneration();
    }

    public Association mlsqlGroupTables() {
        throw new AutoGeneration();
    }

    public static MlsqlGroup fetchByName(String name) {
        MlsqlGroup group = MlsqlGroup.where(map("name", name)).singleFetch();
        return group;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
