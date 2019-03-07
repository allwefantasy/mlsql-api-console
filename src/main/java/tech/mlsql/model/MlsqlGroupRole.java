package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

/**
 * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlGroupRole extends Model {
    @ManyToOne
    private MlsqlGroup mlsqlGroup;

    public Association mlsqlGroup() {
        throw new AutoGeneration();
    }

    
}
