package uasb.c14220127.myapplication

data class InvoiceData(
    val invoiceId: String = "",
    val bookingId: String = "",
    val userId: String = "",
    val workerId: String = "",
    val workerName: String = "",
    val date: String = "",
    val scheduledDateTime: Long = 0L,  // Tambahkan field ini
    val amount: Int = 0,
    val paymentMethod: String = "",
    val timestamp: Long = System.currentTimeMillis()
)