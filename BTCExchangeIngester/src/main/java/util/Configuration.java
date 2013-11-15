/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 * 
 *  http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package util;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * A simple class to manage loading the property file containing needed configuration data
 * from the package. Once loaded the configuration is held in memory as a singleton.  Since
 * we already require the simplejpa.properties file to support SimpleJPA, we use that
 * to store additional configuration values.
 */
public class Configuration {

	private static final Configuration configuration = new Configuration();
    private final Properties props = new Properties();
    private static final String AWS_PROPERTY_PATH = "properties/AwsCredentials.properties";
    
    private Configuration () {
        try {
            props.load(new FileInputStream(AWS_PROPERTY_PATH));
        } catch (Exception e) {
            System.out.println("Unable to load configuration: "+e.getMessage());
        }
    }

    public static final Configuration getInstance () {
        return configuration;
    }
    
    public String getProperty (String propertyName) {
        return props.getProperty(propertyName);
    }
}
