package digital.raywel.japchai.stomp_client

/**
 * Configuration for [KStompClient].
 *
 * ใช้กำหนด behavior เรื่อง reconnect และ heartbeat/timeout
 * เพื่อให้ WebSocket + STOMP ทำงานเสถียรบน Android (ที่ network เปลี่ยนบ่อย)
 */
data class StompConfig(

    /**
     * เปิด/ปิด auto reconnect เมื่อ WebSocket หลุด
     *
     * - true: ถ้าหลุดจะพยายามเชื่อมต่อใหม่อัตโนมัติ
     * - false: หลุดแล้วจบเลย ต้องเรียก connect() ใหม่เอง
     */
    val reconnectEnabled: Boolean = true,

    /**
     * เวลา delay ครั้งแรกก่อน reconnect (milliseconds)
     *
     * ตัวอย่าง:
     * - initialDelay = 1000ms (1 วินาที)
     */
    val reconnectInitialDelayMs: Long = 1000,

    /**
     * เวลา delay สูงสุดของ reconnect (milliseconds)
     *
     * ถ้า reconnect attempt เยอะ ๆ จะเพิ่ม delay ไปเรื่อย ๆ
     * แต่จะไม่เกินค่านี้
     *
     * ตัวอย่าง:
     * - maxDelay = 30000ms (30 วินาที)
     */
    val reconnectMaxDelayMs: Long = 30000,

    /**
     * ตัวคูณของ exponential backoff ตอน reconnect
     *
     * สูตรคร่าว ๆ:
     * delay = initialDelay * (multiplier ^ (attempt - 1))
     *
     * ตัวอย่าง:
     * attempt1 = 1s
     * attempt2 = 1s * 1.7 = 1.7s
     * attempt3 = 1.7s * 1.7 = 2.89s
     * ...
     */
    val reconnectMultiplier: Double = 1.7,

    /**
     * ระยะเวลาที่ client จะส่ง STOMP heartbeat ไปยัง server (milliseconds)
     *
     * STOMP heartbeat คือการส่ง "\n" เพื่อบอกว่า connection ยังไม่ตาย
     *
     * ค่านี้ช่วยให้:
     * - กัน idle connection ถูกตัด
     * - ช่วย detect connection dead ได้เร็วขึ้น
     *
     * ถ้า server ไม่รองรับ heartbeat ก็ไม่เป็นไร
     * แต่ client จะยังส่ง heartbeat ออกไป
     *
     * ตัวอย่าง:
     * - 10000ms = ส่งทุก 10 วินาที
     */
    val heartbeatSendIntervalMs: Long = 10_000,

    /**
     * ระยะเวลาที่ client จะ "รอ server activity" ก่อนตัดสินว่า connection ตาย (milliseconds)
     *
     * ถ้า client ไม่ได้รับอะไรจาก server เลย (message/heartbeat) เกินเวลานี้
     * client จะ close websocket แล้ว trigger reconnect
     *
     * เหมาะสำหรับ Android เพราะ network หลุดแบบเงียบ ๆ ได้บ่อย
     *
     * ตัวอย่าง:
     * - 30000ms = ถ้าเงียบเกิน 30 วินาที จะถือว่าหลุด
     *
     * แนะนำให้ตั้งมากกว่า heartbeatSendIntervalMs หลายเท่า
     * เช่น 60-120 วินาที
     */
    val serverTimeoutMs: Long = 30_000
)
