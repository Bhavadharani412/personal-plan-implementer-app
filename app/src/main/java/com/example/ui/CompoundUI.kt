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
        containerColor = DarkForestGreen,
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
                        colors = CardDefaults.cardColors(containerColor = AccentGreen),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, LightMintGreen.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Alert", tint = LightMintGreen)
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
        color = DarkForestGreen,
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    color = LightMintGreen
                )
                Text(
                    text = "STREAK & PROGRESS MANAGER",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.5.sp
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
                        tint = LightMintGreen
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(AccentGreen.copy(alpha = 0.3f))
                        .border(1.dp, AccentGreen, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "XP",
                        style = Typography.labelSmall,
                        color = GoldXPAccent,
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
        color = DarkForestGreen,
        tonalElevation = 8.dp,
        border = BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                        .weight(1f)
                        .clickable(onClick = { onTabSelected(route) })
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (active) AccentGreen else Color.Transparent)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (active) LightMintGreen else MutedText,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = label,
                                style = Typography.labelSmall,
                                color = if (active) LightMintGreen else MutedText,
                                fontSize = 10.sp
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Shield Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.2f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "YOUR STREAK",
                            style = Typography.labelSmall.copy(
                                fontSize = 10.sp,
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "Level %02d", level),
                                style = Typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = WhiteText
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$xp XP earned total",
                            style = Typography.labelSmall,
                            color = GoldXPAccent,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Streak block matching style:
                    // h-12 w-12 rounded-xl bg-[#0F2E15] border border-[#2F6B3B] flex flex-col items-center justify-center
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkForestGreen)
                            .border(1.dp, AccentGreen, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "$streak",
                                style = Typography.titleLarge.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = WhiteText
                            )
                            Text(
                                text = "STREAK",
                                style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                color = MutedText
                            )
                        }
                    }
                }
            }
        }

        // Stats grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Focus hours
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("FOCUS HOURS", style = Typography.labelSmall, color = MutedText)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(String.format(Locale.US, "%.1f Hrs", focusHrs), style = Typography.headlineMedium, color = LightMintGreen)
                    }
                }

                // XP Progress
                Card(
                    modifier = Modifier.weight(1.5f),
                    colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("TOTAL XP", style = Typography.labelSmall, color = MutedText)
                        Spacer(modifier = Modifier.height(4.dp))
                        val progressMax = 1000
                        val currentLevelXp = xp % progressMax
                        val displayLvlXp = currentLevelXp.coerceAtLeast(0)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text("$xp", style = Typography.headlineMedium, color = GoldXPAccent)
                            Text("$displayLvlXp / $progressMax XP", style = Typography.labelSmall, color = MutedText)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { displayLvlXp.toFloat() / progressMax },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = LightMintGreen,
                            trackColor = DarkForestGreen
                        )
                    }
                }
            }
        }

        // Active priority selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("ACTIVE PRIORITY", style = Typography.labelSmall, color = MutedText)
                    Spacer(modifier = Modifier.height(8.dp))
                    if (incompleteTasks.isNotEmpty()) {
                        val activeTask = incompleteTasks.first()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(activeTask.title, style = Typography.titleLarge, color = WhiteText)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Badge(containerColor = AccentGreen, contentColor = WhiteText) {
                                        Text(activeTask.priority, style = Typography.labelSmall, modifier = Modifier.padding(2.dp))
                                    }
                                    Badge(containerColor = DarkSurfaceElevated, contentColor = LightMintGreen) {
                                        Text(activeTask.category, style = Typography.labelSmall, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    viewModel.setSelectedFocusTask(activeTask)
                                    viewModel.selectTab("FOCUS")
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("START TIMER", style = Typography.labelSmall, color = DarkForestGreen)
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
                                "No items in your plan.",
                                style = Typography.bodyMedium,
                                color = MutedText,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.selectTab("PLANNER") },
                                colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("ADD TO PLAN", style = Typography.labelSmall, color = DarkForestGreen)
                            }
                        }
                    }
                }
            }
        }

        // Today's Plan
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TODAY'S PLAN", style = Typography.labelMedium, color = LightMintGreen)
                    TextButton(onClick = { viewModel.selectTab("PLANNER") }) {
                        Text("VIEW ALL", style = Typography.labelSmall, color = MutedText)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (incompleteTasks.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkForestGreen.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Clear", tint = SuccessGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Today's Plan is Cleared.\nSelect ADD TO PLAN or Quick Add to add a task.",
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
                                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.1f)),
                                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                  ) {
                                      Row(
                                          verticalAlignment = Alignment.CenterVertically,
                                          horizontalArrangement = Arrangement.spacedBy(12.dp),
                                          modifier = Modifier.weight(1f)
                                      ) {
                                          Icon(
                                              imageVector = Icons.Default.List,
                                              contentDescription = "Task",
                                              tint = LightMintGreen
                                          )
                                          Text(
                                              text = task.title,
                                              style = Typography.bodyLarge,
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
                                              uncheckedColor = LightMintGreen
                                          )
                                      )
                                  }
                              }
                          }
                      }
                  }
              }
          }

        // Quote Preset Block
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
                .padding(16.dp)
        ) {
            Text(
                text = "PLANNER",
                style = Typography.labelLarge,
                color = LightMintGreen
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoriesList.forEach { cat ->
                    val isActive = selectedCategoryFilter == cat
                    Button(
                        onClick = { selectedCategoryFilter = cat },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) LightMintGreen else AccentGreen.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, if (isActive) LightMintGreen else AccentGreen.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat.uppercase(),
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isActive) DarkForestGreen else MutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredTasks.isEmpty()) {
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
                            contentDescription = "Empty",
                            tint = AccentGreen,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "PLANNER IS EMPTY",
                            style = Typography.labelLarge,
                            color = LightMintGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Add your first task to get started on your plan.",
                            style = Typography.bodyMedium,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredTasks) { task ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (task.isCompleted) AccentGreen.copy(alpha = 0.05f) else AccentGreen.copy(alpha = 0.15f)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (task.isCompleted) AccentGreen.copy(alpha = 0.2f) else AccentGreen.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
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
                                            uncheckedColor = LightMintGreen
                                        )
                                    )
                                    Column {
                                        Text(
                                            text = task.title,
                                            style = Typography.bodyLarge,
                                            color = if (task.isCompleted) MutedText else WhiteText,
                                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Badge(
                                                containerColor = if (task.priority == "P0") DangerRed else AccentGreen,
                                                contentColor = WhiteText
                                            ) {
                                                Text(task.priority, style = Typography.labelSmall)
                                            }
                                            Badge(
                                                containerColor = DarkForestGreen,
                                                contentColor = LightMintGreen
                                            ) {
                                                Text(task.category, style = Typography.labelSmall)
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
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = LightMintGreen)
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
            containerColor = LightMintGreen,
            contentColor = DarkForestGreen,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
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
            colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
            border = BorderStroke(1.dp, LightMintGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (initialTask == null) "ADD NEW TASK" else "EDIT TASK",
                    style = Typography.labelLarge,
                    color = LightMintGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Column {
                    Text("TASK NAME", style = Typography.labelSmall, color = MutedText)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightMintGreen,
                            unfocusedBorderColor = AccentGreen,
                            focusedTextColor = WhiteText,
                            unfocusedTextColor = WhiteText
                        ),
                        placeholder = { Text("e.g. Implement Dijkstra Algorithm", color = MutedText) }
                    )
                }

                Column {
                    Text("PRIORITY LEVEL", style = Typography.labelSmall, color = MutedText)
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
                                    containerColor = if (isSelected) LightMintGreen else DarkSurfaceElevated
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    p,
                                    style = Typography.labelSmall,
                                    color = if (isSelected) DarkForestGreen else WhiteText
                                )
                            }
                        }
                    }
                }

                Column {
                    Text("CATEGORY", style = Typography.labelSmall, color = MutedText)
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
                                            containerColor = if (isSelected) LightMintGreen else DarkSurfaceElevated
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(vertical = 4.dp, horizontal = 2.dp)
                                    ) {
                                        Text(
                                            cat,
                                            style = Typography.labelSmall,
                                            color = if (isSelected) DarkForestGreen else WhiteText,
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
                        Text("CANCEL", style = Typography.labelSmall, color = MutedText)
                    }
                    Button(
                        onClick = { onConfirm(title, priority, category) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SAVE TASK", style = Typography.labelSmall, color = DarkForestGreen)
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
             Text(
                "BHAVA 3.0 PROGRESS TRACKER",
                style = Typography.labelLarge,
                color = LightMintGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "A 90-day progress habits map to build a consistent daily streak.",
                style = Typography.bodyMedium,
                color = MutedText
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("COMPLETED DAYS BASELINE", style = Typography.labelSmall, color = MutedText)
                            Text("Day $completedCount of 90 Completed", style = Typography.headlineMedium, color = WhiteText)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGreen)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("$progressPercent% COMPLETE", style = Typography.labelSmall, color = LightMintGreen)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { completedCount.toFloat() / 90f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = LightMintGreen,
                        trackColor = DarkBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("HIGH FOCUS DAYS: $beastCount", style = Typography.labelSmall, color = GoldXPAccent)
                        Text("DAILY HABIT TRACKING", style = Typography.labelSmall, color = MutedText)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, AccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STREAK GRID", style = Typography.labelMedium, color = LightMintGreen)
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
                                        "COMPLETED" -> LightMintGreen
                                        "LOW_DAY" -> SuccessGreen.copy(alpha = 0.5f)
                                        else -> Color.Transparent
                                    }
                                    val borderColor = when (day.status) {
                                        "UNATTEMPTED" -> AccentGreen.copy(alpha = 0.5f)
                                        else -> cellColor
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(cellColor)
                                            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                                            .clickable { dayToConfigure = day },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${day.dayNumber}",
                                            style = Typography.labelSmall,
                                            fontSize = 8.sp,
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

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("LIGHT FOCUS", style = Typography.labelSmall, color = SuccessGreen.copy(alpha = 0.5f))
                        Text("FULL Focus", style = Typography.labelSmall, color = LightMintGreen)
                        Text("HIGH FOCUS", style = Typography.labelSmall, color = GoldXPAccent)
                        Text("Click grid cell to change status.", style = Typography.labelSmall, color = MutedText)
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("90-DAY FOCUS PHASES", style = Typography.labelMedium, color = LightMintGreen)
                val phases = listOf(
                    Triple("Phase 1: Foundation", "Days 1-22: Learn core data structures and logic.", completedCount >= 22),
                    Triple("Phase 2: Apps Build", "Days 23-45: Write modern projects or applications.", completedCount >= 45),
                    Triple("Phase 3: Expansion", "Days 46-68: Learn system architecture and design.", completedCount >= 68),
                    Triple("Phase 4: Goals Achieved", "Days 69-90: Practice mock interviews and apply for jobs.", completedCount >= 90)
                )

                phases.forEach { (title, subtitle, completed) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                        border = BorderStroke(1.dp, if (completed) SuccessGreen else AccentGreen.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(title, style = Typography.titleLarge, color = if (completed) SuccessGreen else WhiteText)
                                Text(subtitle, style = Typography.bodyMedium, color = MutedText)
                            }
                            if (completed) {
                                Icon(Icons.Default.Check, contentDescription = "Finished", tint = SuccessGreen)
                            } else {
                                Icon(Icons.Default.Lock, contentDescription = "Active", tint = MutedText)
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
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, LightMintGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CONFIGURE STATE: DAY ${day.dayNumber}", style = Typography.labelLarge, color = LightMintGreen)
                    Text("Configure status baseline for this day's run:", style = Typography.bodyMedium, color = MutedText)

                    statePresets.forEach { preset ->
                        val isActive = day.status == preset
                        Button(
                            onClick = {
                                viewModel.updateDayStatus(day.dayNumber, preset)
                                dayToConfigure = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) LightMintGreen else DarkSurfaceElevated
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                preset,
                                style = Typography.labelSmall,
                                color = if (isActive) DarkForestGreen else WhiteText
                            )
                        }
                    }

                    TextButton(onClick = { dayToConfigure = null }) {
                        Text("CLOSE", style = Typography.labelSmall, color = MutedText)
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NEW JOURNAL ENTRY - $formattedDate", style = Typography.labelMedium, color = LightMintGreen)
                IconButton(onClick = { showForm = false }) {
                    Icon(Icons.Default.Close, contentDescription = "Back", tint = WhiteText)
                }
            }

            OutlinedTextField(
                value = shipped,
                onValueChange = { shipped = it },
                label = { Text("What did you accomplish today?", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightMintGreen,
                    unfocusedBorderColor = AccentGreen,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = blockers,
                onValueChange = { blockers = it },
                label = { Text("What blockers / friction did you face today?", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightMintGreen,
                    unfocusedBorderColor = AccentGreen,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = improvements,
                onValueChange = { improvements = it },
                label = { Text("What can you improve tomorrow?", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightMintGreen,
                    unfocusedBorderColor = AccentGreen,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = lessons,
                onValueChange = { lessons = it },
                label = { Text("Lessons learned (Markdown)", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightMintGreen,
                    unfocusedBorderColor = AccentGreen,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            OutlinedTextField(
                value = wins,
                onValueChange = { wins = it },
                label = { Text("Daily wins (comma separated)", style = Typography.bodyMedium, color = MutedText) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LightMintGreen,
                    unfocusedBorderColor = AccentGreen,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText
                )
            )

            Column {
                Text("ENERGY LEVEL: $energy / 5", style = Typography.labelSmall, color = MutedText)
                Slider(
                    value = energy.toFloat(),
                    onValueChange = { energy = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3,
                    colors = SliderDefaults.colors(
                        thumbColor = LightMintGreen,
                        activeTrackColor = LightMintGreen,
                        inactiveTrackColor = DarkSurfaceElevated
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
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("SAVE JOURNAL ENTRY", style = Typography.labelSmall, color = DarkForestGreen)
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "JOURNAL LOGS",
                    style = Typography.labelLarge,
                    color = LightMintGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A record of your daily learnings, wins, blockages, and mood.",
                    style = Typography.bodyMedium,
                    color = MutedText
                )
                Spacer(modifier = Modifier.height(16.dp))

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
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "NO EXECUTION LOGS ARCHIVED",
                                style = Typography.labelMedium,
                                color = LightMintGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Ship first review to index progress into the core mainframe.",
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
                                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { detailEntry = entry }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("RUN DATE: ${entry.date}", style = Typography.labelSmall, color = LightMintGreen)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (entry.reflectionWhatShipped.isNotBlank()) entry.reflectionWhatShipped else "No output quantified.",
                                            style = Typography.bodyLarge,
                                            color = WhiteText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Badge(containerColor = DarkBackground, contentColor = GoldXPAccent) {
                                                Text("Energy: Lvl ${entry.energyLevel}", style = Typography.labelSmall, modifier = Modifier.padding(2.dp))
                                            }
                                        }
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteJournalEntry(entry) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Clean Log", tint = DangerRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            FloatingActionButton(
                onClick = { showForm = true },
                containerColor = LightMintGreen,
                contentColor = DarkForestGreen,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("add_journal_fab")
            ) {
                Icon(Icons.Default.Create, contentDescription = "Log Shipment")
            }
        }
    }

    detailEntry?.let { entry ->
        Dialog(onDismissRequest = { detailEntry = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, LightMintGreen),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("JOURNAL ENTRY: ${entry.date}", style = Typography.labelLarge, color = LightMintGreen, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

                    Divider(color = AccentGreen)

                    Text("ACCOMPLISHED GOALS", style = Typography.labelSmall, color = MutedText)
                    Text(entry.reflectionWhatShipped.ifBlank { "N/A" }, style = Typography.bodyLarge, color = WhiteText)

                    Text("BLOCKERS / FRICTION", style = Typography.labelSmall, color = MutedText)
                    Text(entry.reflectionBlocker.ifBlank { "N/A" }, style = Typography.bodyLarge, color = WhiteText)

                    Text("TOMORROW'S IMPROVEMENTS", style = Typography.labelSmall, color = MutedText)
                    Text(entry.reflectionImprovement.ifBlank { "N/A" }, style = Typography.bodyLarge, color = WhiteText)

                    Text("LESSONS LEARNED", style = Typography.labelSmall, color = MutedText)
                    Text(entry.lessonsLearned.ifBlank { "N/A" }, style = Typography.bodyLarge, color = WhiteText)

                    Text("DAILY WINS", style = Typography.labelSmall, color = MutedText)
                    Text(entry.wins.ifBlank { "N/A" }, style = Typography.bodyLarge, color = WhiteText)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ENERGY SCORE: ${entry.energyLevel}/5", style = Typography.labelMedium, color = LightMintGreen)
                    }

                    Button(
                        onClick = { detailEntry = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                    ) {
                        Text("CLOSE DETAILS", style = Typography.labelSmall, color = WhiteText)
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

    val completedTasksCount = tasks.count { it.isCompleted }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "PROGRESS AND STREAK METRICS",
                style = Typography.labelLarge,
                color = LightMintGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Track your daily focus hours, habit completion, and streak history.",
                style = Typography.bodyMedium,
                color = MutedText
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL XP", style = Typography.labelSmall, color = MutedText)
                        Text("$xp", style = Typography.headlineLarge, color = GoldXPAccent)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("FOCUS TIME", style = Typography.labelSmall, color = MutedText)
                        Text(String.format(Locale.US, "%.1f H", focusHoursVal), style = Typography.headlineLarge, color = LightMintGreen)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("COMPLETED", style = Typography.labelSmall, color = MutedText)
                        Text("$completedTasksCount", style = Typography.headlineLarge, color = SuccessGreen)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DAILY FOCUS HISTORY", style = Typography.labelMedium, color = LightMintGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Your daily study and focus timeline over the last 24 hours.", style = Typography.bodyMedium, color = MutedText)
                    Spacer(modifier = Modifier.height(16.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        val width = size.width
                        val height = size.height

                        val linesCount = 4
                        for (i in 0..linesCount) {
                            val y = i * (height / linesCount)
                            drawLine(
                                color = AccentGreen.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }

                        val points = if (focusHoursVal > 0) {
                            listOf(
                                Offset(0f, height * 0.9f),
                                Offset(width * 0.2f, height * 0.5f),
                                Offset(width * 0.4f, height * 0.75f),
                                Offset(width * 0.6f, height * 0.2f),
                                Offset(width * 0.8f, height * 0.6f),
                                Offset(width, height * 0.1f)
                            )
                        } else {
                            listOf(
                                Offset(0f, height * 0.9f),
                                Offset(width * 0.5f, height * 0.9f),
                                Offset(width, height * 0.9f)
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
                            color = LightMintGreen,
                            style = Stroke(width = 4f)
                        )

                        points.forEach { pt ->
                            drawCircle(
                                color = GoldXPAccent,
                                radius = 6f,
                                center = pt
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("00:00", style = Typography.labelSmall, color = MutedText)
                        Text("06:00", style = Typography.labelSmall, color = MutedText)
                        Text("12:00", style = Typography.labelSmall, color = MutedText)
                        Text("18:00", style = Typography.labelSmall, color = MutedText)
                        Text("24:00", style = Typography.labelSmall, color = MutedText)
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("SUBJECT PRACTICE PROGRESS", style = Typography.labelMedium, color = LightMintGreen)
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
                                Text(name, style = Typography.bodyMedium, color = WhiteText)
                                Text("$ratio%", style = Typography.labelSmall, color = LightMintGreen)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { ratio / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = LightMintGreen,
                                trackColor = DarkForestGreen
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentGreen.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("STREAK ACTIVITY GRID", style = Typography.labelMedium, color = LightMintGreen)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        for (col in 0 until 18) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                for (row in 0 until 5) {
                                    val cellRatio = (col + row) % 4
                                    val bg = when (cellRatio) {
                                        3 -> LightMintGreen
                                        2 -> AccentGreen
                                        1 -> SuccessGreen
                                        else -> DarkForestGreen
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(bg)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Streak activity metric represents consecutive habit tracking days.",
                        style = Typography.bodyMedium,
                        color = MutedText,
                        fontSize = 11.sp
                    )
                }
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

    var isTimerActive by remember { mutableStateOf(false) }
    var countdownSeconds by remember { mutableStateOf(50 * 60) }
    var selectedSessionMinutes by remember { mutableStateOf(50) }
    var showTaskSelectorDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerActive, countdownSeconds) {
        if (isTimerActive && countdownSeconds > 0) {
            delay(1000)
            countdownSeconds -= 1
        } else if (isTimerActive && countdownSeconds == 0) {
            isTimerActive = false
            val xpReward = selectedSessionMinutes * 10
            viewModel.logFocusSession(
                missionName = selectedTask?.title ?: "Focus block",
                durationMinutes = selectedSessionMinutes,
                xpEarned = xpReward
            )
            selectedTask?.let { viewModel.updateTaskCompletion(it, true) }
            countdownSeconds = selectedSessionMinutes * 60
        }
    }

    val displayMin = countdownSeconds / 60
    val displaySec = countdownSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", displayMin, displaySec)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
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
                    text = if (isTimerActive) "TIMER IN FOCUS MODE" else "STANDBY // SELECT STUDY TIMER",
                    style = Typography.labelSmall,
                    color = LightMintGreen
                )
            }
            Text("BHAVA 3.0 // FOCUS", style = Typography.labelSmall, color = MutedText)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("STUDY FOCUS BLOCK", style = Typography.labelSmall, color = MutedText)
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = DarkSurfaceElevated,
                border = BorderStroke(1.dp, AccentGreen),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTaskSelectorDropdown = true }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        selectedTask?.title ?: "STANDALONE FOCUS BLOCK // SELECT TASK",
                        style = Typography.headlineMedium,
                        color = if (selectedTask == null) MutedText else WhiteText,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expand",
                        tint = LightMintGreen
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${selectedSessionMinutes * 10} XP REWARD POTENTIAL",
                style = Typography.labelSmall.copy(letterSpacing = 1.sp),
                color = GoldXPAccent,
                fontWeight = FontWeight.Bold
            )

            // Concentric circular timer design
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                // Outer dashed circle
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = AccentGreen.copy(alpha = 0.5f),
                        style = Stroke(
                            width = 1.5f,
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                                floatArrayOf(8f, 12f), 0f
                            )
                        )
                    )
                }
                
                // Solid Inner padding and borders
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clip(RoundedCornerShape(200.dp))
                        .background(AccentGreen.copy(alpha = 0.05f))
                        .border(
                            width = 6.dp, 
                            color = AccentGreen.copy(alpha = 0.2f), 
                            shape = RoundedCornerShape(200.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formattedTime,
                            style = Typography.displayLarge.copy(
                                fontSize = 44.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-1).sp
                            ),
                            color = WhiteText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "STUDY FOCUS TIMER",
                            style = Typography.labelSmall.copy(
                                fontSize = 9.sp,
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Presets row styled to sleek standards
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val presets = listOf(25, 50, 90)
                presets.forEach { min ->
                    val isActive = selectedSessionMinutes == min
                    Button(
                        onClick = {
                            if (!isTimerActive) {
                                selectedSessionMinutes = min
                                countdownSeconds = min * 60
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) LightMintGreen else AccentGreen.copy(alpha = 0.15f)
                        ),
                        border = BorderStroke(1.dp, if (isActive) LightMintGreen else AccentGreen.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${min}m", 
                            style = Typography.labelSmall.copy(fontWeight = FontWeight.Bold), 
                            color = if (isActive) DarkForestGreen else WhiteText
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isTimerActive) {
                Button(
                    onClick = {
                        isTimerActive = false
                        countdownSeconds = selectedSessionMinutes * 60
                        viewModel.showToast("Focus cancelled. Timer reset.")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CANCEL TIMER", style = Typography.labelSmall, color = WhiteText)
                }
                Button(
                    onClick = { isTimerActive = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("PAUSE TIMER", style = Typography.labelSmall, color = DarkForestGreen)
                }
            } else {
                Button(
                    onClick = {
                        isTimerActive = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("START STUDY TIMER", style = Typography.labelSmall, color = DarkForestGreen)
                }
            }
        }
    }

    if (showTaskSelectorDropdown) {
        Dialog(onDismissRequest = { showTaskSelectorDropdown = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, LightMintGreen),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "SELECT FOCUS TASK",
                        style = Typography.labelLarge,
                        color = LightMintGreen,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (incompleteTasks.isEmpty()) {
                        Text(
                            "No tasks logged. Complete focus standalone or create a task in PLANNER.",
                            style = Typography.bodyMedium,
                            color = MutedText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showTaskSelectorDropdown = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                        ) {
                            Text("OK", style = Typography.labelSmall)
                        }
                    } else {
                        LazyColumn(modifier = Modifier.height(200.dp)) {
                            items(incompleteTasks) { task ->
                                Text(
                                    text = task.title,
                                    style = Typography.bodyLarge,
                                    color = WhiteText,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSelectedFocusTask(task)
                                            showTaskSelectorDropdown = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 8.dp)
                                )
                                Divider(color = AccentGreen.copy(alpha = 0.5f))
                            }
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "SYSTEM CONFIG CONTROL PANEL",
                style = Typography.labelLarge,
                color = LightMintGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Modify baseline variables, target parameters, or wipe state.",
                style = Typography.bodyMedium,
                color = MutedText
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, AccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("IDENTITY MOTIVATIONAL PRESET", style = Typography.labelMedium, color = LightMintGreen)

                    motivationQuotes.forEach { q ->
                        val isSelected = settings.selectedQuotePreset == q
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateQuotePreset(q) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(q, style = Typography.bodyLarge, color = if (isSelected) LightMintGreen else WhiteText)
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.updateQuotePreset(q) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = LightMintGreen,
                                    unselectedColor = MutedText
                                )
                            )
                        }
                        Divider(color = AccentGreen.copy(alpha = 0.3f))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, AccentGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("TARGET STUDY DEADLINE", style = Typography.labelMedium, color = LightMintGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Set targeted study dates countdown metrics.",
                        style = Typography.bodyMedium,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(12.dp))

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
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LightMintGreen,
                                unfocusedBorderColor = AccentGreen,
                                focusedTextColor = WhiteText,
                                unfocusedTextColor = WhiteText
                            ),
                            placeholder = { Text("YYYY-MM-DD", color = MutedText) }
                        )
                        Button(
                            onClick = { viewModel.updateExamTargetDate(targetInput) },
                            colors = ButtonDefaults.buttonColors(containerColor = LightMintGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("SAVE", style = Typography.labelSmall, color = DarkForestGreen)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("RESET ALL DATA", style = Typography.labelMedium, color = DangerRed)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Deletes all recorded app data including journal logs, planned tasks, and focus timers.",
                        style = Typography.bodyMedium,
                        color = MutedText
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showWipeConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("RESET ALL APP DATA", style = Typography.labelSmall, color = WhiteText)
                    }
                }
            }
        }
    }

    if (showWipeConfirm) {
        Dialog(onDismissRequest = { showWipeConfirm = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkForestGreen),
                border = BorderStroke(1.dp, DangerRed),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("CONFIRM RESET", style = Typography.labelLarge, color = DangerRed)
                    Text(
                        "This irreversible action resets Tasks, Journal entries, Focus times, and clears the 90-day progress map.",
                        style = Typography.bodyMedium,
                        color = WhiteText,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = { showWipeConfirm = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("CANCEL", style = Typography.labelSmall, color = MutedText)
                        }
                        Button(
                            onClick = {
                                viewModel.resetAllData()
                                showWipeConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("PURGE", style = Typography.labelSmall, color = WhiteText)
                        }
                    }
                }
            }
        }
    }
}
