/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.leotech.smartpid;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

/**
 *
 * @author Anusha
 */
public class PropertiesLoader {

    private Properties symbolmap;

    public PropertiesLoader(File file) throws MalformedURLException, IOException {
        symbolmap = new Properties();

        //Populate the symbol map from the XML file
        symbolmap.loadFromXML(file.toURI().toURL().openStream());

    }

    //variable length arguments are packed into an array
    //which can be accessed and passed just like any array
    public String lookupSymbol(String symbol, String... variables) {
        //Retrieve the value of the associated key
        String message = symbolmap.getProperty(symbol);
        if (message == null) {
            return "";
        }
        //Interpolate parameters if necessary
        //and return the message
        return String.format(message, variables);
    }
}
