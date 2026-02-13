package digital.raywel.japchai.kstomp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import digital.raywel.japchai.kstomp.model.ChatMessage

@Composable
fun ChatBubble(
    msg: ChatMessage,
    isMe: Boolean,
    darkMode: Boolean
) {
    val bubbleColor = if (isMe) Color(0xFF4F46E5)
    else if (darkMode) Color(0xFF374151)
    else Color(0xFFE5E7EB)

    val contentColor = if (isMe) Color.White
    else if (darkMode) Color.White
    else Color.Black

    val time = remember(msg.ts) {
        java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date(msg.ts))
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bubbleColor)
                .padding(12.dp)
        ) {
            Text(
                text = "${msg.sender} • $time",
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = msg.content,
                fontSize = 14.sp,
                color = contentColor
            )
        }
    }

    Spacer(modifier = Modifier.height(10.dp))
}