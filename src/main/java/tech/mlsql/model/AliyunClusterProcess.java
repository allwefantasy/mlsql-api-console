package tech.mlsql.model;

import net.csdn.common.collections.WowCollections;
import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

/**
 * 2019-01-07 WilliamZhu(allwefantasy@gmail.com)
 */
public class AliyunClusterProcess extends Model {

    @ManyToOne
    private MlsqlUser mlsqlUser;

    public Association mlsqlUser() {
        throw new AutoGeneration();
    }

    static int FAIL = 1;
    static int SUCCESS = 2;
    static int PROCESSING = 3;

    private Integer id;
    private Integer status;
    private String reason;
    private String startTime;
    private String endTime;

    public Integer getId() {
        return id;
    }

    public static AliyunClusterProcess newItem(MlsqlUser user) {
        AliyunClusterProcess process = create(WowCollections.map(
                "status", PROCESSING,
                "startTime", System.currentTimeMillis() + ""));
        process.mlsqlUser().set(user);
        process.save();
        return process;
    }

    public static AliyunClusterProcess markSuccues(Integer id, String reason) {
        return _mark(id, SUCCESS, reason);
    }

    public static AliyunClusterProcess _mark(Integer id, Integer status, String reason) {
        AliyunClusterProcess item = AliyunClusterProcess.findById(id);
        item.setStatus(status);
        item.setReason(reason);
        item.setEndTime(System.currentTimeMillis() + "");
        item.save();
        return item;
    }

    public static AliyunClusterProcess markFail(Integer id, String reason) {
        return _mark(id, FAIL, reason);
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
