package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.OneToMany;
import java.util.List;

import static net.csdn.common.collections.WowCollections.list;

/**
 * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlTable extends Model {
    private String name;
    private String tableType;
    private String db;
    private String sourceType;

    @OneToMany
    private List<MlsqlGroupTable> mlsqlGroupTables = list();
    @OneToMany
    private List<MlsqlGroupRoleAuth> mlsqlGroupRoleAuths = list();

    public Association mlsqlGroupTables() {
        throw new AutoGeneration();
    }
    public Association mlsqlGroupRoleAuths() {
        throw new AutoGeneration();
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

}
