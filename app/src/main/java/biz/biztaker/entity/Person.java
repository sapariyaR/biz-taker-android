package biz.biztaker.entity;

import java.io.Serializable;

/**
 * Created by Anand Jakhaniya on 11-02-2018.
 * @author Anand Jakhaniya
 */

public class Person implements Serializable {

    public Long localId;
    public Long userId;
    public String email;
    public String role;
    public String firstName;
    public String lastName;
    public String gender;
    public Long birthDate;
    public Double latitude;
    public Double longitude;
    public String address;
    public String dpUrl;

}
