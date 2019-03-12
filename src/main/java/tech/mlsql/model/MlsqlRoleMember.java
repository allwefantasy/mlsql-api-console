package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

/**
 * 2019-03-12 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlRoleMember extends Model {

    @ManyToOne
    private MlsqlUser mlsqlUser;

    @ManyToOne
    private MlsqlGroupRole mlsqlGroupRole;

    public Association mlsqlUser() {
        throw new AutoGeneration();
    }

    public Association mlsqlGroupRole() {
        throw new AutoGeneration();
    }
}
