package com.gmail.keith1987.pojos;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by keith on 18/03/2018.
 */
public class Order implements Serializable {
    private String accont;
    private long ReceivedAt;
    private long SubmittedAt;
    private String market;
    private String action;
    private int size;

    public Order(){}

    public Order(String accont, long ReceivedAt, long SubmittedAt, String market, String action, int size) {
        this.accont = accont;
        this.ReceivedAt = ReceivedAt;
        this.SubmittedAt = SubmittedAt;
        this.market = market;
        this.action = action;
        this.size = size;
    }

    public String getAccont() {
        return accont;
    }

    public void setAccont(String accont) {
        this.accont = accont;
    }

    public long getReceivedAt() {
        return ReceivedAt;
    }

    @JacksonXmlProperty(localName = "ReceivedAt")
    public void setReceivedAt(long ReceivedAt) {
        this.ReceivedAt = ReceivedAt;
    }

    public long getSubmittedAt() {
        return SubmittedAt;
    }

    @JacksonXmlProperty(localName = "SubmittedAt")
    public void setSubmittedAt(long SubmittedAt) {
        this.SubmittedAt = SubmittedAt;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(accont, ReceivedAt, SubmittedAt, market, action, size);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Order){
            final Order other = (Order) obj;
            return Objects.equals(accont, other.accont)
                    && Objects.equals(ReceivedAt, other.ReceivedAt)
                    && Objects.equals(SubmittedAt, other.SubmittedAt)
                    && Objects.equals(market, other.market)
                    && Objects.equals(action, other.action)
                    && Objects.equals(size, other.size);
        } else{
            return false;
        }
    }
}
