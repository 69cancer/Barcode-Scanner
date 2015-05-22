package com.dxd.barcodescanner;

/**
 * Created by dxd on 5/21/15.
 */
public class Products {
    private String key;
    private String value;

    public Products() {

    }

    public Products(String key, String value) {
        super();
        this.key = key;
        this.value = value;

    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
