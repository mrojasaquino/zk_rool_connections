package com.ecaresoft.cp.ui;

import org.zkoss.zul.Window;

/**
 *
 * @author odelarosa
 */
public class Index extends Window {

    public Index() {

        setWidth("100%");
        setHeight("100%");
    }

    public void onCreate() {
        getPage().setTitle("Connection Pool");
    }

}
