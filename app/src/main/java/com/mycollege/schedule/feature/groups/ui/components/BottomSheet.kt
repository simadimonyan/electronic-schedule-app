package com.mycollege.schedule.feature.groups.ui.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ChipDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FilterChip
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.app.activity.domain.models.LoadingState
import com.mycollege.schedule.feature.groups.ui.state.GroupEvent
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledBlue
import com.mycollege.schedule.shared.ui.theme.disabledLightBlue
import com.mycollege.schedule.shared.ui.theme.disabledWhite
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    loadingState: LoadingState,
    progress: Int,
    groupState: GroupState,
    handleEvent: (GroupEvent) -> Unit,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    cachedGroups: Map<String, Long>,
    cachedTeachers: Map<String, Long>
) {
    val context: Context = LocalContext.current
    val animatedProgress = animateFloatAsState(targetValue = progress / 100f, label = "progress")

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = Modifier.wrapContentHeight()
            .padding(10.dp, 30.dp, 10.dp, 15.dp)
            .imePadding(),
        sheetState = sheetState,
        shape = RoundedCornerShape(17.dp),
        contentColor = Color.White,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black) },
        onDismissRequest = onDismiss
    ) {
        if (loadingState.chooseConfigurationLoading) {
            Text(
                text = context.getString(R.string.update_data),
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 17.sp,
                color = Color.Black
            )

            LinearProgressIndicator(
                progress = { animatedProgress.value },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp, 10.dp),
                color = buttons,
                trackColor = background
            )
            Spacer(modifier = Modifier.height(20.dp))
        } else {

            // отобразить данные по окончанию загрузки
            if (!loadingState.networkIssues) handleEvent(GroupEvent.Display)

            BottomSheet(groupState, cachedGroups, cachedTeachers, sheetState) { newValue ->
                when (selectedIndex) {
                    0 -> handleEvent(GroupEvent.UpdateCourse("$newValue курс"))
                    1 -> handleEvent(GroupEvent.UpdateSpeciality(newValue))
                    2 -> handleEvent(GroupEvent.UpdateGroup(newValue))
                    3 -> handleEvent(GroupEvent.UpdateDepartment(newValue))
                    4 -> handleEvent(GroupEvent.UpdateTeacher(newValue))
                }
                onDismiss()
            }

        }
        Spacer(modifier = Modifier.height(10.dp))
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    groupState: GroupState,
    cachedGroups: Map<String, Long>,
    cachedTeachers: Map<String, Long>,
    sheetState: SheetState,
    updateValue: (String) -> Unit,
) {
    when (groupState.selectedIndex) {
        0 -> CourseKeys(groupState.coursesToDisplay, updateValue)
        1 -> SpecialityKeys(groupState.levelsToDisplay, updateValue)
        2 -> GroupListContent(groupState.groupsToDisplay, updateValue, cachedGroups, sheetState)
        3 -> DepartmentListContent(groupState.departmentsToDisplay, updateValue)
        4 -> TeacherListContent(groupState.teachersToDisplay, updateValue, cachedTeachers, sheetState)
    }
}

