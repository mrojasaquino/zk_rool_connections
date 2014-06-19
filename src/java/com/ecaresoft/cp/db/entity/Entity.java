package com.ecaresoft.cp.db.entity;

import com.ecaresoft.cp.db.DB;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author odelarosa
 */
public class Entity {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean save() {
        boolean retValue = true;
        try {
            Map<String, Object> map = PropertyUtils.describe(this);

            map.keySet().remove("class");

            String[] keys = map.keySet().toArray(new String[]{});

            String query = getSql(map);

            Connection conn = null;
            PreparedStatement pstmt = null;

            try {
                conn = DB.getInstance().getConnection();

                pstmt = conn.prepareStatement(query);

                for (int i = 0; i < keys.length; i++) {
                    String key = keys[i];

                    if ("id".equals(key)) {
                        pstmt.setObject(i + 1, id);
                    } else {
                        pstmt.setObject(i + 1, map.get(key));
                    }
                }

                if (map.get("id") != null) {
                    pstmt.setObject(keys.length+1, id);
                }

                pstmt.executeUpdate();

            } catch (SQLException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

                if (pstmt != null) {
                    try {
                        pstmt.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

                if (conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException ex) {
                        Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
        }

        return retValue;
    }

    private String getSql(Map<String, Object> map) {
        StringBuilder str = new StringBuilder();

        map.keySet().remove("class");

        if (StringUtils.isBlank(id)) {
            str.append("insert into ").append(getClass().getSimpleName()).append(" ( ");

            str.append(StringUtils.join(map.keySet(), ","));

            str.append(" ) values ( ");

            for (int i = 0; i < map.keySet().size(); i++) {
                str.append("?");

                if (i + 1 != map.keySet().size()) {
                    str.append(",");
                }
            }

            str.append(" )");

            setId(DB.getRandomId());
        } else {
            str.append("update ").append(getClass().getSimpleName()).append(" set ");

            String[] arr = map.keySet().toArray(new String[]{});

            for (int i = 0; i < arr.length; i++) {
                str.append(arr[i]).append(" = ?");

                if (i + 1 != arr.length) {
                    str.append(", ");
                }
            }

            str.append(" where id = ? ");
        }

        return str.toString();
    }
}
