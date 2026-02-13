package digital.raywel.japchai.kstomp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import digital.raywel.japchai.kstomp.R
import digital.raywel.japchai.kstomp.ui.component.ChatBubble
import digital.raywel.japchai.kstomp.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel()) {

    val messages by viewModel.messages.collectAsState()
    val connected by viewModel.connected.collectAsState()

    var name by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var darkMode by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    val enabled = text.isNotBlank()

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

    val interactionSource = remember { MutableInteractionSource() }

    val pillShape: Shape = RoundedCornerShape(999.dp)

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
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = pillShape,
                            ambientColor = Color.Black.copy(alpha = 0.10f),
                            spotColor = Color.Black.copy(alpha = 0.10f)
                        )
                        .clip(pillShape)
                        .background(inputBg)
                        .border(
                            width = 1.dp,
                            color = if (darkMode) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.10f),
                            shape = pillShape
                        )
                        .padding(horizontal = 16.dp),
                    singleLine = true,
                    cursorBrush = SolidColor(Color(0xFF4F46E5)),
                    textStyle = TextStyle(
                        color = textColor,
                        fontSize = 15.sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (text.isNotBlank()) {
                                viewModel.sendMessage(text.trim())
                                text = ""
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxHeight(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isBlank()) {
                                Text(
                                    text = "Type a message…",
                                    color = textColor.copy(alpha = 0.45f),
                                    fontSize = 15.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        viewModel.sendMessage(text.trim())
                        text = ""
                    },
                    enabled = enabled
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_send),
                        contentDescription = "Send",
                        tint = if (enabled) Color(0xFF4F46E5) else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}