@Composable
fun CourseKeys(
    coursesToDisplay: List<String>,
    updateValue: (String) -> Unit
) {
    coursesToDisplay.forEachIndexed { index, key ->
        if (coursesToDisplay.isEmpty()) return@forEachIndexed

        if (index != 0) {
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(25.dp, 0.dp),
                color = disabledWhite
            )
        }

        Card(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable { updateValue(key) },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.elevatedCardElevation(0.dp),
        ) {
            Text(
                text = "$key курс",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun SpecialityKeys(
    specialitiesToDisplay: List<String>,
    updateValue: (String) -> Unit
) {
    specialitiesToDisplay.forEachIndexed { index, speciality ->
        if (index != 0) {
            HorizontalDivider(
                thickness = 1.dp,
                modifier = Modifier.padding(25.dp, 0.dp),
                color = disabledWhite
            )
        }

        Card(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                .clickable { updateValue(speciality) },
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.elevatedCardElevation(0.dp),
        ) {
            Text(
                text = speciality,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                textAlign = TextAlign.Center,
                fontSize = 19.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }

    HorizontalDivider(
        thickness = 1.dp,
        modifier = Modifier.padding(25.dp, 0.dp),
        color = disabledWhite
    )

    Card(
        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            .clickable { updateValue("Все уровни") },
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(0.dp),
    ) {
        Text(
            text = LocalContext.current.getString(R.string.all_specialities),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            textAlign = TextAlign.Center,
            fontSize = 19.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GroupListContent(
    groupsToDisplay: List<String>,
    updateValue: (String) -> Unit,
    cachedGroups: Map<String, Long>,
    sheetState: SheetState
) {

    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var chipSelected by remember { mutableStateOf(false) }
    val filteredGroups = remember(groupsToDisplay, searchQuery, chipSelected, cachedGroups) {
        var result = groupsToDisplay

        if (chipSelected) {
            result = result.filter { cachedGroups.contains(it) }
        }

        if (searchQuery.isNotBlank()) {
            result = result.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        result
    }

    SearchField("Поиск группы", {
        searchQuery = it
    }) {
        if (it) {
            scope.launch {
                sheetState.expand()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {

        item {

            FilterChip(
                onClick = {
                    chipSelected = !chipSelected

                },
                selected = chipSelected,
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = disabledLightBlue,
                    disabledContentColor = Color.Black,
                    disabledBackgroundColor = disabledBlue,
                    selectedContentColor = Color.White,
                    selectedBackgroundColor = buttons
                )
            ) {
                Text(
                    "Сохранено",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (chipSelected) Color.White else buttons
                )
            }

            if (filteredGroups.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Список пуст",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 100.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }


        }

        itemsIndexed(filteredGroups, key = { _, group -> group }) { index, group ->
            group.let { groupName ->
                if (index != 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(25.dp, 0.dp),
                        color = disabledWhite
                    )
                }

                Card(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable { updateValue(groupName) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.elevatedCardElevation(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = groupName,
                            modifier = Modifier
                                .padding(10.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (cachedGroups.keys.contains(groupName)) {
                                CachedMark(Modifier.padding(end = 25.dp))
                            }
                        }

                    }
                }
            }
        }
    }

}

@Composable
fun DepartmentListContent(
    departmentsToDisplay: List<String>,
    updateValue: (String) -> Unit
) {
    LazyColumn {
        itemsIndexed(departmentsToDisplay.sortedBy { -it.length }, key = { _, department -> department }) { index, department ->
            department.let { department ->
                if (department != "null") {

                    if (index != 0) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            modifier = Modifier.padding(25.dp, 0.dp),
                            color = disabledWhite
                        )
                    }

                    Card(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            .clickable { updateValue(department) },
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.elevatedCardElevation(0.dp),
                    ) {
                        Text(
                            text = department,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TeacherListContent(
    teachersToDisplay: List<String>,
    updateValue: (String) -> Unit,
    cachedTeachers: Map<String, Long>,
    sheetState: SheetState
) {

    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var chipSelected by remember { mutableStateOf(false) }
    val filteredTeachers = remember(teachersToDisplay, searchQuery, chipSelected, cachedTeachers) {
        var result = teachersToDisplay

        if (chipSelected) {
            result = result.filter { cachedTeachers.contains(it) }
        }

        if (searchQuery.isNotBlank()) {
            result = result.filter { it.contains(searchQuery, ignoreCase = true) }
        }

        result
    }

    SearchField("Поиск преподавателя", {
        searchQuery = it
    }) {
        if (it) {
            scope.launch {
                sheetState.expand()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
    ) {

        item {

            FilterChip(
                onClick = {
                    chipSelected = !chipSelected

                },
                selected = chipSelected,
                modifier = Modifier
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ChipDefaults.filterChipColors(
                    backgroundColor = disabledLightBlue,
                    disabledContentColor = Color.Black,
                    disabledBackgroundColor = disabledBlue,
                    selectedContentColor = Color.White,
                    selectedBackgroundColor = buttons
                )
            ) {
                Text(
                    "Сохранено",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (chipSelected) Color.White else buttons
                )
            }

            if (filteredTeachers.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Список пуст",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 100.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }
        }

        itemsIndexed(filteredTeachers, key = { _, teacher -> teacher }) { index, teacher ->
            teacher.let { teacherLabel ->
                if (index != 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        modifier = Modifier.padding(25.dp, 0.dp),
                        color = disabledWhite
                    )
                }

                Card(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        .clickable { updateValue(teacherLabel) },
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.elevatedCardElevation(0.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = teacherLabel,
                            modifier = Modifier
                                .padding(10.dp),
                            textAlign = TextAlign.Center,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (cachedTeachers.keys.contains(teacherLabel)) {
                                CachedMark(Modifier.padding(end = 25.dp))
                            }
                        }

                    }
                }

            }
        }
    }
}