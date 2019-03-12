package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.util.List;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

/**
 * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlGroupRole extends Model {
    @ManyToOne
    private MlsqlGroup mlsqlGroup;

    @OneToMany
    private List<MlsqlTable> mlsqlTables = list();

    @OneToMany
    private List<MlsqlRoleMember> mlsqlRoleMembers = list();

    public Association mlsqlGroup() {
        throw new AutoGeneration();
    }

    public Association mlsqlTables() {
        throw new AutoGeneration();
    }

    public Association mlsqlRoleMembers() {
        throw new AutoGeneration();
    }


    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static boolean exists(String name, MlsqlGroup group) {
        return MlsqlGroupRole.where(map("mlsqlGroup", group, "name", name)).fetch().size() > 0;
    }
}
