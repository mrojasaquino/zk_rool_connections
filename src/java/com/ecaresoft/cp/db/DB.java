package com.ecaresoft.cp.db;

import com.ecaresoft.cp.db.entity.Entity;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConstructorUtils;
import org.apache.commons.beanutils.PropertyUtils;

/**
 *
 * @author odelarosa
 */
public class DB {

    private static DB instance;

    /**
     * Obtiene una instancia de la clase
     *
     * @return Instancia de la clase
     */
    public static DB getInstance() {
        if (instance == null) {
            try {
                Class.forName("org.postgresql.Driver");// carga el driver de la base
                BoneCPConfig config = new BoneCPConfig();// crea una nueva configuración
                config.setJdbcUrl("jdbc:postgresql://192.168.11.123:5432/alumnos");// asigna el url JDBC
                config.setUsername("ecaresoft");// asigna el username
                config.setPassword("ecaresoft");// asigna el password

                config.setPartitionCount(3);
                config.setMaxConnectionsPerPartition(5);

                instance = new DB(); // nueva instancia
                instance.connectionPool = new BoneCP(config);// configuración del pool
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return instance;
    }

    /**
     * Id aleatorio, solo con fines de ejempolo
     *
     * @return Id generado
     */
    public static String getRandomId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Obtiene un objeto de la base de datos por medio de su id
     *
     * @param <T> Extiende de {@link Entity}
     * @param id Id del objecto
     * @param type Tipo del objeto que extiende de {@link Entity}
     * @return Objecto o nulo si no lo encuentra
     */
    public static <T extends Entity> T get(String id, Class<T> type) {
        List<T> list = get(new String[]{"id"}, new Object[]{id}, 1, type);
        return list.size() > 0 ? list.get(0) : null;
    }

    /**
     * Obtiene una lista de objetos de base de datos basada en sus parámetros
     *
     * @param <T> Extiende de {@link Entity}
     * @param properties Propiedades a revisar, puede ser nula
     * @param values Valores a asignar puede ser nula
     * @param type Tipo del objeto que extiende de {@link Entity}
     * @return Listado de objetos de base de datos
     */
    public static <T> List<T> get(String[] properties, Object[] values, Class<T> type) {
        return get(properties, values, -1, type);
    }

    /**
     * Obtiene una lista de todos los objetos de base de datos basada en sus
     * parámetros
     *
     * @param <T> Extiende de {@link Entity}
     * @param type Tipo del objeto que extiende de {@link Entity}
     * @return Listado de objetos de base de datos
     */
    public static <T> List<T> get(Class<T> type) {
        return get(null, null, -1, type);
    }

    /**
     * Obtiene una lista de objetos de base de datos basada en sus parámetros
     *
     * @param <T> Extiende de {@link Entity}
     * @param properties Propiedades a revisar, puede ser nula
     * @param values Valores a asignar puede ser nula
     * @param limit Limite de registros
     * @param type Tipo del objeto que extiende de {@link Entity}
     * @return Listado de objetos de base de datos
     */
    public static <T> List<T> get(String[] properties, Object[] values, int limit, Class<T> type) {
        List<T> list = new ArrayList<T>();

        boolean useParameters = properties != null && values != null && properties.length > 0 && values.length > 0 && properties.length == values.length;

        StringBuilder str = new StringBuilder("select * from ");

        str.append(type.getSimpleName());

        if (useParameters) {
            str.append(" where ");

            for (int i = 0; i < properties.length; i++) {
                str.append(properties[i]).append(" = ? ");

                if (i + 1 != properties.length) {
                    str.append(" and ");
                }
            }
        }

        if (limit > 0) {
            str.append(" limit ").append(limit);
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DB.getInstance().getConnection();

            pstmt = conn.prepareStatement(str.toString());

            if (useParameters) {
                for (int i = 0; i < values.length; i++) {
                    pstmt.setObject(i + 1, values[i]);
                }
            }

            rs = pstmt.executeQuery();

            while (rs.next()) {

                PropertyDescriptor[] descriptors = PropertyUtils.getPropertyDescriptors(type);

                try {
                    T entity = ConstructorUtils.invokeConstructor(type, null);
                    for (PropertyDescriptor descriptor : descriptors) {
                        if (!"class".equals(descriptor.getName())) {
                            BeanUtils.copyProperty(entity, descriptor.getName(), rs.getObject(descriptor.getName()));
                        }
                    }
                    list.add(entity);
                } catch (NoSuchMethodException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InstantiationException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (pstmt != null) {
                try {
                    pstmt.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return list;
    }

    private BoneCP connectionPool;

    /**
     * Obtiene una conexión del pool
     *
     * @return Conexión del pool o null
     */
    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = connectionPool.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        }

        return connection;
    }

    /**
     * Apaga el pool
     */
    public void shutdown() {
        connectionPool.shutdown();
    }

}
