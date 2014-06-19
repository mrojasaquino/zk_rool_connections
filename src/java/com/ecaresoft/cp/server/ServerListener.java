package com.ecaresoft.cp.server;

import com.ecaresoft.cp.db.DB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 *
 * @author odelarosa
 */
public class ServerListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        
    }

    public void contextDestroyed(ServletContextEvent sce) {
        DB.getInstance().shutdown();
    }

}
