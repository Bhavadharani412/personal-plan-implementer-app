package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompoundApp(viewModel: CompoundViewModel) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            HeaderBar()
        },
        bottomBar = {
            BottomNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Toast Notification Overlay
            toastMessage?.let { msg ->
                LaunchedEffect(msg) {
                    delay(3000)
                    viewModel.clearToast()
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .zIndex(99f)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.2f)),
                        elevation = CardDefaults.cardElevation(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Alert", tint = AccentGreen)
                            Text(
                                text = msg,
                                style = Typography.bodyMedium,
                                color = WhiteText
                            )
                        }
                    }
                }
            }

            // Screen Switch Logic
            when (currentTab) {
                "DASHBOARD" -> DashboardScreen(viewModel = viewModel)
                "PLANNER" -> PlannerScreen(viewModel = viewModel)
                "FOCUS" -> FocusScreen(viewModel = viewModel)
                "JOURNAL" -> JournalScreen(viewModel = viewModel)
                "ANALYTICS" -> AnalyticsScreen(viewModel = viewModel)
                "SETTINGS" -> SettingsScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HeaderBar() {
    Surface(
        color = DarkBackground,
        border = BorderStroke(0.dp, Color.Transparent),
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Bhava 3.0",
                    style = Typography.titleLarge.copy(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = DarkForestGreen
                )
                Text(
                    text = "STREAK & PROGRESS MANAGER",
                    style = Typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MutedText
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = DarkForestGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AccentGreen.copy(alpha = 0.12f))
                        .border(1.dp, AccentGreen.copy(alpha = 0.4f), RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "XP",
                        style = Typography.labelSmall,
                        color = AccentGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavBar(
    currentTab: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        color = DarkSurfaceElevated,
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, DarkBorder),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                Triple("DASHBOARD", "Dash", Icons.Default.Home),
                Triple("PLANNER", "Plan", Icons.Default.DateRange),
                Triple("FOCUS", "Focus", Icons.Default.PlayArrow),
                Triple("JOURNAL", "Journal", Icons.Default.Edit),
                Triple("ANALYTICS", "Streak", Icons.Default.Star),
                Triple("SETTINGS", "Settings", Icons.Default.Settings)
            )

            tabs.forEach { (route, label, icon) ->
                val active = currentTab == route
                Column(
                    modifier = Modifier
                        .clickable(onClick = { onTabSelected(route) })
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) AccentGreen.copy(alpha = 0.12f) else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (active) AccentGreen else MutedText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                style = Typography.labelSmall,
                                color = if (active) AccentGreen else MutedText,
                                fontSize = 10.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

// 1. DASHBOARD SCREEN
@Composable
fun DashboardScreen(viewModel: CompoundViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    val xp by viewModel.totalXP.collectAsStateWithLifecycle(0)
    val level by viewModel.currentLevel.collectAsStateWithLifecycle(1)
    val streak by viewModel.currentStreak.collectAsStateWithLifecycle(0)
    val focusHrs by viewModel.focusHours.collectAsStateWithLifecycle(0.0)

    val incompleteTasks = tasks.filter { !it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Hero Streak Momentum Card (Dark Espresso background #473C33, white text #FFFFFF)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "CURRENT STREAK",
                                style = Typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    letterSpacing = 1.8.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White.copy(alpha = 0.65f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = String.format(Locale.US, "Level %02d Active", level),
                                style = Typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = Color.White
                            )
                        }

                        // Circular streak flame count
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "🔥 $streak",
                                    style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Text(
                                    text = "DAYS",
                                    style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Motivational phrase depending on streak momentum
                    val momentumMessage = when {
                        streak == 0 -> "Let's log a study block or task to start today's habit spark!"
                        streak < 3 -> "Your momentum is starting. Keep the flame glowing!"
                        streak < 7 -> "You are building serious momentum. Stay dedicated!"
                        else -> "Incredible! 7+ days of deep accountability. Keep pushing!"
                    }
                    Text(
                        text = momentumMessage,
                        style = Typography.bodyMedium.copy(fontSize = 14.sp),
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Level XP Indicator inside hero card
                    val progressMax = 1000
                    val currentLevelXp = xp % progressMax
                    val displayLvlXp = currentLevelXp.coerceAtLeast(0)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$xp / $progressMax XP SECURED",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${(displayLvlXp.toFloat() / progressMax * 100).toInt()}%",
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { displayLvlXp.toFloat() / progressMax },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = SuccessGreen,
                        trackColor = Color.White.copy(alpha = 0.15f)
                    )
                }
            }
        }

        // Secondary focus metrics in floating soft cards (#FFFFFF surface, #E7DED2 borders)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Focus hours
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("FOCUS HOURS", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f Hrs", focusHrs),
                            style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                            color = AccentGreen
                        )
                    }
                }

                // XP Progress Secondary Overview
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("SAGE GOLD LEVEL", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(GoldXPAccent)
                            )
                            Text(
                                text = "Grade ${String.format(Locale.US, "%02d", level)}",
                                style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = WhiteText
                            )
                        }
                    }
                }
            }
        }

        // Active study/priority action layout
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ACTIVE PRIORITY", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGreen.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("CORE", style = Typography.labelSmall.copy(fontSize = 9.sp), color = AccentGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (incompleteTasks.isNotEmpty()) {
                        val activeTask = incompleteTasks.first()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeTask.title,
                                    style = Typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp),
                                    color = WhiteText
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(GoldXPAccent.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(activeTask.priority, style = Typography.labelSmall.copy(fontSize = 9.sp), color = DarkForestGreen)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(DarkBackground)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(activeTask.category, style = Typography.labelSmall.copy(fontSize = 9.sp), color = MutedText)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    viewModel.setSelectedFocusTask(activeTask)
                                    viewModel.selectTab("FOCUS")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("START", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                "No items matching active priority.",
                                style = Typography.bodyMedium,
                                color = MutedText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.selectTab("PLANNER") },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("ADD TO PLAN", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Today's Plan listing
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TODAY'S PLAN", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = DarkForestGreen)
                    TextButton(onClick = { viewModel.selectTab("PLANNER") }) {
                        Text("VIEW ALL", style = Typography.labelSmall, color = AccentGreen)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                if (incompleteTasks.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSurfaceElevated,
                        border = BorderStroke(1.dp, DarkBorder),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Clear", tint = SuccessGreen, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Today's Plan is completed!\nSelect ADD TO PLAN to outline new milestones.",
                                style = Typography.bodyMedium,
                                color = MutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        incompleteTasks.take(3).forEach { task ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                border = BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(24.dp),
                                elevation = CardDefaults.cardElevation(1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(RoundedCornerShape(50))
                                                .background(if (task.priority.uppercase() == "HIGH") GoldXPAccent else SuccessGreen)
                                        )
                                        Text(
                                            text = task.title,
                                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = WhiteText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.updateTaskCompletion(task, it) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SuccessGreen,
                                            uncheckedColor = MutedText.copy(alpha = 0.5f),
                                            checkmarkColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Inspiration Quote Block
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "\"${settings.selectedQuotePreset}\"",
                    style = Typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = MutedText,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

// 2. PLANNER (TASK MANAGER) SCREEN
@Composable
fun PlannerScreen(viewModel: CompoundViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val categoriesList = listOf("ALL", "Dentist PWA", "DSA", "Core Engineering", "AI Learning", "Cybersecurity", "Journal")
    val filteredTasks = if (selectedCategoryFilter == "ALL") tasks else tasks.filter { it.category == selectedCategoryFilter }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Text(
                text = "PLANNER",
                style = Typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = DarkForestGreen
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Category filters horizontally scrollable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriesList.forEach { cat ->
                    val isActive = selectedCategoryFilter == cat
                    Button(
                        onClick = { selectedCategoryFilter = cat },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) DarkForestGreen else DarkSurfaceElevated
                        ),
                        border = BorderStroke(1.dp, if (isActive) DarkForestGreen else DarkBorder),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.uppercase(),
                            style = Typography.labelSmall.copy(fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium),
                            color = if (isActive) Color.White else MutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Empty",
                            tint = MutedText.copy(alpha = 0.5f),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "NO MILESTONES DEFINED",
                            style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = DarkForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add custom task tags or milestones using the float action below.",
                            style = Typography.bodyMedium,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks) { task ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) SuccessGreen.copy(alpha = 0.12f) else DarkSurfaceElevated
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (task.isCompleted) SuccessGreen.copy(alpha = 0.3f) else DarkBorder
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(if (task.isCompleted) 0.dp else 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = task.isCompleted,
                                        onCheckedChange = { viewModel.updateTaskCompletion(task, it) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = SuccessGreen,
                                            uncheckedColor = MutedText.copy(alpha = 0.5f),
                                            checkmarkColor = Color.White
                                        )
                                    )
                                    Column {
                                        Text(
                                            text = task.title,
                                            style = Typography.bodyLarge.copy(
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 16.sp
                                            ),
                                            color = if (task.isCompleted) MutedText else WhiteText,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (task.priority.uppercase() == "P0") GoldXPAccent.copy(alpha = 0.15f)
                                                        else AccentGreen.copy(alpha = 0.1f)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = task.priority,
                                                    style = Typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = if (task.priority.uppercase() == "P0") DarkForestGreen else AccentGreen
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(DarkBackground)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = task.category,
                                                    style = Typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = MutedText
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { taskToEdit = task },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = AccentGreen)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteTask(task) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = DarkForestGreen,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 18.dp) // Offset slightly up from the floating bottom bar
                .testTag("add_task_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (showAddTaskDialog) {
        TaskFormDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, priority, category ->
                viewModel.addTask(title, priority, category)
                showAddTaskDialog = false
            }
        )
    }

    taskToEdit?.let { task ->
        TaskFormDialog(
            initialTask = task,
            onDismiss = { taskToEdit = null },
            onConfirm = { title, priority, category ->
                viewModel.editTask(task, title, priority, category)
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskFormDialog(
    initialTask: Task? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var priority by remember { mutableStateOf(initialTask?.priority ?: "P1") }
    var category by remember { mutableStateOf(initialTask?.category ?: "DSA") }

    val priorities = listOf("P0", "P1", "P2")
    val categories = listOf("Dentist PWA", "DSA", "Core Engineering", "AI Learning", "Cybersecurity", "Journal")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (initialTask == null) "NEW WORKSTREAM ITEM" else "REDEFINE SYSTEM TASK",
                    style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = DarkForestGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("TITLE / TARGET", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        placeholder = { Text("e.g. Architect Core Spanner Schema", color = MutedText.copy(alpha = 0.5f)) }
                    )
                }

                Column {
                    Text("PRIORITY CATEGORY", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        priorities.forEach { p ->
                            val isSelected = priority == p
                            Button(
                                onClick = { priority = p },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) DarkForestGreen else DarkBackground
                                ),
                                border = BorderStroke(1.dp, if (isSelected) DarkForestGreen else DarkBorder),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = p,
                                    style = Typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                    color = if (isSelected) Color.White else MutedText
                                )
                            }
                        }
                    }
                }

                Column {
                    Text("KNOWLEDGE STREAM", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categories.chunked(2).forEach { rowCats ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowCats.forEach { cat ->
                                    val isSelected = category == cat
                                    Button(
                                        onClick = { category = cat },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) AccentGreen else DarkBackground
                                        ),
                                        border = BorderStroke(1.dp, if (isSelected) AccentGreen else DarkBorder),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            style = Typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                            color = if (isSelected) Color.White else MutedText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("CANCEL", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    }
                    Button(
                        onClick = { onConfirm(title, priority, category) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("SAVE TASKS", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}

// 3. 90-DAY TRACKER
@Composable
fun Tracker90DayScreen(viewModel: CompoundViewModel) {
    val progressList by viewModel.allDayProgress.collectAsStateWithLifecycle()
    val beastCount by viewModel.beastDays.collectAsStateWithLifecycle(0)
    val completedCount by viewModel.completedDaysCount.collectAsStateWithLifecycle(0)

    var dayToConfigure by remember { mutableStateOf<DayProgress?>(null) }

    val progressPercent = if (progressList.isNotEmpty()) {
        (completedCount.toFloat() / 90f * 100).toInt()
    } else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
             Text(
                text = "HABIT PROGRESS TRACKER",
                style = Typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = DarkForestGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A structured 90-day progress matrix designed to establish consistency and maintain streak loops.",
                style = Typography.bodyMedium,
                color = MutedText
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("HABIT BASELINE COMPLETION", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Day $completedCount of 90 Completed", style = Typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp), color = WhiteText)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("$progressPercent% COMPLETED", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / 90f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AccentGreen,
                        trackColor = DarkBackground
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HIGH FOCUS DAYS: $beastCount", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = GoldXPAccent), color = GoldXPAccent)
                        Text("CORE TIMELINE STATUS", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("HABIT MATRIX GRID", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = DarkForestGreen)
                    Spacer(modifier = Modifier.height(12.dp))

                    val chunks = progressList.sortedBy { it.dayNumber }.chunked(10)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        chunks.forEach { rowDays ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowDays.forEach { day ->
                                    val cellColor = when (day.status) {
                                        "BEAST_MODE" -> GoldXPAccent
                                        "COMPLETED" -> SuccessGreen
                                        "LOW_DAY" -> SuccessGreen.copy(alpha = 0.45f)
                                        else -> Color.Transparent
                                    }
                                    val borderColor = when (day.status) {
                                        "UNATTEMPTED" -> DarkBorder
                                        else -> cellColor
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(cellColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                                            .clickable { dayToConfigure = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${day.dayNumber}",
                                            style = Typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = if (day.status == "UNATTEMPTED") MutedText else DarkForestGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (rowDays.size < 10) {
                                    val remaining = 10 - rowDays.size
                                    for (i in 0 until remaining) {
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(SuccessGreen.copy(alpha=0.45f)))
                            Text("LOW", style = Typography.labelSmall, color = MutedText, fontSize = 10.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(SuccessGreen))
                            Text("FULL", style = Typography.labelSmall, color = MutedText, fontSize = 10.sp)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(GoldXPAccent))
                            Text("BEAST", style = Typography.labelSmall, color = MutedText, fontSize = 10.sp)
                        }
                        Text("Click cells to toggle.", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentGreen, fontSize = 10.sp)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("90-DAY FOCUS PHASES", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = DarkForestGreen)
                val phases = listOf(
                    Triple("Phase 1: Foundation", "Days 1-22: Formulate key patterns and daily focus schedules.", completedCount >= 22),
                    Triple("Phase 2: Apps Build", "Days 23-45: Construct and architect functional engineering frameworks.", completedCount >= 45),
                    Triple("Phase 3: Expansion", "Days 46-68: Expand concepts to high-scale layout structures.", completedCount >= 68),
                    Triple("Phase 4: Goals Achieved", "Days 69-90: Leverage accountability arrays and complete targets.", completedCount >= 90)
                )

                phases.forEach { (title, subtitle, completed) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, if (completed) SuccessGreen else DarkBorder),
                        shape = RoundedCornerShape(24.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(title, style = Typography.titleLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold), color = if (completed) SuccessGreen else WhiteText)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(subtitle, style = Typography.bodyMedium.copy(fontSize = 13.sp), color = MutedText)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            if (completed) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = SuccessGreen, modifier = Modifier.size(24.dp))
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = "Active", tint = MutedText, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    dayToConfigure?.let { day ->
        val statePresets = listOf("UNATTEMPTED", "LOW_DAY", "COMPLETED", "BEAST_MODE")
        Dialog(onDismissRequest = { dayToConfigure = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CONFIGURE STATE: DAY ${day.dayNumber}", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DarkForestGreen)
                    Text("Select execution baseline for Day ${day.dayNumber}:", style = Typography.bodyMedium, color = MutedText, textAlign = TextAlign.Center)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statePresets.forEach { preset ->
                            val isActive = day.status == preset
                            Button(
                                onClick = {
                                    viewModel.updateDayStatus(day.dayNumber, preset)
                                    dayToConfigure = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isActive) DarkForestGreen else DarkBackground
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = if (!isActive) BorderStroke(1.dp, DarkBorder) else null
                            ) {
                                Text(
                                    text = preset,
                                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isActive) Color.White else DarkForestGreen
                                )
                            }
                        }
                    }

                    TextButton(onClick = { dayToConfigure = null }) {
                        Text("Dismiss Option", style = Typography.labelSmall, color = MutedText)
                    }
                }
            }
        }
    }
}

// 4. JOURNAL SCREEN
@Composable
fun JournalScreen(viewModel: CompoundViewModel) {
    val entries by viewModel.allJournalEntries.collectAsStateWithLifecycle()

    var showForm by remember { mutableStateOf(false) }
    var detailEntry by remember { mutableStateOf<JournalEntry?>(null) }

    var shipped by remember { mutableStateOf("") }
    var blockers by remember { mutableStateOf("") }
    var improvements by remember { mutableStateOf("") }
    var lessons by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("Focus") }
    var energy by remember { mutableStateOf(3) }
    var wins by remember { mutableStateOf("") }

    val formattedDate = remember {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    if (showForm) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "NEW EXECUTION LOG", 
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                    color = DarkForestGreen
                )
                IconButton(onClick = { showForm = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Form", tint = DarkForestGreen)
                }
            }

            Text("DATE: $formattedDate", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)

            OutlinedTextField(
                value = shipped,
                onValueChange = { shipped = it },
                label = { Text("What did you accomplish today?", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = blockers,
                onValueChange = { blockers = it },
                label = { Text("What blockers / friction did you face today?", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = improvements,
                onValueChange = { improvements = it },
                label = { Text("What can you improve tomorrow?", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = lessons,
                onValueChange = { lessons = it },
                label = { Text("Lessons learned (Markdown/Notes)", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = wins,
                onValueChange = { wins = it },
                label = { Text("Daily wins (comma separated)", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            Column {
                Text("ENERGY CAPACITY FLOW: Level $energy / 5", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                Spacer(modifier = Modifier.height(6.dp))
                Slider(
                    value = energy.toFloat(),
                    onValueChange = { energy = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = AccentGreen,
                        activeTrackColor = AccentGreen,
                        inactiveTrackColor = DarkBorder
                    )
                )
            }

            Button(
                onClick = {
                    viewModel.addJournalEntry(
                        date = formattedDate,
                        shipped = shipped,
                        blockers = blockers,
                        improvements = improvements,
                        lessons = lessons,
                        mood = mood,
                        energy = energy,
                        wins = wins
                    )
                    shipped = ""
                    blockers = ""
                    improvements = ""
                    lessons = ""
                    wins = ""
                    showForm = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("COMMIT JOURNAL ARCHIVE", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                Text(
                    text = "JOURNAL LOGS",
                    style = Typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = DarkForestGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Archive daily reflections, micro-learnings, roadblocks, and energy alignment metrics.",
                    style = Typography.bodyMedium,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(18.dp))

                if (entries.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "None",
                                tint = AccentGreen,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "NO ARCHIVED LOGS FOUND",
                                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = DarkForestGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Commit your first daily reflection review to index historical progression.",
                                style = Typography.bodyMedium,
                                color = MutedText,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(entries) { entry ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                                border = BorderStroke(1.dp, DarkBorder),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { detailEntry = entry },
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("LOG RUN DATE: ${entry.date}", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = if (entry.reflectionWhatShipped.isNotBlank()) entry.reflectionWhatShipped else "Unquantified goals committed.",
                                            style = Typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                            color = WhiteText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(DarkBackground)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text("Energy Level: ${entry.energyLevel}/5", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DarkForestGreen)
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteJournalEntry(entry) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Purge Reflection", tint = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showForm = true },
                containerColor = AccentGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_journal_fab")
            ) {
                Icon(Icons.Default.Create, contentDescription = "Log Reflected Goals")
            }
        }
    }

    detailEntry?.let { entry ->
        Dialog(onDismissRequest = { detailEntry = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "JOURNAL ENTRY: ${entry.date}", 
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold), 
                        color = DarkForestGreen, 
                        textAlign = TextAlign.Center, 
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = DarkBorder)

                    Column {
                        Text("ACCOMPLISHED MILESTONES", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(entry.reflectionWhatShipped.ifBlank { "Unreported accomplishments." }, style = Typography.bodyLarge, color = WhiteText)
                    }

                    Column {
                        Text("Roadblocks Faced", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(entry.reflectionBlocker.ifBlank { "No roadblocks encountered." }, style = Typography.bodyLarge, color = WhiteText)
                    }

                    Column {
                        Text("Areas to Focus/Improve", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(entry.reflectionImprovement.ifBlank { "No improvement points identified." }, style = Typography.bodyLarge, color = WhiteText)
                    }

                    Column {
                        Text("Lessons learned", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(entry.lessonsLearned.ifBlank { "No specific lessons documented." }, style = Typography.bodyLarge, color = WhiteText)
                    }

                    Column {
                        Text("Today's Wins", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(entry.wins.ifBlank { "No unique wins recorded." }, style = Typography.bodyLarge, color = WhiteText)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ENERGY LEVEL:", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${entry.energyLevel} / 5", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DarkForestGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { detailEntry = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Close Archive Sheet", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }
}

// 5. ANALYTICS SCREEN
@Composable
fun AnalyticsScreen(viewModel: CompoundViewModel) {
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val xp by viewModel.totalXP.collectAsStateWithLifecycle(0)
    val focusHoursVal by viewModel.focusHours.collectAsStateWithLifecycle(0.0)
    val categoryHoursMap by viewModel.categoryHours.collectAsStateWithLifecycle(emptyMap())

    val completedTasksCount = tasks.count { it.isCompleted }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "ANALYTICS",
                style = Typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = DarkForestGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track your daily study hours, habit completions, and cumulative system progress.",
                style = Typography.bodyMedium,
                color = MutedText
            )
        }

        // Summary Card (Soft Floating Card, corner radius 24.dp, subtle background/borders)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GOLDEN XP", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$xp", style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = GoldXPAccent)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("FOCUS TIME", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(String.format(Locale.US, "%.1f H", focusHoursVal), style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = AccentGreen)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("COMPLETED", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("$completedTasksCount", style = Typography.headlineLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp), color = SuccessGreen)
                }
            }
        }

        // Newly Added: Category-wise Hour Tracking
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "CATEGORY WORKSTREAM BLOCK LOGS", 
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                    color = DarkForestGreen
                )

                categoryHoursMap.forEach { (cat, hrs) ->
                    val ratio = if (focusHoursVal > 0) (hrs / focusHoursVal).coerceIn(0.0, 1.0) else 0.0
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(cat, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = WhiteText)
                            Text(String.format(Locale.US, "%.1f Hrs (%.0f%%)", hrs, ratio * 100), style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { ratio.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentGreen,
                            trackColor = DarkBackground
                        )
                    }
                }
            }
        }

        // Linear Focus Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "DAILY STUDY INTENSITIES", 
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                    color = DarkForestGreen
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text("Focus velocity timeline over the past week.", style = Typography.bodyMedium, color = MutedText)
                Spacer(modifier = Modifier.height(16.dp))

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val linesCount = 4
                    for (i in 0..linesCount) {
                        val y = i * (height / linesCount)
                        drawLine(
                            color = DarkBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f
                        )
                    }

                    val points = if (focusHoursVal > 0) {
                        listOf(
                            Offset(0f, height * 0.85f),
                            Offset(width * 0.2f, height * 0.45f),
                            Offset(width * 0.4f, height * 0.65f),
                            Offset(width * 0.6f, height * 0.15f),
                            Offset(width * 0.8f, height * 0.55f),
                            Offset(width, height * 0.25f)
                        )
                    } else {
                        listOf(
                            Offset(0f, height * 0.85f),
                            Offset(width * 0.5f, height * 0.85f),
                            Offset(width, height * 0.85f)
                        )
                    }

                    val path = Path().apply {
                        moveTo(points[0].x, points[0].y)
                        for (i in 1 until points.size) {
                            val prev = points[i - 1]
                            val curr = points[i]
                            cubicTo(
                                x1 = (prev.x + curr.x) / 2,
                                y1 = prev.y,
                                x2 = (prev.x + curr.x) / 2,
                                y2 = curr.y,
                                x3 = curr.x,
                                y3 = curr.y
                            )
                        }
                    }

                    drawPath(
                        path = path,
                        color = AccentGreen,
                        style = Stroke(width = 3.5f)
                    )

                    points.forEach { pt ->
                        drawCircle(
                            color = GoldXPAccent,
                            radius = 5.5f,
                            center = pt
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("MON", style = Typography.labelSmall, color = MutedText)
                    Text("TUE", style = Typography.labelSmall, color = MutedText)
                    Text("WED", style = Typography.labelSmall, color = MutedText)
                    Text("THU", style = Typography.labelSmall, color = MutedText)
                    Text("FRI", style = Typography.labelSmall, color = MutedText)
                    Text("SAT", style = Typography.labelSmall, color = MutedText)
                }
            }
        }

        // Subject practicing progression
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SUBJECT PRACTICE OVERVIEW", 
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                    color = DarkForestGreen
                )
                Spacer(modifier = Modifier.height(4.dp))

                val computedCoding = (completedTasksCount * 12).coerceAtMost(100)
                val computedAlgo = (completedTasksCount * 18).coerceAtMost(100)
                val computedDesign = (completedTasksCount * 10).coerceAtMost(100)
                val computedComm = (completedTasksCount * 15).coerceAtMost(100)

                val skills = listOf(
                    Pair("Coding & Development", computedCoding),
                    Pair("Algorithms & Logic", computedAlgo),
                    Pair("Design & Systems", computedDesign),
                    Pair("Communication & Prep", computedComm)
                )

                skills.forEach { (name, ratio) ->
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(name, style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = WhiteText)
                            Text("$ratio%", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = AccentGreen)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { ratio / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = AccentGreen,
                            trackColor = DarkBackground
                        )
                    }
                }
            }
        }

        // Streak activity map block grid (Warm Minimal pastel GitHub style)
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
            border = BorderStroke(1.dp, DarkBorder),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "STREAK ACTIVITY GRID", 
                    style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), 
                    color = DarkForestGreen
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    for (col in 0 until 18) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            for (row in 0 until 5) {
                                val cellRatio = (col + row) % 5
                                val bg = when (cellRatio) {
                                    4 -> GoldXPAccent
                                    3 -> SuccessGreen
                                    2 -> SuccessGreen.copy(alpha = 0.6f)
                                    1 -> SuccessGreen.copy(alpha = 0.3f)
                                    else -> DarkBackground
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(bg)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A dense sequential matrix representing your active deep study consistency.",
                    style = Typography.bodyMedium,
                    color = MutedText,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// 6. FOCUS MODE SCREEN
@Composable
fun FocusScreen(viewModel: CompoundViewModel) {
    val selectedTask by viewModel.selectedFocusTask.collectAsStateWithLifecycle()
    val tasks by viewModel.allTasks.collectAsStateWithLifecycle()
    val incompleteTasks = tasks.filter { !it.isCompleted }

    val isTimerActive by viewModel.isTimerActive.collectAsStateWithLifecycle()
    val countdownSeconds by viewModel.countdownSeconds.collectAsStateWithLifecycle()
    val selectedSessionMinutes by viewModel.selectedSessionMinutes.collectAsStateWithLifecycle()
    val selectedTimerCategory by viewModel.selectedTimerCategory.collectAsStateWithLifecycle()

    var showTaskSelectorDropdown by remember { mutableStateOf(false) }
    var showCategorySelectorDropdown by remember { mutableStateOf(false) }
    var customMinInput by remember { mutableStateOf("") }

    val displayMin = countdownSeconds / 60
    val displaySec = countdownSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", displayMin, displaySec)

    val defaultCategories = listOf("Dentist PWA", "DSA", "Core Engineering", "AI Learning", "Cybersecurity", "Journal")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(22.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (isTimerActive) DangerRed else SuccessGreen)
                )
                Text(
                    text = if (isTimerActive) "SECURE STUDY SESSION ACTIVE" else "STUDY BLOCK STANDBY",
                    style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                    color = DarkForestGreen
                )
            }
            Text("BHAVA 3.0", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
        }

        // Active Task Selector Container
        Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
            Text("STUDY TARGET / MILESTONE", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, if (selectedTask != null) AccentGreen else DarkBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (!isTimerActive) showTaskSelectorDropdown = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selectedTask?.title ?: "STANDALONE TASK RUN // CLICK TO ASSIGN",
                        style = Typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                        color = if (selectedTask == null) MutedText else WhiteText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { if (!isTimerActive) viewModel.setSelectedFocusTask(null) },
                        enabled = !isTimerActive,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (selectedTask != null) Icons.Default.Close else Icons.Default.ArrowDropDown,
                            contentDescription = "Task control",
                            tint = DarkForestGreen
                        )
                    }
                }
            }
        }

        // Show standalone category drawer in case no task is active
        if (selectedTask == null) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text("STANDALONE FOCUS CATEGORY TRACKING", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = DarkSurfaceElevated,
                    border = BorderStroke(1.dp, DarkBorder),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { if (!isTimerActive) showCategorySelectorDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Subject: $selectedTimerCategory",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = AccentGreen,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Categories",
                            tint = DarkForestGreen
                        )
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${selectedSessionMinutes * 10} XP REWARD POTENTIAL",
                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp),
                color = GoldXPAccent
            )

            // Dynamic interactive circular timer
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Outer dashed visual orbit guide
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = AccentGreen.copy(alpha = 0.35f),
                        style = Stroke(
                            width = 2f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 12f), 0f
                            )
                        )
                    )
                }
                
                // Solid Inner circular shield (pure soft White background)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                        .clip(RoundedCornerShape(210.dp))
                        .background(Color.White)
                        .border(
                            width = 4.dp, 
                            color = AccentGreen.copy(alpha = 0.15f), 
                            shape = RoundedCornerShape(210.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            style = Typography.displayLarge.copy(
                                fontSize = 42.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = DarkForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTimerActive) "FOCUS ACTIVE" else "STANDBY",
                            style = Typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MutedText
                        )
                    }
                }
            }

            // Quick Preset Selection: 25, 45, 67, 120 minutes!
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("QUICK TIME PRESETS", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val presets = listOf(25, 45, 67, 120)
                    presets.forEach { min ->
                        val isActive = selectedSessionMinutes == min
                        Button(
                            onClick = {
                                if (!isTimerActive) {
                                    viewModel.setSessionMinutes(min)
                                }
                            },
                            enabled = !isTimerActive,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) DarkForestGreen else DarkSurfaceElevated,
                                disabledContainerColor = DarkBackground
                            ),
                            border = BorderStroke(1.dp, if (isActive) DarkForestGreen else DarkBorder),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "${min}m", 
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                                color = if (isActive) Color.White else MutedText
                            )
                        }
                    }
                }
            }

            // Dynamic Intelligent Random Hour Selector
            OutlinedButton(
                onClick = { viewModel.rollSmartSessionMinutes(selectedTask) },
                enabled = !isTimerActive,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DarkForestGreen
                ),
                border = BorderStroke(1.dp, AccentGreen),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(44.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Roll Random",
                        tint = AccentGreen
                    )
                    Text(
                        text = "ROLL DYNAMIC TIME LIMIT (MAX 120M)",
                        style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = DarkForestGreen
                    )
                }
            }

            // Custom Direct Numeric Minute Entry Box & Tactical Slider Control
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "TACTILE SCROLL OVERLAY (MAX 120M)",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = DarkForestGreen
                    )

                    // Intuitively shows current value as slider drags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Value:",
                            style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MutedText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkBackground)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${selectedSessionMinutes} Minutes",
                                style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AccentGreen
                            )
                        }
                    }

                    Slider(
                        value = selectedSessionMinutes.toFloat().coerceIn(1f, 120f),
                        onValueChange = { val mins = it.toInt().coerceIn(1, 120)
                            viewModel.setSessionMinutes(mins)
                        },
                        valueRange = 1f..120f,
                        colors = SliderDefaults.colors(
                            thumbColor = AccentGreen,
                            activeTrackColor = AccentGreen,
                            inactiveTrackColor = DarkBackground
                        ),
                        enabled = !isTimerActive,
                        modifier = Modifier.fillMaxWidth().testTag("custom_timer_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 Min", style = Typography.labelSmall, color = MutedText)
                        Text("60 Min", style = Typography.labelSmall, color = MutedText)
                        Text("120 Min", style = Typography.labelSmall, color = MutedText)
                    }

                    HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 2.dp))

                    Text(
                        text = "DIAL PRECISE COMPILATION DURATION",
                        style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = DarkForestGreen
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customMinInput,
                            onValueChange = { customMinInput = it },
                            placeholder = { Text("E.g., 55", color = MutedText.copy(alpha = 0.5f), fontSize = 12.sp) },
                            singleLine = true,
                            textStyle = Typography.bodyMedium.copy(color = WhiteText),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText,
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = DarkBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isTimerActive
                        )
                        Button(
                            onClick = {
                                val mins = customMinInput.toIntOrNull()
                                if (mins != null && mins in 1..120) {
                                    viewModel.setSessionMinutes(mins)
                                    customMinInput = ""
                                } else {
                                    viewModel.showToast("Enter minutes between 1 and 120")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(14.dp),
                            enabled = !isTimerActive
                        ) {
                            Text("SET", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Active Controls Panel Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isTimerActive) {
                Button(
                    onClick = {
                        viewModel.cancelTimer()
                        viewModel.showToast("Focus aborted. Countdown reset.")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("ABORT TIMER", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
                Button(
                    onClick = { viewModel.pauseTimer() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("PAUSE RUN", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.startTimer()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("START BACKGROUND STUDY TIMER", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
            }
        }
    }

    // Task Selection dialogue
    if (showTaskSelectorDropdown) {
        Dialog(onDismissRequest = { showTaskSelectorDropdown = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SELECT FOCUS TARGET",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkForestGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (incompleteTasks.isEmpty()) {
                        Text(
                            text = "No tasks registered. Create a task in PLANNER first.",
                            style = Typography.bodyMedium,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showTaskSelectorDropdown = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("OK", style = Typography.labelSmall, color = Color.White)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(incompleteTasks) { task ->
                                Text(
                                    text = task.title,
                                    style = Typography.bodyLarge.copy(fontSize = 15.sp),
                                    color = WhiteText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSelectedFocusTask(task)
                                            showTaskSelectorDropdown = false
                                        }
                                        .padding(vertical = 14.dp, horizontal = 8.dp)
                                )
                                Divider(color = DarkBorder)
                            }
                        }
                    }
                }
            }
        }
    }

    // Category Selection dialogue
    if (showCategorySelectorDropdown) {
        Dialog(onDismissRequest = { showCategorySelectorDropdown = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SELECT STANDALONE SYSTEM",
                        style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = DarkForestGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(defaultCategories) { cat ->
                            Text(
                                text = cat,
                                style = Typography.bodyLarge.copy(fontSize = 15.sp),
                                color = WhiteText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.setSelectedTimerCategory(cat)
                                        showCategorySelectorDropdown = false
                                    }
                                    .padding(vertical = 14.dp, horizontal = 8.dp)
                            )
                            Divider(color = DarkBorder)
                        }
                    }
                }
            }
        }
    }
}

// 7. CONFIGURATION / SETTINGS SCREEN
@Composable
fun SettingsScreen(viewModel: CompoundViewModel) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    var showWipeConfirm by remember { mutableStateOf(false) }

    val motivationQuotes = listOf(
        "Execution creates confidence.",
        "No zero days.",
        "Consistency compounds.",
        "Ship before perfection.",
        "One more step today."
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "SYSTEM CONFIG CONTROL",
                style = Typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                ),
                color = DarkForestGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Modify core parameters, change motivation presets, or wipe execution database archives.",
                style = Typography.bodyMedium,
                color = MutedText
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("IDENTITY MOTIVATION PRESET", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DarkForestGreen)

                    motivationQuotes.forEach { q ->
                        val isSelected = settings.selectedQuotePreset == q
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateQuotePreset(q) }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q, 
                                style = Typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal), 
                                color = if (isSelected) AccentGreen else WhiteText
                            )
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateQuotePreset(q) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = AccentGreen,
                                    unselectedColor = DarkBorder
                                )
                            )
                        }
                        HorizontalDivider(color = DarkBorder)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DarkBorder),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("TARGET DURATION DEADLINE", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DarkForestGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Establish target calendar metrics for automated countdown displays.",
                        style = Typography.bodyMedium,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    var targetInput by remember { mutableStateOf(settings.examTargetDate) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = targetInput,
                            onValueChange = { targetInput = it },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AccentGreen,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            placeholder = { Text("YYYY-MM-DD", color = MutedText) }
                        )
                        Button(
                            onClick = { viewModel.updateExamTargetDate(targetInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkForestGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(50.dp)
                        ) {
                            Text("SAVE", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("RESTORE SYSTEM DEFAULTS (WIPE)", style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = DangerRed)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Irreversibly cleans structural data models including daily ship checklists, habit configurations, timer entries, and journal reports.",
                        style = Typography.bodyMedium,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showWipeConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("RESET CONSOLE DATA ENGINE", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                    }
                }
            }
        }
    }

    if (showWipeConfirm) {
        Dialog(onDismissRequest = { showWipeConfirm = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DangerRed),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CONFIRM TOTAL PURGE", style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = DangerRed)
                    Text(
                        text = "This process is permanent. Resets your Planner targets, logged execution sheets, 90-day progress benchmarks, and experience scores.",
                        style = Typography.bodyMedium,
                        color = WhiteText,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TextButton(
                            onClick = { showWipeConfirm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("ABORT", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MutedText)
                        }
                        Button(
                            onClick = {
                                viewModel.resetAllData()
                                showWipeConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("PURGE ALL", style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
