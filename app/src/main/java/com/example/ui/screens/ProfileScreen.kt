package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.ui.IddetViewModel
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.example.ui.components.VerificationBadge
import com.example.ui.components.ActfileCard
import com.example.ui.components.MarkdownEditor
import com.example.ui.components.OpenGraphPreview
import com.example.data.UserProfileNetwork
import com.example.data.UpdateProfileRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: IddetViewModel, navController: NavController) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Safe check
    if (currentUser == null) return
    val user = currentUser!!
    
    val userActfiles by viewModel.getUserActfiles(user.id).collectAsStateWithLifecycle(initialValue = emptyList())
    
    var showEditDialog by remember { mutableStateOf(false) }
    var showVerificationDialog by remember { mutableStateOf(false) }
    var commentDialogTarget by remember { mutableStateOf<String?>(null) }
    
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
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
                    IconButton(onClick = { 
                        scope.launch {
                            viewModel.refreshProfile()
                            Toast.makeText(context, "Profil actualisé !", Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Profile")
                    }
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Cat Logo
                    Image(
                        painter = painterResource(id = com.example.R.drawable.ic_cat_logo),
                        contentDescription = "IDDET Cat Logo",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shadow(4.dp, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("IDDET Support", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SupportButton(
                            name = "Crislem",
                            color = Color(0xFFFACC15),
                            modifier = Modifier.weight(1f),
                            onClick = { 
                                // Mock ID for support
                                navController.navigate("chat/crislem")
                            }
                        )
                        SupportButton(
                            name = "Doffranel",
                            color = Color(0xFF06B6D4),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("chat/doffranel") }
                        )
                        SupportButton(
                            name = "C.M.O",
                            color = Color(0xFF8B5CF6),
                            modifier = Modifier.weight(1f),
                            onClick = { navController.navigate("chat/c.m.o") }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { 
                            // Open a default support chat
                            navController.navigate("chat/support")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Icon(Icons.Default.SupportAgent, contentDescription = "Support")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Contacter Support Client (!)")
                    }
                }
            }
            
            item {
            // Profile Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.username.firstOrNull()?.toString()?.uppercase() ?: "?",
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "@${user.username}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(8.dp))
                            VerificationBadge(modifier = Modifier.size(24.dp), userName = user.username)
                        }
                    }
                    
                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Niveau ${user.level} (XP: ${user.xp})") },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        if (!user.zodiacSign.isNullOrBlank()) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text("✨ ${user.zodiacSign}") },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    labelColor = MaterialTheme.colorScheme.tertiary
                                )
                            )
                        }
                    }

                    // Display complete contact info
                    if (!user.email.isNullOrBlank() || !user.phoneNumber.isNullOrBlank() || !user.birthDate.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (!user.email.isNullOrBlank()) {
                                    Text(
                                        text = "📧 Email : ${user.email}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!user.phoneNumber.isNullOrBlank()) {
                                    Text(
                                        text = "📞 Téléphone : ${user.phoneNumber}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (!user.birthDate.isNullOrBlank()) {
                                    Text(
                                        text = "📅 Naissance : ${user.birthDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${user.followersCount}", fontWeight = FontWeight.Bold)
                            Text(text = "Followers", style = MaterialTheme.typography.labelMedium)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${user.followingCount}", fontWeight = FontWeight.Bold)
                            Text(text = "Following", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showEditDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Modifier le profil")
                        }

                        if (!user.isVerified) {
                            Button(
                                onClick = { showVerificationDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFDC2626)
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vérifier mon compte")
                            }
                        }
                    }
                }
            }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "My Actfiles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(userActfiles, key = { it.id }) { actfile ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    ActfileCard(
                        actfile = actfile,
                        onLike = { viewModel.likeActfile(it) },
                        onView = { viewModel.incrementView(it) },
                        onUserClick = {}, // It's me
                        onComment = { navController.navigate("discussion/$it") },
                        onDelete = { viewModel.deleteActfile(it) },
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
            }
            
            if (userActfiles.isEmpty()) {
                item {
                    Text(
                        text = "You haven't posted any actfiles yet.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        var editedUsername by remember { mutableStateOf(user.username) }
        var editedBio by remember { mutableStateOf(user.bio) }
        var editedAvatarUrl by remember { mutableStateOf(user.avatarUrl ?: "") }
        var editedPrivacy by remember { mutableStateOf(user.privacySetting) }
        var editedEmail by remember { mutableStateOf(user.email ?: "") }
        var editedPhone by remember { mutableStateOf(user.phoneNumber ?: "") }
        var editedBirthDate by remember { mutableStateOf(user.birthDate ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modifier le Profil") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editedUsername,
                        onValueChange = { editedUsername = it },
                        label = { Text("Nom d'utilisateur") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedAvatarUrl,
                        onValueChange = { editedAvatarUrl = it },
                        label = { Text("URL de l'avatar") },
                        placeholder = { Text("https://example.com/image.png") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedEmail,
                        onValueChange = { editedEmail = it },
                        label = { Text("Adresse Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedPhone,
                        onValueChange = { editedPhone = it },
                        label = { Text("Numéro de Téléphone") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedBirthDate,
                        onValueChange = { editedBirthDate = it },
                        label = { Text("Anniversaire (JJ/MM/AAAA)") },
                        placeholder = { Text("Ex: 15/08/1995") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Paramètres de Confidentialité", style = MaterialTheme.typography.titleSmall)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { editedPrivacy = "Public" }) {
                            RadioButton(
                                selected = editedPrivacy == "Public",
                                onClick = { editedPrivacy = "Public" }
                            )
                            Text("Public")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { editedPrivacy = "Private" }) {
                            RadioButton(
                                selected = editedPrivacy == "Private",
                                onClick = { editedPrivacy = "Private" }
                            )
                            Text("Privé")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    var calculatedZodiac: String? = null
                    try {
                        val parts = editedBirthDate.split("/")
                        if (parts.size == 3) {
                            val day = parts[0].trim().toIntOrNull()
                            val month = parts[1].trim().toIntOrNull()
                            if (day != null && month != null && day in 1..31 && month in 1..12) {
                                calculatedZodiac = getZodiacSign(day, month)
                            }
                        }
                    } catch (e: Exception) {}

                    viewModel.updateProfile(
                        username = editedUsername,
                        avatarUrl = editedAvatarUrl,
                        bio = editedBio,
                        privacySetting = editedPrivacy,
                        email = editedEmail,
                        phoneNumber = editedPhone,
                        birthDate = editedBirthDate,
                        zodiacSign = calculatedZodiac
                    )
                    Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                    showEditDialog = false
                }) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showVerificationDialog) {
        var isVerifying by remember { mutableStateOf(false) }
        var verificationSuccess by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        val isLevelMet = user.level >= 2
        val isBioMet = user.bio.isNotBlank()
        val allCriteriaMet = isLevelMet && isBioMet

        AlertDialog(
            onDismissRequest = { if (!isVerifying) showVerificationDialog = false },
            title = {
                Text(
                    text = "Vérification Connect",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (!verificationSuccess) {
                        Text(
                            text = "Obtenez le badge d'authenticité rouge Iddet pour prouver votre identité et votre activité.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Criterion 1: Level 2+
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isLevelMet) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isLevelMet) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Être Actif sur la plateforme (Niveau 2+)",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Votre niveau actuel : ${user.level} (Requis : 2)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isLevelMet) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Criterion 2: Bio not empty
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isBioMet) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isBioMet) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Profil Complété (Bio remplie)",
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isBioMet) "Votre bio est configurée !" else "Veuillez remplir votre bio de profil.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isBioMet) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        if (!allCriteriaMet) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "💡 Astuce : Publiez des actfiles ou recevez des likes pour gagner de l'XP et passer au niveau 2 !",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            )
                        }
                    } else {
                        // Success screen
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFFDC2626).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Félicitations !",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = Color(0xFFDC2626)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Votre compte a été vérifié avec succès. Le badge Connect rouge est maintenant affiché sur votre profil !",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                if (!verificationSuccess) {
                    Button(
                        onClick = {
                            isVerifying = true
                            scope.launch {
                                kotlinx.coroutines.delay(1500)
                                viewModel.verifyCurrentUser()
                                isVerifying = false
                                verificationSuccess = true
                            }
                        },
                        enabled = allCriteriaMet && !isVerifying,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFDC2626)
                        )
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Vérifier maintenant")
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            showVerificationDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Terminer")
                    }
                }
            },
            dismissButton = {
                if (!verificationSuccess && !isVerifying) {
                    TextButton(onClick = { showVerificationDialog = false }) {
                        Text("Annuler")
                    }
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

fun getZodiacSign(day: Int, month: Int): String {
    return when (month) {
        1 -> if (day < 20) "Capricorne" else "Verseau"
        2 -> if (day < 19) "Verseau" else "Poissons"
        3 -> if (day < 21) "Poissons" else "Bélier"
        4 -> if (day < 20) "Bélier" else "Taureau"
        5 -> if (day < 21) "Taureau" else "Gémeaux"
        6 -> if (day < 21) "Gémeaux" else "Cancer"
        7 -> if (day < 23) "Cancer" else "Lion"
        8 -> if (day < 23) "Lion" else "Vierge"
        9 -> if (day < 23) "Vierge" else "Balance"
        10 -> if (day < 23) "Balance" else "Scorpion"
        11 -> if (day < 22) "Scorpion" else "Sagittaire"
        12 -> if (day < 22) "Sagittaire" else "Capricorne"
        else -> ""
    }
}

@Composable
fun SupportButton(name: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.15f),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}
