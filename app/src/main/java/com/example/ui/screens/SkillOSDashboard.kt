@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.entity.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillOSViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillOSDashboard(
    viewModel: SkillOSViewModel,
    modifier: Modifier = Modifier
) {
    // Collect Room database resources
    val plans by viewModel.plans.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val topics by viewModel.topics.collectAsStateWithLifecycle()
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val activeTimer by viewModel.activeTimer.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerDisplaySeconds.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf("home") } // "home", "focus", "subjects", "analytics"

    // Sub-screeen stack state definitions
    var activeTopicForDetails by remember { mutableStateOf<Topic?>(null) }
    var selectedCategoryIdSelectionForQuickStart by remember { mutableStateOf<Int?>(null) }
    var selectedSubjectIdSelectionForQuickStart by remember { mutableStateOf<Int?>(null) }

    // Dialog state controllers
    var showAddPlanDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf<String?>(null) } // "Daily","Weekly","Monthly","Plan"

    // Scaffold with full dynamic light/dark safe areas
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ColorBackground),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(SkillSage),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "BK",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "SkillOS",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = SkillNightTime,
                                    lineHeight = 22.sp
                                )
                                Text(
                                    text = "DEVELOPER MODE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = SkillMutedEggplant.copy(alpha = 0.7f),
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                        // Hot Streak indicator
                        val streakCount = viewModel.calculateCurrentStreak()
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .shadow(elevation = 1.dp, shape = RoundedCornerShape(50))
                                .background(ColorCard)
                                .border(1.dp, ColorSecondaryBg, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔥",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streakCount Day Streak",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = SkillNightTime
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBackground,
                    titleContentColor = SkillNightTime
                )
            )
        },
        bottomBar = {
            Surface(
                color = ColorCard,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skillos_bottom_nav"),
                border = BorderStroke(1.dp, ColorSecondaryBg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item 1: Dashboard
                    val homeSelected = currentTab == "home"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                activeTopicForDetails = null
                                currentTab = "home"
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Dashboard",
                            tint = if (homeSelected) SkillTeal else SkillMutedEggplant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dashboard",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (homeSelected) SkillTeal else SkillMutedEggplant.copy(alpha = 0.4f)
                        )
                    }

                    // Item 2: Growth (Subjects)
                    val subjectsSelected = currentTab == "subjects"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                activeTopicForDetails = null
                                currentTab = "subjects"
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Growth",
                            tint = if (subjectsSelected) SkillTeal else SkillMutedEggplant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Growth",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (subjectsSelected) SkillTeal else SkillMutedEggplant.copy(alpha = 0.4f)
                        )
                    }

                    // Item 3: Center FAB timer action
                    val timerSelected = currentTab == "focus" || activeTimer != null
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(elevation = 6.dp, shape = RoundedCornerShape(50))
                            .background(SkillNightTime, RoundedCornerShape(50))
                            .clickable {
                                activeTopicForDetails = null
                                currentTab = "focus"
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Focus Timer",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        if (activeTimer != null) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SkillPoppy)
                                    .align(Alignment.TopEnd)
                            )
                        }
                    }

                    // Item 4: Analytics
                    val analyticsSelected = currentTab == "analytics"
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                activeTopicForDetails = null
                                currentTab = "analytics"
                            }
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = "Analytics",
                            tint = if (analyticsSelected) SkillTeal else SkillMutedEggplant.copy(alpha = 0.4f),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Analytics",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (analyticsSelected) SkillTeal else SkillMutedEggplant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorBackground)
                .padding(innerPadding)
        ) {
            // Route dispatcher
            AnimatedContent(
                targetState = if (activeTopicForDetails != null) "topic_details" else currentTab,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "PaneTransition"
            ) { stateRoute ->
                when (stateRoute) {
                    "topic_details" -> {
                        activeTopicForDetails?.let { topicObj ->
                            TopicDetailPane(
                                topic = topicObj,
                                viewModel = viewModel,
                                activeTimer = activeTimer,
                                onBack = { activeTopicForDetails = null },
                                onStartSession = {
                                    viewModel.startSession(topicObj.id)
                                    activeTopicForDetails = null
                                    currentTab = "focus"
                                }
                            )
                        }
                    }
                    "home" -> {
                        HomeScreenLayout(
                            viewModel = viewModel,
                            plans = plans,
                            categories = categories,
                            subjects = subjects,
                            topics = topics,
                            sessions = sessions,
                            activeTimer = activeTimer,
                            selectedCatId = selectedCategoryIdSelectionForQuickStart,
                            selectedSubId = selectedSubjectIdSelectionForQuickStart,
                            onCatSelect = { selectedCategoryIdSelectionForQuickStart = it },
                            onSubSelect = { selectedSubjectIdSelectionForQuickStart = it },
                            onTopicSelect = { topicId ->
                                viewModel.startSession(topicId)
                                selectedCategoryIdSelectionForQuickStart = null
                                selectedSubjectIdSelectionForQuickStart = null
                                currentTab = "focus"
                            },
                            onAddPlanClick = { showAddPlanDialog = true },
                            onAddCategoryClick = { showAddCategoryDialog = true },
                            onGenerateReport = { showReportDialog = it }
                        )
                    }
                    "focus" -> {
                        ActiveFocusWorkspace(
                            viewModel = viewModel,
                            activeTimer = activeTimer,
                            timerSeconds = timerSeconds,
                            topics = topics,
                            subjects = subjects,
                            categories = categories,
                            onExploreTopics = { currentTab = "subjects" }
                        )
                    }
                    "subjects" -> {
                        SubjectsManagerPane(
                            viewModel = viewModel,
                            categories = categories,
                            subjects = subjects,
                            topics = topics,
                            goals = goals,
                            onTopicClick = { activeTopicForDetails = it },
                            onAddSubjectClick = { showAddSubjectDialog = true },
                            onAddTopicClick = { showAddTopicDialog = true },
                            onAddGoalClick = { showAddGoalDialog = true }
                        )
                    }
                    "analytics" -> {
                        AnalyticsDashboardTab(
                            viewModel = viewModel,
                            categories = categories,
                            subjects = subjects,
                            topics = topics,
                            sessions = sessions
                        )
                    }
                }
            }
        }
    }

    // --- FORM DIALOG TRIGGERS ---

    if (showAddPlanDialog) {
        AddPlanDialog(
            onDismiss = { showAddPlanDialog = false },
            onSave = { name, durationDays ->
                val now = System.currentTimeMillis()
                val delta = durationDays * 24L * 60L * 60L * 1000L
                viewModel.addPlan(name, now, now + delta)
                showAddPlanDialog = false
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, hexColor ->
                viewModel.addCategory(name, hexColor)
                showAddCategoryDialog = false
            }
        )
    }

    if (showAddSubjectDialog) {
        AddSubjectDialog(
            categories = categories,
            onDismiss = { showAddSubjectDialog = false },
            onSave = { name, catId, icon ->
                viewModel.addSubject(name, catId, icon)
                showAddSubjectDialog = false
            }
        )
    }

    if (showAddTopicDialog) {
        AddTopicDialog(
            subjects = subjects,
            onDismiss = { showAddTopicDialog = false },
            onSave = { name, subId, desc ->
                viewModel.addTopic(name, subId, desc)
                showAddTopicDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            subjects = subjects,
            onDismiss = { showAddGoalDialog = false },
            onSave = { subId, targetMins ->
                viewModel.addGoal(subId, targetMins)
                showAddGoalDialog = false
            }
        )
    }

    showReportDialog?.let { reportType ->
        PerformanceReportDialog(
            reportType = reportType,
            viewModel = viewModel,
            onDismiss = { showReportDialog = null }
        )
    }
}

