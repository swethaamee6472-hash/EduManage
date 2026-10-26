package com.sset.erp.repository;

import com.sset.erp.model.FeeCharge;
import com.sset.erp.model.FeePayment;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Data access contract for student charges and immutable payment receipts.
 */
public interface FeeRepository {
    List<FeeCharge> findCharges() throws IOException;
    List<FeeCharge> findChargesByStudent(String rollNumber) throws IOException;
    List<FeePayment> findPaymentsByStudent(String rollNumber) throws IOException;
    List<FeePayment> findPaymentsForCharge(String feeId) throws IOException;
    BigDecimal getPaidAmount(String feeId) throws IOException;
    FeeCharge saveCharge(FeeCharge charge) throws IOException;
    FeePayment recordPayment(String feeId, BigDecimal amount, String paymentMethod,
                             String receivedBy) throws IOException;
}
