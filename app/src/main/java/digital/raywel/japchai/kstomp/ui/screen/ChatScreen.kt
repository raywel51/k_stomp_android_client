package digital.raywel.japchai.kstomp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import digital.raywel.japchai.kstomp.model.ChatMessage
import digital.raywel.japchai.kstomp.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {

    val messages by viewModel.messages.collectAsState()
    val connected by viewModel.connected.collectAsState()

    var name by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var darkMode by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // auto scroll when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val bgColor = if (darkMode) Color(0xFF111827) else Color(0xFFF3F4F6)
    val cardColor = if (darkMode) Color(0xFF1F2937) else Color.White
    val textColor = if (darkMode) Color.White else Color.Black
    val inputBg = if (darkMode) Color(0xFF374151) else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF4F46E5))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "STOMP Chat",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (connected) "ONLINE" else "OFFLINE",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { darkMode = !darkMode },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6366F1)
                )
            ) {
                Text("Toggle Dark", fontSize = 12.sp)
            }
        }

        // Chat container
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(12.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(cardColor)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                state = listState
            ) {
                items(messages) { msg ->
                    val isMe = msg.sender == viewModel.deviceName

                    ChatBubble(
                        msg = msg,
                        isMe = isMe,
                        darkMode = darkMode
                    )
                }
            }
        }

        // Input area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Type a message") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (name.isNotBlank() && text.isNotBlank()) {
                                viewModel.sendMessage(name.trim(), text.trim())
                                text = ""
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedContainerColor = inputBg,
                        unfocusedContainerColor = inputBg
                    )
                )

                Button(
                    onClick = {
                        viewModel.sendMessage(name.trim(), text.trim())
                        text = ""
                    }
                ) {
                    Text("Send")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

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