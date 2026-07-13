package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.data.ActfileWithUser
import com.example.ui.IddetViewModel
import com.example.ui.components.ActfileCard
import com.example.ui.components.VerificationBadge
import com.example.ui.components.MarkdownEditor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: IddetViewModel, navController: NavController) {
    val actfiles by viewModel.actfiles.collectAsStateWithLifecycle()
    val followedActfiles by viewModel.followedActfiles.collectAsStateWithLifecycle()
    val recommendedUsers by viewModel.giants.collectAsStateWithLifecycle()
    
    var feedTab by remember { mutableStateOf(0) } // 0: For You, 1: Following
    var showComposer by remember { mutableStateOf(false) }
    
    val activeActfiles = remember(feedTab, actfiles, followedActfiles) {
        when (feedTab) {
            0 -> actfiles
            1 -> followedActfiles
            else -> actfiles.shuffled()
        }
    }
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()
    
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            // Hide FAB when app bar is hiding
            if (scrollBehavior.state.heightOffset > -50f) {
                FloatingActionButton(
                    onClick = { showComposer = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Actfile")
                }
            }
        },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("IDDET Feed", fontWeight = FontWeight.Bold) },
                    actions = {
                        val notifications by viewModel.notifications.collectAsState()
                        val unreadCount = notifications.count { !it.isRead }
                        
                        IconButton(onClick = { navController.navigate("notifications") }) {
                            BadgedBox(
                                badge = {
                                    if (unreadCount > 0) {
                                        Badge { Text(unreadCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                            }
                        }
                        IconButton(onClick = { viewModel.refreshActfiles() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    ),
                    scrollBehavior = scrollBehavior
                )
                // Feed Selector
                TabRow(
                    selectedTabIndex = feedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = feedTab == 0,
                        onClick = { feedTab = 0 },
                        text = { Text("For You", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = feedTab == 1,
                        onClick = { feedTab = 1 },
                        text = { Text("Following", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = feedTab == 2,
                        onClick = { feedTab = 2 },
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Random", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                if (recommendedUsers.isNotEmpty() && (feedTab == 0 || activeActfiles.isEmpty())) {
                    item {
                        Text(
                            text = if (feedTab == 1) "Follow users to populate your feed!" else "Recommended for you",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(recommendedUsers, key = { it.id }) { user ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { navController.navigate("profile/${user.id}") }
                                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                        .width(100.dp)
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
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = user.username,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                items(activeActfiles, key = { it.id }) { actfile ->
                    val isMine = actfile.userId == viewModel.currentUser.value?.id
                    ActfileCard(
                        actfile = actfile,
                        onLike = { viewModel.likeActfile(it) },
                        onView = { viewModel.incrementView(it) },
                        onUserClick = {
                            val currentUserId = viewModel.currentUser.value?.id
                            if (it == currentUserId) {
                                navController.navigate("profile")
                            } else {
                                navController.navigate("profile/$it")
                            }
                        },
                        onComment = { actfileId ->
                            navController.navigate("discussion/$actfileId")
                        },
                        onDelete = if (isMine) { { viewModel.deleteActfile(it) } } else null,
                        onMentionClick = { username ->
                            scope.launch {
                                val u = viewModel.getUserByUsername(username)
                                if (u != null) {
                                    navController.navigate("profile/${u.id}")
                                }
                            }
                        }
                    )
                }

                if (activeActfiles.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (feedTab == 1) "No posts in your following feed yet. Find friends to follow!" else "No posts available.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
        
        if (showComposer) {
            ActfileComposer(
                onDismiss = { showComposer = false },
                onPublish = { content, tags ->
                    viewModel.publishActfile(content, tags)
                    showComposer = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActfileComposer(
    onDismiss: () -> Unit,
    onPublish: (String, String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "New Actfile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            MarkdownEditor(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = "Write your markdown actfile here...\n\nHint: Use the toolbar for bold, links, etc."
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Tags (comma separated, e.g. code, design)") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (content.isNotBlank()) onPublish(content, tags)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Publish Actfile")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
