package com.smartcafe.model;

import java.time.LocalDateTime;

public class Payment {

    public static final String METHOD_CASH          = "CASH";
    public static final String METHOD_CARD          = "CARD";
    public static final String METHOD_MOBILE        = "MOBILE_PAYMENT";

    private int           id;
    private int           orderId;
    private String        paymentMethod;
    private double        amountPaid;
    private double        changeAmount;
    private String        transactionRef;
    private int           cashierId;
    private LocalDateTime paidAt;

    public Payment() {}

    public Payment(int orderId, String paymentMethod, double amountPaid,
                   double changeAmount, int cashierId) {
        this.orderId       = orderId;
        this.paymentMethod = paymentMethod;
        this.amountPaid    = amountPaid;
        this.changeAmount  = changeAmount;
        this.cashierId     = cashierId;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public int     getId()        { return id; }
    public void    setId(int v)   { id = v; }

    public int     getOrderId()       { return orderId; }
    public void    setOrderId(int v)  { orderId = v; }

    public String  getPaymentMethod()          { return paymentMethod; }
    public void    setPaymentMethod(String v)  { paymentMethod = v; }

    public double  getAmountPaid()       { return amountPaid; }
    public void    setAmountPaid(double v) { amountPaid = v; }

    public double  getChangeAmount()       { return changeAmount; }
    public void    setChangeAmount(double v) { changeAmount = v; }

    public String  getTransactionRef()          { return transactionRef; }
    public void    setTransactionRef(String v)  { transactionRef = v; }

    public int     getCashierId()       { return cashierId; }
    public void    setCashierId(int v)  { cashierId = v; }

    public LocalDateTime getPaidAt()               { return paidAt; }
    public void          setPaidAt(LocalDateTime v) { paidAt = v; }
}
