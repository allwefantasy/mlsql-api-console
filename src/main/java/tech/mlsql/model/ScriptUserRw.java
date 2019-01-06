package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.association.Association;
import net.csdn.jpa.model.Model;

import javax.persistence.ManyToOne;

public class ScriptUserRw extends Model {

    @ManyToOne
    private MlsqlUser mlsqlUser;
    @ManyToOne
    private ScriptFile scriptFile;

    public Association mlsqlUser() {
        throw new AutoGeneration();
    }

    public Association scriptFile() {
        throw new AutoGeneration();
    }


    private Integer id;
    private Integer readable;
    private Integer writable;
    private Integer isOwner;
    private Integer isDelete = 2;

    static Integer Delete = 1;
    static Integer UnDelete = 2;

    static Integer READ = 1;
    static Integer UNREAD = 2;

    static Integer WRITE = 1;
    static Integer UNWRITE = 2;

    static Integer OWNER = 1;
    static Integer UNOWNER = 2;


    public boolean isOwner() {
        if (isOwner == OWNER) return true;
        if (isOwner == UNOWNER) return false;
        throw new RuntimeException("isOwner:" + isOwner + " is not supported");
    }

    public boolean readable() {
        if (readable == READ) return true;
        if (readable == UNREAD) return false;
        throw new RuntimeException("readable:" + readable + " is not supported");
    }

    public boolean writable() {
        if (writable == WRITE) return true;
        if (writable == UNWRITE) return false;
        throw new RuntimeException("writable:" + writable + " is not supported");
    }

    public boolean isDelete() {
        if (isDelete == Delete) return true;
        if (isDelete == UnDelete) return false;
        throw new RuntimeException("isDelete:" + isDelete + " is not supported");
    }

    public Integer getIsDelete() {
        return isDelete;
    }

    public void setIsDelete(Integer isDelete) {
        this.isDelete = isDelete;
    }

    public int getReadable() {
        return readable;
    }

    public void setReadable(int readable) {
        this.readable = readable;
    }

    public int getWritable() {
        return writable;
    }

    public void setWritable(int writable) {
        this.writable = writable;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
