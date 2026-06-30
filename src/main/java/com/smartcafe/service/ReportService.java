package com.smartcafe.service;

import com.smartcafe.exception.AppException;

import java.time.LocalDate;
import java.util.List;

public interface ReportService {

    record DailySalesRow(LocalDate date, int orderCount, double revenue, double avgOrder) {}
    record MonthlySalesRow(int year, int month, String monthName, int orderCount, double revenue) {}
    record ProductSalesRow(String productName, int timesOrdered, int totalQty, double totalRevenue) {}
    record CustomerReportRow(String customerName, String phone, int visitCount, double totalSpent, int loyaltyPoints) {}

    List<DailySalesRow>    getDailySales(LocalDate from, LocalDate to) throws AppException;
    List<MonthlySalesRow>  getMonthlySales(int year) throws AppException;
    List<ProductSalesRow>  getProductSales(LocalDate from, LocalDate to) throws AppException;
    List<CustomerReportRow> getTopCustomers(int limit) throws AppException;
}
