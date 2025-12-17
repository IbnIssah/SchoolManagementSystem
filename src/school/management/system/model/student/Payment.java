package school.management.system.model.student;

import lombok.Data;
import java.util.Date;

/**
 * Represents a payment made by a student in the school management system.
 * 
 * @author Ibn Issah
 */
@Data
public class Payment {
    /** The unique identifier of the payment. */
    private int paymentId;
    /** The unique identifier of the student who made the payment. */
    private int studentId;
    /** The amount paid by the student. */
    private double amountPaid;
    /** The date when the payment was made. */
    private Date paymentDate;
    /** The term for which the payment was made. */
    private String term;
    /** The academic year for which the payment was made. */
    private int academicYear;
}