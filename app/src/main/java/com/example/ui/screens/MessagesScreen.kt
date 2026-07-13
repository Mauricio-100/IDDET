package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Mic
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.scale
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.User
import com.example.ui.IddetViewModel
import com.example.ui.components.VerificationBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(viewModel: IddetViewModel, navController: NavController) {
    val partners by viewModel.chatPartners.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshConversations()
        while (true) {
            kotlinx.coroutines.delay(8000)
            viewModel.refreshConversations()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (partners.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No messages yet. Find someone to chat with!",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(partners, key = { it.id }) { partner ->
                        ChatPartnerItem(
                            user = partner,
                            onClick = { navController.navigate("chat/${partner.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatPartnerItem(user: User, onClick: () -> Unit) {
    // Mock status logic based on user ID for demonstration
    val isOnline = user.id.hashCode() % 2 == 0
    val isTyping = user.id.hashCode() % 3 == 0
    val isRecordingVoice = user.id.hashCode() % 5 == 0 && !isTyping
    val isRead = user.id.hashCode() % 7 == 0

    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    val typingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    val micScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.username.firstOrNull()?.toString()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = user.username,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (user.isVerified) {
                    Spacer(modifier = Modifier.width(4.dp))
                    VerificationBadge(userName = user.username)
                }
                Spacer(modifier = Modifier.width(4.dp))
                if (isOnline) {
                    Text("!!", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                } else {
                    Text("!", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isRead) {
                    Text("!! ", color = Color(0xFF2196F3), fontWeight = FontWeight.Bold)
                }
                if (isTyping) {
                    Text(
                        text = "°•°•°?",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = typingAlpha),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (isRecordingVoice) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Recording",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp).scale(micScale)
                    )
                } else {
                    Text(
                        text = "Tap to view conversation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}
