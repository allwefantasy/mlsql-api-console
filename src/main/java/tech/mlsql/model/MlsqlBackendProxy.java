package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

import static net.csdn.common.collections.WowCollections.map;

/**
 * 2019-03-13 WilliamZhu(allwefantasy@gmail.com)
 */
public class MlsqlBackendProxy extends Model {
    @ManyToOne
    private MlsqlGroup mlsqlGroup;

    public Association mlsqlGroup() {
        throw new AutoGeneration();
    }

    private String backendName;

    public String getBackendName() {
        return backendName;
    }

    public void setBackendName(String backendName) {
        this.backendName = backendName;
    }

    public static MlsqlBackendProxy build(String teamName, String backendName) {
        MlsqlGroup mlsqlGroup = MlsqlGroup.fetchByName(teamName);
        MlsqlBackendProxy mlsqlBackendProxy = MlsqlBackendProxy.create(map("mlsqlGroup", mlsqlGroup, "backendName", backendName));
        mlsqlBackendProxy.save();
        return mlsqlBackendProxy;
    }

    public static MlsqlBackendProxy findByName(String backendName) {
        MlsqlBackendProxy mlsqlBackendProxy = MlsqlBackendProxy.where(map("backendName", backendName)).singleFetch();
        return mlsqlBackendProxy;
    }
}
