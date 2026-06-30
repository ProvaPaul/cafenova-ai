package com.smartcafe.website.service;

import com.smartcafe.website.dto.request.ReservationRequest;
import com.smartcafe.website.entity.*;
import com.smartcafe.website.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final CafeTableRepository cafeTableRepository;
    private final NotificationRepository notificationRepository;

    public List<CafeTableEntity> getAvailableTables() {
        return cafeTableRepository.findAllByOrderByTableNumberAsc();
    }

    @Transactional
    public ReservationEntity create(Long customerId, ReservationRequest req) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        cafeTableRepository.findById(req.getTableId())
                .orElseThrow(() -> new IllegalArgumentException("Table not found"));

        ReservationEntity res = ReservationEntity.builder()
                .customerId(customerId)
                .tableId(req.getTableId())
                .customerName(customer.getFullName())
                .partySize(req.getPartySize())
                .reservationDate(LocalDate.parse(req.getReservationDate()))
                .reservationTime(LocalTime.parse(req.getReservationTime()))
                .status("PENDING")
                .notes(req.getNotes())
                .build();
        res = reservationRepository.save(res);

        notificationRepository.save(NotificationEntity.builder()
                .customerId(customerId)
                .title("Reservation Received")
                .message("Your reservation on " + req.getReservationDate() + " at " + req.getReservationTime() + " is pending confirmation.")
                .type("RESERVATION")
                .build());

        return res;
    }

    public List<ReservationEntity> getMyReservations(Long customerId) {
        return reservationRepository.findByCustomerIdOrderByReservationDateDescReservationTimeDesc(customerId);
    }

    @Transactional
    public ReservationEntity cancel(Long customerId, Long reservationId) {
        ReservationEntity res = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (!res.getCustomerId().equals(customerId))
            throw new IllegalArgumentException("Unauthorized");
        if ("COMPLETED".equals(res.getStatus()) || "CANCELLED".equals(res.getStatus()))
            throw new IllegalArgumentException("Cannot cancel this reservation");
        res.setStatus("CANCELLED");
        return reservationRepository.save(res);
    }
}
