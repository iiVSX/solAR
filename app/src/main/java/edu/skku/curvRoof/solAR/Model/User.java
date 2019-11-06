package edu.skku.curvRoof.solAR.Model;

import java.io.Serializable;
import java.util.ArrayList;

public class User implements Serializable {
    String tel;
    String userID;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    String email;
    double elec_fee;
    double expect_fee;
    ArrayList<Trial> trialList;

    public User() {
        trialList = new ArrayList<>();
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<Trial> getTrialList() {
        return trialList;
    }

    public double getElec_fee() {
        return elec_fee;
    }

    public void setElec_fee(double elec_fee) {
        this.elec_fee = elec_fee;
    }

    public double getExpect_fee() {
        return expect_fee;
    }

    public void setExpect_fee(double expect_fee) {
        this.expect_fee = expect_fee;
    }

}
