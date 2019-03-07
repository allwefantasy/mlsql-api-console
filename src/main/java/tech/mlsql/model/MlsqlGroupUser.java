package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

/**
 * 2019-03-06 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlGroupUser extends Model {
    @ManyToOne
    private MlsqlUser mlsqlUser;

    @ManyToOne
    private MlsqlGroup mlsqlGroup;

    public Association mlsqlUser() {
        throw new AutoGeneration();
    }

    public Association mlsqlGroup() {
        throw new AutoGeneration();
    }
}
