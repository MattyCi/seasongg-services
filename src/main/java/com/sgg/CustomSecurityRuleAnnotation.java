package com.sgg;

public @interface CustomSecurityRuleAnnotation {

    /**
     * The name of the parameter in the controller method which contains the
     * resource ID this permisssion is required for
     * @return resourceIdName
     */
    String resourceIdName();

    /**
     * The permission required, e.g. READ_ONLY, ADMIN, WRITE
     * @return permission
     */
    String permission();

}
