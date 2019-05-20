package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlGroupRoleAuth extends Model {

    @ManyToOne
    private MlsqlGroupRole mlsqlGroupRole;

    @ManyToOne
    @JoinColumn(name = "mlsql_table_id")
    private MlsqlTable mlsqlTable;

    public Association mlsqlGroupRole() {
        throw new AutoGeneration();
    }

    public MlsqlTable getMlsqlTable() {
        return mlsqlTable;
    }

    private String operateType;

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }
}