// --- HOME SCREEN LAYOUT ---
@Composable
fun HomeScreenLayout(
    viewModel: SkillOSViewModel,
    plans: List<Plan>,
    categories: List<Category>,
    subjects: List<Subject>,
    topics: List<Topic>,
    sessions: List<Session>,
    activeTimer: ActiveTimer?,
    selectedCatId: Int?,
    selectedSubId: Int?,
    onCatSelect: (Int?) -> Unit,
    onSubSelect: (Int?) -> Unit,
    onTopicSelect: (Int) -> Unit,
    onAddPlanClick: () -> Unit,
    onAddCategoryClick: () -> Unit,
    onGenerateReport: (String) -> Unit
) {
    val nonDeletedPlans = plans.filter { !it.isDeleted }
    val activePlan = nonDeletedPlans.find { !it.isCompleted } ?: nonDeletedPlans.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and streak context
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("welcome_streak_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ColorCard),
                border = BorderStroke(1.dp, ColorSecondaryBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ACTIVE PLAN",
                            fontSize = 10.sp,
                            color = SkillMutedEggplant,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activePlan?.name ?: "No Active Plan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SkillNightTime,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (activePlan != null) {
                        val currentMs = System.currentTimeMillis()
                        val diffMs = activePlan.endDate - activePlan.startDate
                        val elapsedMs = maxOf(0L, currentMs - activePlan.startDate)
                        val totalDays = maxOf(1L, TimeUnit.MILLISECONDS.toDays(diffMs))
                        val elapsedDays = min(totalDays, TimeUnit.MILLISECONDS.toDays(elapsedMs) + 1)
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "Day $elapsedDays",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SkillTeal
                            )
                            Text(
                                text = "of $totalDays",
                                fontSize = 10.sp,
                                color = SkillMutedEggplant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Button(
                            onClick = onAddPlanClick,
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
                        ) {
                            Text("New Plan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Mid section: Scrollable Week Calendar
        item {
            WeekCalendarWidget()
        }

        // Today Summary Card
        item {
            TodaySummaryCard(sessions, viewModel.goals.value)
        }

        // Core 3-TAP Quick Start selection drawer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_start_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SkillNightTime),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bolt Quick Timer",
                            tint = SkillDaffodil,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "QUICK START FOCUS",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (activeTimer != null) {
                        Text(
                            text = "⚠️ An active session is currently running! Navigate to Focus tab to supervise.",
                            color = SkillPeach,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Interactive 3-Tap Selection
                        AnimatedVisibility(visible = selectedCatId == null) {
                            Column {
                                Text(
                                    text = "Step 1: Pick Category",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val safeCats = categories.filter { !it.isDeleted }
                                    if (safeCats.isEmpty()) {
                                        Text("No categories found. Add one below!", color = Color.White.copy(0.5f), fontSize = 12.sp)
                                    }
                                    safeCats.forEach { cat ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(Color(android.graphics.Color.parseColor(cat.color)))
                                                .clickable { onCatSelect(cat.id) }
                                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = cat.name,
                                                color = SkillNightTime,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = selectedCatId != null && selectedSubId == null) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val cat = categories.find { it.id == selectedCatId }
                                    Text(
                                        text = "Step 2: Choose Subject (${cat?.name})",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    TextButton(onClick = { onCatSelect(null) }) {
                                        Text("Back", color = SkillSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val filteredSubs = subjects.filter { it.categoryId == selectedCatId && !it.isDeleted }
                                if (filteredSubs.isEmpty()) {
                                    Text("No subjects found in this Category. Set one up in the Growth tab!", color = Color.White.copy(0.5f), fontSize = 12.sp)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        filteredSubs.forEach { sub ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color.White.copy(alpha = 0.1f))
                                                    .clickable { onSubSelect(sub.id) }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = "Subject Icon",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = sub.name,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = selectedCatId != null && selectedSubId != null) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val sub = subjects.find { it.id == selectedSubId }
                                    Text(
                                        text = "Step 3: Track Topic in ${sub?.name}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 12.sp
                                    )
                                    TextButton(onClick = { onSubSelect(null) }) {
                                        Text("Back", color = SkillSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                val filteredTopics = topics.filter { it.subjectId == selectedSubId && !it.isCompleted && !it.isDeleted }
                                if (filteredTopics.isEmpty()) {
                                    Text("All topics completed or empty! Create topics in the Growth tab.", color = Color.White.copy(0.5f), fontSize = 12.sp)
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        filteredTopics.forEach { topic ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(SkillTeal.copy(alpha = 0.15f))
                                                    .border(1.dp, SkillTeal.copy(0.3f), RoundedCornerShape(12.dp))
                                                    .clickable { onTopicSelect(topic.id) }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = topic.name,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    if (topic.description.isNotBlank()) {
                                                        Text(
                                                            text = topic.description,
                                                            color = Color.White.copy(0.7f),
                                                            fontSize = 11.sp,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                                Icon(
                                                    imageVector = Icons.Default.PlayArrow,
                                                    contentDescription = "Start Timer",
                                                    tint = SkillTeal,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Neglect Warnings Area
        val neglectWarnings = viewModel.getNeglectWarnings()
        if (neglectWarnings.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NEGLECT WARNINGS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SkillNightTime,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = Icons.Default.NotificationImportant,
                            contentDescription = "Warnings",
                            tint = SkillPoppy,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        neglectWarnings.take(3).forEach { warning ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SkillPoppy.copy(alpha = 0.1f))
                                    .border(1.dp, SkillPoppy.copy(0.2f), RoundedCornerShape(16.dp))
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning sign Indicator",
                                    tint = SkillPoppy,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${warning.name} inactive for ${warning.idleDays} days",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SkillNightTime
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Overview Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CATEGORY INSIGHTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillNightTime,
                    letterSpacing = 1.sp
                )
                TextButton(onClick = onAddCategoryClick) {
                    Text("+ Category", color = SkillTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        val nonDeletedCats = categories.filter { !it.isDeleted }
        if (nonDeletedCats.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No categories added yet.", color = SkillSage, fontSize = 13.sp)
                }
            }
        } else {
            items(nonDeletedCats.chunked(2)) { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { cat ->
                        CategoryInsightCard(
                            category = cat,
                            sessions = sessions,
                            topics = topics,
                            subjects = subjects,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // On-demand performance report dispatcher
        item {
            Text(
                text = "ON-DEMAND AUDIT REPORTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillNightTime,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Daily", "Weekly", "Monthly", "Plan").forEach { report ->
                    Button(
                        onClick = { onGenerateReport(report) },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorSecondaryBg),
                        shape = RoundedCornerShape(50)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Summarize,
                            contentDescription = "Summaries",
                            tint = SkillMutedEggplant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "$report Report", color = SkillNightTime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- CALENDAR SCROLLABLE CONTAINER ---
@Composable
fun WeekCalendarWidget() {
    val localZoneName = ZoneId.systemDefault()
    val today = LocalDate.now(localZoneName)
    val formatter = DateTimeFormatter.ofPattern("E")
    val numFormatter = DateTimeFormatter.ofPattern("d")

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "TIMELINE WEEK",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SkillNightTime,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in -3..3) {
                val date = today.plusDays(i.toLong())
                val isToday = date == today
                val isFuture = date.isAfter(today)
                val dayOfWeek = date.format(formatter)
                val dayOfMonth = date.format(numFormatter)

                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                        .then(if (isFuture) Modifier.alpha(0.4f) else Modifier),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayOfWeek.take(1), // M, T, W, T, F, etc.
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillMutedEggplant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .then(
                                if (isToday) {
                                    Modifier
                                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(50))
                                        .background(SkillTeal, RoundedCornerShape(50))
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayOfMonth,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else SkillNightTime
                        )
                    }
                }
            }
        }
    }
}

// --- TODAY SUMMARY CARD ---
@Composable
fun TodaySummaryCard(sessions: List<Session>, goals: List<Goal>) {
    val localZoneSelection = ZoneId.systemDefault()
    val today = LocalDate.now(localZoneSelection)

    val todaySessionMins = sessions.filter {
        !it.isDeleted && Instant.ofEpochMilli(it.startTime).atZone(localZoneSelection).toLocalDate() == today
    }.sumOf { it.durationMinutes }

    // Calculate weekly average goals daily mapping (Total target weekly mins / 7)
    val totalWeeklyTargetMins = goals.sumOf { it.targetMinutes }
    val todayTargetMins = if (totalWeeklyTargetMins > 0) totalWeeklyTargetMins / 7 else 120 // defaults to 2 hours if no goals

    val progress = if (todayTargetMins > 0) todaySessionMins.toFloat() / todayTargetMins.toFloat() else 0f
    val percentage = min(100, (progress * 100f).toInt())

    val remainingMins = maxOf(0, todayTargetMins - todaySessionMins)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("today_summary_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCard),
        border = BorderStroke(1.dp, ColorSecondaryBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    val hrs = todaySessionMins / 60
                    val mins = todaySessionMins % 60
                    Text(
                        text = if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillNightTime,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Invested today",
                        fontSize = 12.sp,
                        color = SkillMutedEggplant,
                        fontWeight = FontWeight.Medium
                    )
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(48.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = ColorSecondaryBg,
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = SkillSkyBlue,
                            startAngle = -90f,
                            sweepAngle = min(1f, progress) * 360f,
                            useCenter = false,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Text(
                        text = "$percentage%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillSkyBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = min(1.0f, progress),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SkillSkyBlue,
                trackColor = ColorSecondaryBg
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val targetHrs = todayTargetMins / 60
                val targetMins = todayTargetMins % 60
                val plannedText = if (targetHrs > 0) {
                    if (targetMins > 0) "Planned: ${targetHrs}h ${targetMins}m" else "Planned: ${targetHrs}h"
                } else "Planned: ${targetMins}m"

                val remHrs = remainingMins / 60
                val remMins = remainingMins % 60
                val remainingText = if (remainingMins > 0) {
                    if (remHrs > 0) {
                        if (remMins > 0) "Remaining: ${remHrs}h ${remMins}m" else "Remaining: ${remHrs}h"
                    } else "Remaining: ${remMins}m"
                } else "Remaining: 0m"

                Text(
                    text = plannedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillMutedEggplant.copy(alpha = 0.5f)
                )
                Text(
                    text = remainingText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillMutedEggplant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

// --- CATEGORY INSIGHT CARD ---
@Composable
fun CategoryInsightCard(
    category: Category,
    sessions: List<Session>,
    topics: List<Topic>,
    subjects: List<Subject>,
    modifier: Modifier = Modifier
) {
    val catColor = Color(android.graphics.Color.parseColor(category.color))

    // Calculate Category level stats
    val catSubjectsIds = subjects.filter { it.categoryId == category.id && !it.isDeleted }.map { it.id }
    val catTopicsIds = topics.filter { it.subjectId in catSubjectsIds && !it.isDeleted }.map { it.id }
    val catSessions = sessions.filter { it.topicId in catTopicsIds && !it.isDeleted }

    val totalMins = catSessions.sumOf { it.durationMinutes }
    val completedTopics = topics.filter { it.subjectId in catSubjectsIds && it.isCompleted && !it.isDeleted }.size
    val totalTopics = topics.filter { it.subjectId in catSubjectsIds && !it.isDeleted }.size

    val icon = when (category.name.lowercase()) {
        "learning" -> Icons.Default.School
        "projects" -> Icons.Default.Bolt
        "career" -> Icons.Default.Work
        "freelancing" -> Icons.Default.AttachMoney
        else -> Icons.Default.Star
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = catColor.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, catColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(catColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = category.name,
                    color = catColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val hrs = totalMins / 60
                val mins = totalMins % 60
                Text(
                    text = if (hrs > 0) "${hrs}h Invested" else "${mins}m Invested",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillNightTime
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedTopics / $totalTopics completed",
                    fontSize = 10.sp,
                    color = SkillMutedEggplant.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- ACTIVE FOCUS SCREEN WORKSPACE ---
@Composable
fun ActiveFocusWorkspace(
    viewModel: SkillOSViewModel,
    activeTimer: ActiveTimer?,
    timerSeconds: Long,
    topics: List<Topic>,
    subjects: List<Subject>,
    categories: List<Category>,
    onExploreTopics: () -> Unit
) {
    if (activeTimer == null) {
        // Empty State: instructions to spin a timer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.HourglassEmpty,
                contentDescription = "No timer running",
                tint = SkillSage,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Workspace Idle",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SkillNightTime
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Use the Quick Start widget in Today, or tap any Topic in the Growth pane to spawn a trackable UTC session.",
                fontSize = 14.sp,
                color = SkillSage,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onExploreTopics,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text(text = "Navigate to Growth Hub", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    } else {
        val topic = topics.find { it.id == activeTimer.topicId }
        val subject = if (topic != null) subjects.find { it.id == topic.subjectId } else null
        val category = if (subject != null) categories.find { it.id == subject.categoryId } else null
        val catColor = category?.color?.let { Color(android.graphics.Color.parseColor(it)) } ?: SkillTeal

        var showSaveCompletedDialog by remember { mutableStateOf(false) }

        var elapsedSeconds by remember(activeTimer) {
            val initialMs = if (activeTimer != null) {
                val now = System.currentTimeMillis()
                if (activeTimer.isPaused) {
                    activeTimer.lastPauseTimestamp - activeTimer.startTime - activeTimer.pausedDurationMs
                } else {
                    val pausedDiff = if (activeTimer.lastPauseTimestamp > 0) {
                        now - activeTimer.lastPauseTimestamp
                    } else 0L
                    now - activeTimer.startTime - activeTimer.pausedDurationMs - pausedDiff
                }
            } else 0L
            mutableStateOf(maxOf(0L, initialMs / 1000L))
        }

        LaunchedEffect(activeTimer, activeTimer?.isPaused) {
            if (activeTimer != null) {
                while (true) {
                    val now = System.currentTimeMillis()
                    val totalMs = if (activeTimer.isPaused) {
                        activeTimer.lastPauseTimestamp - activeTimer.startTime - activeTimer.pausedDurationMs
                    } else {
                        val pausedDiff = if (activeTimer.lastPauseTimestamp > 0) {
                            now - activeTimer.lastPauseTimestamp
                        } else 0L
                        now - activeTimer.startTime - activeTimer.pausedDurationMs - pausedDiff
                    }
                    elapsedSeconds = maxOf(0L, totalMs / 1000L)
                    kotlinx.coroutines.delay(1000L)
                }
            } else {
                elapsedSeconds = 0L
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task context headers
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(catColor.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = category?.name ?: "Focus Area",
                        color = SkillNightTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = topic?.name ?: "Unknown Topic",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillNightTime,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subject?.name ?: "Subject Class",
                    fontSize = 14.sp,
                    color = SkillSage,
                    fontWeight = FontWeight.Bold
                )
            }

            // High priority radial timer UI
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(240.dp)
            ) {
                // Background tracking arc
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = ColorSecondaryBg,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                    // Animated ticking sweep
                    val sweepProgress = (elapsedSeconds % 60) / 60f
                    drawArc(
                        color = catColor,
                        startAngle = -90f,
                        sweepAngle = sweepProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val displayHrs = elapsedSeconds / 3600
                    val displayMins = (elapsedSeconds % 3600) / 60
                    val displaySecs = elapsedSeconds % 60
                    val timeString = if (displayHrs > 0) {
                        String.format("%02d:%02d:%02d", displayHrs, displayMins, displaySecs)
                    } else {
                        String.format("%02d:%02d", displayMins, displaySecs)
                    }

                    Text(
                        text = timeString,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkillNightTime,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (activeTimer.isPaused) "PAUSED" else "FOCUS ACTIVE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (activeTimer.isPaused) SkillPoppy else SkillTeal,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Command Control pills
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.cancelActiveSession() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(50))
                            .background(ColorSecondaryBg)
                            .testTag("cancel_timer_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Discard Focus Timer", tint = SkillNightTime)
                    }

                    if (activeTimer.isPaused) {
                        Button(
                            onClick = { viewModel.resumeSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillTeal),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(56.dp)
                                .width(130.dp)
                                .testTag("resume_timer_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseSession() },
                            colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .height(56.dp)
                                .width(130.dp)
                                .testTag("pause_timer_button")
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pause", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    IconButton(
                        onClick = { showSaveCompletedDialog = true },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(50))
                            .background(SkillSkyBlue)
                            .testTag("complete_timer_button")
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Settle/Save Focus Timer", tint = SkillNightTime)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        if (showSaveCompletedDialog) {
            var checkCompletedByHand by remember { mutableStateOf(false) }
            AlertDialog(
                onDismissRequest = { showSaveCompletedDialog = false },
                title = { Text("Seal Study Session", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        val minutesValue = maxOf(1, (timerSeconds / 60L).toInt())
                        Text(
                            text = "Amazing work! This session completes standard verification. We'll record $minutesValue minute(s) of active attention.",
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { checkCompletedByHand = !checkCompletedByHand }
                        ) {
                            Checkbox(checked = checkCompletedByHand, onCheckedChange = { checkCompletedByHand = it })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark Topic as Completed", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.completeActiveSession(markTopicCompleted = checkCompletedByHand)
                            showSaveCompletedDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
                    ) {
                        Text("Log UTC Session", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveCompletedDialog = false }) {
                        Text("Resume Ticking", color = SkillSage, fontWeight = FontWeight.Bold)
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = ColorCard
            )
        }
    }
}

// --- SUBJECTS & GROWTH HUB MANAGEMENT ---
@Composable
fun SubjectsManagerPane(
    viewModel: SkillOSViewModel,
    categories: List<Category>,
    subjects: List<Subject>,
    topics: List<Topic>,
    goals: List<Goal>,
    onTopicClick: (Topic) -> Unit,
    onAddSubjectClick: () -> Unit,
    onAddTopicClick: () -> Unit,
    onAddGoalClick: () -> Unit
) {
    // Subject filter categories tabs
    val nonDeletedCats = categories.filter { !it.isDeleted }
    var selectedCatTabId by remember { mutableStateOf<Int?>(null) }
    if (selectedCatTabId == null && nonDeletedCats.isNotEmpty()) {
        selectedCatTabId = nonDeletedCats.first().id
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Headers + add items buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GROWTH HUB",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillNightTime,
                letterSpacing = 1.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onAddSubjectClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ColorSecondaryBg)
                ) {
                    Icon(imageVector = Icons.Default.LibraryAdd, contentDescription = "Add Subject", tint = SkillNightTime, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onAddTopicClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ColorSecondaryBg)
                ) {
                    Icon(imageVector = Icons.Default.AddTask, contentDescription = "Add Topic", tint = SkillNightTime, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onAddGoalClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(50))
                        .background(ColorSecondaryBg)
                ) {
                    Icon(imageVector = Icons.Default.OutlinedFlag, contentDescription = "Add Goal", tint = SkillNightTime, modifier = Modifier.size(16.dp))
                }
            }
        }

        // Categories Top tab pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            nonDeletedCats.forEach { cat ->
                val isSelected = cat.id == selectedCatTabId
                val catColor = Color(android.graphics.Color.parseColor(cat.color))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (isSelected) SkillNightTime else ColorCard)
                        .border(
                            1.dp,
                            if (isSelected) Color.Transparent else ColorSecondaryBg,
                            RoundedCornerShape(50)
                        )
                        .clickable { selectedCatTabId = cat.id }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(catColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = cat.name,
                            color = if (isSelected) Color.White else SkillNightTime,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Large subjects scrolling column list
        val filteredSubs = subjects.filter { it.categoryId == selectedCatTabId && !it.isDeleted }
        if (filteredSubs.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = "Empty Folder indicator",
                    tint = SkillSage,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text("No subjects under this Category.", color = SkillSage, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredSubs) { subject ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("subject_card_${subject.id}"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // Subject Title
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmarks,
                                        contentDescription = "Subject Icon",
                                        tint = SkillTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = subject.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = SkillNightTime
                                    )
                                }

                                // Delete Button
                                IconButton(onClick = { viewModel.softDeleteSubject(subject.id) }) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Trash delete icon subject", tint = SkillPoppy, modifier = Modifier.size(18.dp))
                                }
                            }

                            // Goal stats if any exists
                            val subGoal = goals.find { it.subjectId == subject.id }
                            if (subGoal != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SkillDaffodil.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Goals", tint = SkillNightTime, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Weekly Goal: ${subGoal.targetMinutes / 60} hours", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SkillNightTime)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.RemoveCircleOutline,
                                        contentDescription = "Remove goal mark",
                                        tint = SkillPoppy,
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clickable { viewModel.deleteGoal(subGoal.id) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = ColorSecondaryBg)
                            Spacer(modifier = Modifier.height(10.dp))

                            // Topics list inside this subject
                            val subTopics = topics.filter { it.subjectId == subject.id && !it.isDeleted }
                            if (subTopics.isEmpty()) {
                                Text(
                                    text = "No topics added. Tap the add topic button above to insert first deep topic focus.",
                                    fontSize = 12.sp,
                                    color = SkillSage
                                )
                            } else {
                                subTopics.forEach { topic ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (topic.isCompleted) ColorSecondaryBg else Color.Transparent)
                                            .clickable { onTopicClick(topic) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            // Checkbox mark topic complete directly
                                            Checkbox(
                                                checked = topic.isCompleted,
                                                onCheckedChange = { isChecked ->
                                                    viewModel.setTopicCompleted(topic.id, isChecked)
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Column {
                                                Text(
                                                    text = topic.name,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 14.sp,
                                                    color = if (topic.isCompleted) SkillSage else SkillNightTime,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (topic.description.isNotBlank()) {
                                                    Text(
                                                        text = topic.description,
                                                        fontSize = 11.sp,
                                                        color = SkillSage,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Icon(
                                            imageVector = Icons.Default.ChevronRight,
                                            contentDescription = "Inspect Details",
                                            tint = SkillSage,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

// --- TOPIC DETAIL DETAIL PANE (RICH NOTES & RESOURCES CRUD) ---
@Composable
fun TopicDetailPane(
    topic: Topic,
    viewModel: SkillOSViewModel,
    activeTimer: ActiveTimer?,
    onBack: () -> Unit,
    onStartSession: () -> Unit
) {
    var noteContent by remember { mutableStateOf("") }
    var originalNoteObj by remember { mutableStateOf<Note?>(null) }
    val resources by viewModel.getResourcesFlowForTopic(topic.id).collectAsStateWithLifecycle(emptyList())

    // Collect note
    LaunchedEffect(topic.id) {
        viewModel.getNoteFlowForTopic(topic.id).collect { note ->
            originalNoteObj = note
            noteContent = note?.content ?: ""
        }
    }

    // Modal state
    var showAddResourceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Topic details header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back icon button details", tint = SkillNightTime)
            }
            Text(
                text = "TOPIC CONTROL",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillNightTime,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = { viewModel.softDeleteTopic(topic.id); onBack() }
            ) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete topic", tint = SkillPoppy)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ColorCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = topic.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillNightTime
                )
                if (topic.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = topic.description,
                        fontSize = 14.sp,
                        color = SkillSage,
                        lineHeight = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = topic.isCompleted,
                            onCheckedChange = { viewModel.setTopicCompleted(topic.id, it) }
                        )
                        Text(
                            text = if (topic.isCompleted) "Completed" else "In Progress",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (topic.isCompleted) SkillTeal else SkillNightTime
                        )
                    }

                    if (activeTimer == null) {
                        Button(
                            onClick = onStartSession,
                            colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Focus Now", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("⚠️ Timer active elsewhere", color = SkillSage, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Resources CRUD Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STUDY RESOURCES",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillNightTime,
                letterSpacing = 1.sp
            )
            TextButton(onClick = { showAddResourceDialog = true }) {
                Text("+ Link Resource", color = SkillTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (resources.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ColorCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No references logged for this topic yet.", color = SkillSage, fontSize = 13.sp)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                resources.forEach { resource ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ColorCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = when (resource.type) {
                                        "YouTube" -> Icons.Default.VideoLibrary
                                        "Docs" -> Icons.Default.Article
                                        "GitHub" -> Icons.Default.Source
                                        else -> Icons.Default.Link
                                    },
                                    contentDescription = "Resource type icon indicator",
                                    tint = SkillTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = resource.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = SkillNightTime
                                    )
                                    Text(
                                        text = resource.url,
                                        fontSize = 11.sp,
                                        color = SkillSage,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Row {
                                val context = LocalContext.current
                                // Launch custom share/url redirect
                                IconButton(
                                    onClick = {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(resource.url))
                                        try { context.startActivity(intent) } catch (e: Exception) {}
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open resource Link", tint = SkillSage, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { viewModel.softDeleteResource(resource.id) }) {
                                    Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete resource Link", tint = SkillPoppy, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notes and rich saving panel
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AUTO-SAVING FOCUS NOTES",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SkillNightTime,
                letterSpacing = 1.sp
            )
            Button(
                onClick = {
                    viewModel.saveNote(topic.id, originalNoteObj?.id ?: 0, noteContent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime),
                shape = RoundedCornerShape(50),
                modifier = Modifier.height(32.dp)
            ) {
                Text("Save Notes", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = noteContent,
            onValueChange = {
                noteContent = it
            },
            placeholder = { Text("Log dynamic study logs, markdown files, codes snippets here...", color = SkillSage) },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ColorCard,
                unfocusedContainerColor = ColorCard,
                focusedBorderColor = SkillNightTime,
                unfocusedBorderColor = ColorSecondaryBg
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showAddResourceDialog) {
        AddResourceDialog(
            onDismiss = { showAddResourceDialog = false },
            onSave = { title, url, type ->
                viewModel.addResource(topic.id, title, url, type)
                showAddResourceDialog = false
            }
        )
    }
}

// --- ANALYTICS DASHBOARD TAB ---
@Composable
fun AnalyticsDashboardTab(
    viewModel: SkillOSViewModel,
    categories: List<Category>,
    subjects: List<Subject>,
    topics: List<Topic>,
    sessions: List<Session>
) {
    var heatmapFilterType by remember { mutableStateOf("Overall") } // "Overall", "Category", "Subject"
    var heatmapFilterId by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Headers overview
        Text(
            text = "ANALYTICS ENGINE",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SkillNightTime,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Dropdown Heatmap filter selection Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = ColorCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Contribution Map filter",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillSage
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Type Selector Filter button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(50))
                            .background(ColorSecondaryBg)
                            .clickable {
                                heatmapFilterType = when (heatmapFilterType) {
                                    "Overall" -> "Category"
                                    "Category" -> "Subject"
                                    else -> "Overall"
                                }
                                heatmapFilterId = 0
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Scope: $heatmapFilterType", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkillNightTime)
                    }

                    // Object Target Selector if not overall
                    if (heatmapFilterType != "Overall") {
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .clip(RoundedCornerShape(50))
                                .background(ColorSecondaryBg)
                                .clickable {
                                    if (heatmapFilterType == "Category") {
                                        val nonDelCats = categories.filter { !it.isDeleted }
                                        if (nonDelCats.isNotEmpty()) {
                                            val currentIdx = nonDelCats.indexOfFirst { it.id == heatmapFilterId }
                                            val nextIndex = (currentIdx + 1) % nonDelCats.size
                                            heatmapFilterId = nonDelCats[nextIndex].id
                                        }
                                    } else {
                                        val nonDelSubs = subjects.filter { !it.isDeleted }
                                        if (nonDelSubs.isNotEmpty()) {
                                            val currentIdx = nonDelSubs.indexOfFirst { it.id == heatmapFilterId }
                                            val nextIndex = (currentIdx + 1) % nonDelSubs.size
                                            heatmapFilterId = nonDelSubs[nextIndex].id
                                        }
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val activeName = if (heatmapFilterType == "Category") {
                                categories.find { it.id == heatmapFilterId }?.name ?: "Select Category"
                            } else {
                                subjects.find { it.id == heatmapFilterId }?.name ?: "Select Subject"
                            }
                            Text(text = activeName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SkillNightTime, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // HEATMAP GRID
        val heatmapMinutes = viewModel.getHeatmapData(heatmapFilterType, heatmapFilterId)
        ContributionHeatmapWidget(heatmapMinutes)

        Spacer(modifier = Modifier.height(16.dp))

        // Time distributions donut chart curves (Canvas rendered)
        SmartInsightsSection(viewModel)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- GITHUB CONTRIBUTION HEATMAP WIDGET ---
@Composable
fun ContributionHeatmapWidget(heatmapMins: Map<LocalDate, Int>) {
    val localZone = ZoneId.systemDefault()
    val today = LocalDate.now(localZone)
    // Align starting day to Monday of 14 weeks ago
    val startMonday = today.minusWeeks(13).with(java.time.DayOfWeek.MONDAY)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("heatmap_widget_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = ColorCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FOCUS FREQUENCY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SkillSage,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Past 14 Weeks",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SkillNightTime
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Draw grid in horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Displays 14 columns of weeks
                for (weekIdx in 0..13) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (dayIdx in 0..6) {
                            val coordDate = startMonday.plusWeeks(weekIdx.toLong()).plusDays(dayIdx.toLong())
                            val durationMins = heatmapMins[coordDate] ?: 0

                            val colorBlock = when {
                                durationMins == 0 -> ColorSecondaryBg
                                durationMins < 15 -> SkillSkyBlue.copy(alpha = 0.3f)
                                durationMins < 45 -> SkillSkyBlue.copy(alpha = 0.62f)
                                durationMins < 90 -> SkillTeal
                                else -> SkillNightTime
                            }

                            // Individual daily contribution block
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(colorBlock)
                                    .clickable {
                                        // Silent reveal duration or toast if wanted
                                    }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map intensity key row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Less", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                listOf(0, 10, 40, 75, 120).forEach { mockMins ->
                    val blockColor = when {
                        mockMins == 0 -> ColorSecondaryBg
                        mockMins < 15 -> SkillSkyBlue.copy(alpha = 0.3f)
                        mockMins < 45 -> SkillSkyBlue.copy(alpha = 0.62f)
                        mockMins < 90 -> SkillTeal
                        else -> SkillNightTime
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(blockColor)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Deep", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// --- SMART INSIGHTS SECTION ---
@Composable
fun SmartInsightsSection(viewModel: SkillOSViewModel) {
    val insights = viewModel.getSmartInsights()

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AI INSIGHTS & METRICS",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SkillNightTime,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            insights.forEach { insight ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = ColorCard),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SkillLavender.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (insight.icon) {
                                    "trending_up" -> Icons.Default.TrendingUp
                                    "trending_down" -> Icons.Default.TrendingDown
                                    "warning" -> Icons.Default.Warning
                                    "check_circle" -> Icons.Default.CheckCircle
                                    "local_fire_department" -> Icons.Default.LocalFireDepartment
                                    else -> Icons.Default.Star
                                },
                                contentDescription = "Insight marker",
                                tint = SkillNightTime,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = insight.title.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SkillSage,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insight.value,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SkillNightTime
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = insight.description,
                                fontSize = 12.sp,
                                color = SkillSage,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- FORM DIALOG CORE IMPLEMENTATIONS ---

@Composable
fun AddPlanDialog(onDismiss: () -> Unit, onSave: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(90) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Activate Sprint Plan", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Plan Slogan") },
                    placeholder = { Text("e.g. 90-Day Full Stack Sprint") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Sprint Duration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                val durations = listOf(18, 20, 45, 90, 180)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    durations.forEach { d ->
                        val isSel = d == selectedDays
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) SkillNightTime else ColorSecondaryBg)
                                .clickable { selectedDays = d }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${d}d",
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SkillNightTime,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedDays) },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Launch Sprint", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SkillSage, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}

@Composable
fun AddCategoryDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#DBC0E8") }

    val palette = listOf(
        "#DBC0E8", "#A3C1E2", "#F7E289", "#FBB28B", "#F76F54",
        "#AEB29A", "#EA5E86", "#47B5A8", "#F9A2C5", "#6B515E"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Construct Category", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Map Palette Theme Color", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(palette) { colorHex ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(android.graphics.Color.parseColor(colorHex)))
                                .border(
                                    3.dp,
                                    if (selectedColor == colorHex) SkillNightTime else Color.Transparent,
                                    RoundedCornerShape(50)
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, selectedColor) },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Create Theme", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SkillSage, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}

@Composable
fun AddSubjectDialog(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCatId by remember { mutableStateOf(0) }
    if (selectedCatId == 0 && categories.isNotEmpty()) {
        selectedCatId = categories.first().id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Subject Class", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Domain Name") },
                    placeholder = { Text("e.g. MERN Stack, Resume") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Associate Category", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val isSel = cat.id == selectedCatId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) SkillNightTime else ColorSecondaryBg)
                                .clickable { selectedCatId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat.name,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SkillNightTime,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && selectedCatId != 0) onSave(name, selectedCatId, "book") },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Save Subject", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SkillSage, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}

@Composable
fun AddTopicDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubId by remember { mutableStateOf(0) }
    if (selectedSubId == 0 && subjects.isNotEmpty()) {
        selectedSubId = subjects.first().id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Initiate Topic Trace", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Topic Focus Keyweight") },
                    placeholder = { Text("e.g. React Hooks, DSA sliding window") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short focus brief") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Associate Class Subject", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { sub ->
                        val isSel = sub.id == selectedSubId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) SkillNightTime else ColorSecondaryBg)
                                .clickable { selectedSubId = sub.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sub.name,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SkillNightTime,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank() && selectedSubId != 0) onSave(name, selectedSubId, description) },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Pin Topic", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SkillSage, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}

@Composable
fun AddGoalDialog(
    subjects: List<Subject>,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var targetHours by remember { mutableStateOf("") }
    var selectedSubId by remember { mutableStateOf(0) }
    if (selectedSubId == 0 && subjects.isNotEmpty()) {
        selectedSubId = subjects.first().id
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settle Focus Target Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetHours,
                    onValueChange = { targetHours = it },
                    label = { Text("Weekly scale target hours") },
                    placeholder = { Text("e.g. 10") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Target Subject Class", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    subjects.forEach { sub ->
                        val isSel = sub.id == selectedSubId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) SkillNightTime else ColorSecondaryBg)
                                .clickable { selectedSubId = sub.id }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sub.name,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SkillNightTime,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val hrs = targetHours.toIntOrNull() ?: 0
                    if (hrs > 0 && selectedSubId != 0) onSave(selectedSubId, hrs * 60)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Set Goal Block", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SkillSage, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}

@Composable
fun AddResourceDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("YouTube") }

    val types = listOf("YouTube", "Docs", "GitHub", "Article", "PDF", "Custom")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Link Media Reference", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Reference label title") },
                    placeholder = { Text("YouTube Tutorial, Wiki Doc") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Asset Full URL Address") },
                    placeholder = { Text("https://...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Resource Type Filter", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    types.forEach { type ->
                        val isSel = type == selectedType
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (isSel) SkillNightTime else ColorSecondaryBg)
                                .clickable { selectedType = type }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = type,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else SkillNightTime,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && url.isNotBlank()) onSave(title, url, selectedType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Log Link", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = SkillSage, fontWeight = FontWeight.Bold) }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}

@Composable
fun PerformanceReportDialog(
    reportType: String,
    viewModel: SkillOSViewModel,
    onDismiss: () -> Unit
) {
    val report = viewModel.generateReport(reportType)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Summarize, contentDescription = "Report", tint = SkillTeal)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = report.title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = SkillNightTime)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "A consolidated audit of actual UTC tracked deep learning/work session efforts mapped to local timezone:",
                    fontSize = 13.sp,
                    color = SkillSage
                )

                Divider(color = ColorSecondaryBg)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Hours Invested", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                        Text(text = String.format("%.1f Hours", report.totalHours), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SkillNightTime)
                    }
                    Column {
                        Text("Focused Sessions", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                        Text(text = "${report.sessionCount} session(s)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SkillNightTime)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Active Topics", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                        Text(text = "${report.activeTopicsCount} topic(s)", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SkillNightTime)
                    }
                    Column {
                        Text("Average Sump", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                        Text(text = String.format("%.0fm per run", report.avgSessionMins), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SkillNightTime)
                    }
                }

                Divider(color = ColorSecondaryBg)

                Column {
                    Text("Top Category Focus Zone", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                    Text(text = report.topCategory, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SkillNightTime)
                }

                Column {
                    Text("Top Active Subject Class", fontSize = 11.sp, color = SkillSage, fontWeight = FontWeight.Bold)
                    Text(text = report.topSubject, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SkillNightTime)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = SkillNightTime)
            ) {
                Text("Understood", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = ColorCard
    )
}
