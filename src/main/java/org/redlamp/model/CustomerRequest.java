package org.redlamp.model;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class CustomerRequest implements Serializable
{

    private String first_name;
    private String last_name,middle_name;
    private String birth_date,bank_verification_number;

    private String address_1, address_2, address_3;
    private String district;
    private String town;

    private String residence;
    private String city, county,state;

    private String phone_number;
    private String gender;
    private long class_code, identity_type_id, occupation_id, marketing_info_id, risk_code_id, opening_reason_id,
            title_id,deposit_class_code;

    private String identity_no;
    private String id_issue_date, id_expiry_date;

    private String marital_status;
    private String home_address_1, home_address_2, home_address_3;

    public String getFirst_name()
    {
        return first_name;
    }

    public void setFirst_name(String first_name)
    {
        this.first_name = first_name;
    }

    public String getLast_name()
    {
        return last_name;
    }
    public void setLast_name(String last_name)
    {
        this.last_name = last_name;
    }

    public String getMiddle_name()
    {
        return middle_name;
    }

    public void setMiddle_name(String middle_name)
    {
        this.middle_name = middle_name;
    }

    public String getBank_verification_number()
    {
        return bank_verification_number;
    }

    public void setBank_verification_number(String bank_verification_number)
    {
        this.bank_verification_number = bank_verification_number;
    }

    public String getBirth_date()
    {
        return birth_date;
    }

    public void setBirth_date(String birth_date)
    {

        this.birth_date = birth_date;
    }

    public String getAddress_1()
    {
        return address_1;
    }

    public void setAddress_1(String address_1)
    {
        this.address_1 = address_1;
    }

    public String getAddress_2()
    {
        return address_2;
    }

    public void setAddress_2(String address_2)
    {
        this.address_2 = address_2;
    }

    public String getAddress_3()
    {
        return address_3;
    }

    public void setAddress_3(String address_3)
    {
        this.address_3 = address_3;
    }

    public String getDistrict()
    {
        return district;
    }

    public void setDistrict(String district)
    {
        this.district = district;
    }

    public String getTown()
    {
        return town;
    }

    public void setTown(String town)
    {
        this.town = town;
    }

    public String getResidence()
    {
        return residence;
    }

    public void setResidence(String residence)
    {
        this.residence = residence;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }

    public String getCounty()
    {
        return county;
    }

    public void setCounty(String county)
    {
        this.county = county;
    }

    public String getState()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state = state;
    }

    public String getPhone_number()
    {
        return phone_number;
    }

    public void setPhone_number(String phone_number)
    {
        this.phone_number = phone_number;
    }

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public long getClass_code()
    {
        return class_code;
    }

    public void setClass_code(long class_code)
    {
        this.class_code = class_code;
    }

    public long getDeposit_class_code()
    {
        return deposit_class_code;
    }

    public void setDeposit_class_code(long deposit_class_code)
    {
        this.deposit_class_code = deposit_class_code;
    }

    public long getIdentity_type_id()
    {
        return identity_type_id;
    }

    public void setIdentity_type_id(long identity_type_id)
    {
        this.identity_type_id = identity_type_id;
    }

    public long getOccupation_id()
    {
        return occupation_id;
    }

    public void setOccupation_id(long occupation_id)
    {
        this.occupation_id = occupation_id;
    }

    public long getMarketing_info_id()
    {
        return marketing_info_id;
    }

    public void setMarketing_info_id(long marketing_info_id)
    {
        this.marketing_info_id = marketing_info_id;
    }

    public long getRisk_code_id()
    {
        return risk_code_id;
    }

    public void setRisk_code_id(long risk_code_id)
    {
        this.risk_code_id = risk_code_id;
    }

    public long getOpening_reason_id()
    {
        return opening_reason_id;
    }

    public void setOpening_reason_id(long opening_reason_id)
    {
        this.opening_reason_id = opening_reason_id;
    }

    public long getTitle_id()
    {
        return title_id;
    }

    public void setTitle_id(long title_id)
    {
        this.title_id = title_id;
    }

    public String getIdentity_no()
    {
        return identity_no;
    }

    public void setIdentity_no(String identity_no)
    {
        this.identity_no = identity_no;
    }

    public String getId_issue_date()
    {
        return id_issue_date;
    }

    public void setId_issue_date(String id_issue_date)
    {
        this.id_issue_date = id_issue_date;
    }

    public String getId_expiry_date()
    {
        return id_expiry_date;
    }

    public void setId_expiry_date(String id_expiry_date)
    {
        this.id_expiry_date = id_expiry_date;
    }

    public String getMarital_status()
    {
        return marital_status;
    }

    public void setMarital_status(String marital_status)
    {
        this.marital_status = marital_status;
    }

    public String getHome_address_1()
    {
        return home_address_1;
    }

    public void setHome_address_1(String home_address_1)
    {
        this.home_address_1 = home_address_1;
    }

    public String getHome_address_2()
    {
        return home_address_2;
    }

    public void setHome_address_2(String home_address_2)
    {
        this.home_address_2 = home_address_2;
    }

    public String getHome_address_3()
    {
        return home_address_3;
    }

    public void setHome_address_3(String home_address_3)
    {
        this.home_address_3 = home_address_3;
    }

    @Override
    public String toString()
    {
        return "CustomerRequest [first_name=" + first_name + ", last_name=" + last_name + ", birth_date=" + birth_date
                + ", address_1=" + address_1 + ", address_2=" + address_2 + ", address_3=" + address_3 + ", district="
                + district + ", town=" + town + ", residence=" + residence + ", city=" + city + ", county=" + county
                + ", phone_number=" + phone_number + ", gender=" + gender + ", class_code=" + class_code
                + ", identity_type_id=" + identity_type_id + ", occupation_id=" + occupation_id + ", marketing_info_id="
                + marketing_info_id + ", risk_code_id=" + risk_code_id + ", opening_reason_id=" + opening_reason_id
                + ", title_id=" + title_id + ", identity_no=" + identity_no + ", id_issue_date=" + id_issue_date
                + ", id_expiry_date=" + id_expiry_date + ", marital_status=" + marital_status + ", home_address_1="
                + home_address_1 + ", home_address_2=" + home_address_2 + ", home_address_3=" + home_address_3 + "]";
    }

}
