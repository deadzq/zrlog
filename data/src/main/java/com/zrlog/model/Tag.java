package com.zrlog.model;

import com.jfinal.plugin.activerecord.Model;
import com.zrlog.common.request.PageableRequest;

import java.util.*;

/**
 * 统计文章中标签出现的次数，方便展示。文章的数据发生变化后，会自动更新记录，无需额外程序控制，对应数据库的tag表
 */
public class Tag extends Model<Tag> {
    public static final Tag dao = new Tag();
    public static final String TABLE_NAME = "tag";

    public List<Tag> find() {
        return find("select tagId as id,text,count from " + TABLE_NAME);
    }

    private Set<String> strToSet(String str) {
        Set<String> tags = new HashSet<>();
        for (String tag : str.split(",")) {
            if (tag.trim().length() > 0) {
                tags.add(tag.trim());
            }
        }
        return tags;
    }

    public boolean update(String nowTagStr, String oldTagStr) {
        String copyNewTagStr = nowTagStr;
        String copyOldTagStr = oldTagStr;
        if (copyNewTagStr == null && oldTagStr == null) {
            return true;
        }
        if (copyNewTagStr == null) {
            copyNewTagStr = "";
        }
        if (copyOldTagStr == null) {
            copyOldTagStr = "";
        }
        if (copyNewTagStr.equals(copyOldTagStr)) {
            return true;
        }
        String[] oldArr = copyOldTagStr.split(",");
        String[] nowArr = copyNewTagStr.split(",");
        Set<String> addSet = new HashSet<>();
        Set<String> deleteSet = new HashSet<>();
        for (String str : nowArr) {
            addSet.add(str.trim());
        }
        for (String str : oldArr) {
            if (!addSet.contains(str)) {
                deleteSet.add(str.trim());
            } else {
                addSet.remove(str);
            }
        }
        insertTag(addSet);
        deleteTag(deleteSet);

        return true;
    }

    public boolean insertTag(String now) {
        return insertTag(strToSet(now));
    }

    private boolean insertTag(Set<String> now) {
        for (String add : now) {
            Tag t = dao.findFirst("select * from " + TABLE_NAME + " where text=?", add);
            if (t == null) {
                new Tag().set("text", add).set("count", 1).save();
            } else {
                t.set("count", t.getInt("count") + 1).update();
            }
        }
        return true;
    }

    public boolean deleteTag(String old) {
        return deleteTag(strToSet(old));
    }

    private boolean deleteTag(Set<String> old) {
        for (String del : old) {
            Tag t = dao.findFirst("select * from " + TABLE_NAME + " where text=?", del);
            if (t != null) {
                if (t.getInt("count") > 1) {
                    t.set("count", t.getInt("count") - 1).update();
                } else {
                    t.delete();
                }
            }
        }
        return true;
    }

    public Map<String, Object> find(PageableRequest page) {
        Map<String, Object> data = new HashMap<>();
        data.put("rows", find("select tagId as id,text,count from " + TABLE_NAME + " limit ?,?", page.getOffset(), page.getRows()));
        ModelUtil.fillPageData(this, page.getPage(), page.getRows(), "from " + TABLE_NAME + "", data, new Object[0]);
        return data;
    }
}
