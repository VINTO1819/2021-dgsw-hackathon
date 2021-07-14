import kotlinx.serialization.Serializable

@Serializable
data class IoTProtocol(
    val opCode: String,
    val payload: Array<Int>
)
