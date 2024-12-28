package uasb.c14220127.myapplication

data class BookingData(
    val bookingId: String = "",
    val userId: String = "",
    val workerId: String = "",
    val date: String = "",
    val duration: String = "",
    val jobs: List<String> = listOf(),
    val price: Int = 0,
    val paymentMethod: String = "",
    val status: String = "pending", // pending, completed, cancelled
    val timestamp: Long = System.currentTimeMillis(),
    // User details
    val userName: String = "",
    val userAddress: String = "",
    val userPhone: String = "",
    // Worker details
    val workerName: String = "",
    val workerAddress: String = "",
    val workerPhone: String = ""
)
