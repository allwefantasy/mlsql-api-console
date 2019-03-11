package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

/**
 * 2019-03-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlGroupTable extends Model {
    @ManyToOne
    private MlsqlTable mlsqlTable;

    @ManyToOne
    private MlsqlGroup mlsqlGroup;


    public Association mlsqlGroup() {
        throw new AutoGeneration();
    }

    public Association mlsqlTable() {
        throw new AutoGeneration();
    }

}
