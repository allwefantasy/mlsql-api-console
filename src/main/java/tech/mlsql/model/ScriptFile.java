package tech.mlsql.model;

import net.csdn.common.exception.AutoGeneration;
import net.csdn.jpa.model.Model;

import javax.persistence.OneToMany;
import java.util.List;

import static net.csdn.common.collections.WowCollections.list;
import static net.csdn.common.collections.WowCollections.map;

public class ScriptFile extends Model {

    @OneToMany
    private List<ScriptUserRw> scriptUserRws = list();

    public ScriptFile parent() {
        return ScriptFile.find(parentId);
    }

    public List<ScriptFile> child() {
        return ScriptFile.where(map("parentId", id)).fetch();
    }

    public static List<ScriptUserRw> scriptUserRws() {
        throw new AutoGeneration();
    }

    private static void markDelete(List<ScriptUserRw> items, MlsqlUser mlsqlUser) {
        items.stream().filter((m) -> {
            MlsqlUser temp_user = (MlsqlUser) m.mlsqlUser().fetch().get(0);
            return temp_user.getId() == mlsqlUser.getId();
        }).forEach((m) -> {
            m.setIsDelete(ScriptUserRw.Delete);
            m.save();
        });
    }

    public static void removeScriptFile(Integer id, MlsqlUser user) {

        ScriptFile sf = ScriptFile.findById(id);
        if (sf != null) {
            if (sf.isDir()) {
                List<ScriptFile> files = ScriptFile.where(map("parentId", sf.id())).fetch();
                for (ScriptFile file : files) {
                    removeScriptFile(file.id(), user);
                }
                markDelete(sf.scriptUserRws, user);

            } else {
                markDelete(sf.scriptUserRws, user);
            }

        }

    }

    public static ScriptFile getItem(Integer id) {
        return ScriptFile.findById(id);
    }


    public static void createScriptFile(MlsqlUser user,
                                        String name,
                                        String content,
                                        boolean isDir, Integer parentId) {
        ScriptFile newfile = new ScriptFile();
        newfile.setName(name);
        newfile.setContent(content);
        newfile.setIsDir(isDir(isDir));
        if (isDir) {
            newfile.setIcon("folder-close");
        } else {
            newfile.setIcon("document");
        }
        if (parentId != -1) {
            newfile.setParentId(parentId);
        }
        newfile.setLabel(name);
        ScriptUserRw sur = ScriptUserRw.create(map(
                "readable", ScriptUserRw.READ,
                "writable", ScriptUserRw.WRITE,
                "isOwner", ScriptUserRw.OWNER
        ));
        sur.mlsqlUser().set(user);
        sur.scriptFile().set(newfile);

        sur.save();
    }

    static int DIR = 1;
    static int FILE = 2;

    public boolean isDir() {
        if (isDir == DIR) return true;
        if (isDir == FILE) return false;
        throw new RuntimeException("isDir:" + isDir + " is not supported");
    }

    public static int isDir(boolean isDir) {
        if (isDir) return DIR;
        else return FILE;
    }

    private Integer id;
    private String name;
    private Integer hasCaret;


    static int Expanded = 1;
    static int NO_Expanded = 2;

    private Integer isExpanded = Expanded;

    public Integer getHasCaret() {
        return hasCaret;
    }

    public void setHasCaret(int hasCaret) {
        this.hasCaret = hasCaret;
    }

    private String icon;
    private String label;
    private int parentId;

    private String content;

    public Integer getIsDir() {
        return isDir;
    }

    public void setIsDir(int isDir) {
        this.isDir = isDir;
    }

    private Integer isDir;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIsExpanded() {
        return isExpanded == Expanded;
    }

    public void setIsExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded ? Expanded : NO_Expanded;
    }
}
