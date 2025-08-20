package com.mycollege.schedule.feature.groups.ui.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.groups.ui.state.GroupEvent
import com.mycollege.schedule.feature.groups.ui.state.GroupState
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.buttons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetContent(
    loading: Boolean,
    progress: Int,
    groupState: GroupState,
    handleEvent: (GroupEvent) -> Unit,
    selectedIndex: Int,
    onDismiss: () -> Unit
) {
    val context: Context = LocalContext.current
    val animatedProgress = animateFloatAsState(targetValue = progress / 100f, label = "progress")

    ModalBottomSheet(
        modifier = Modifier.wrapContentHeight().padding(10.dp, 30.dp, 10.dp, 15.dp),
        sheetState = rememberModalBottomSheetState(),
        shape = RoundedCornerShape(17.dp),
        contentColor = Color.White,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black) },
        onDismissRequest = onDismiss
    ) {
        if (loading) {
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
            handleEvent(GroupEvent.Display)

            BottomSheet(groupState) { newValue ->
                when (selectedIndex) {
                    0 -> handleEvent(GroupEvent.UpdateCourse(newValue))
                    1 -> handleEvent(GroupEvent.UpdateSpeciality(newValue))
                    2 -> handleEvent(GroupEvent.UpdateGroup(newValue))
                }
                onDismiss()
            }

        }
        Spacer(modifier = Modifier.height(10.dp))
    }

}

@Composable
fun BottomSheet(
    groupState: GroupState,
    updateValue: (String) -> Unit,
) {
    when (groupState.selectedIndex) {
        0 -> CourseKeys(groupState.coursesToDisplay, updateValue)
        1 -> SpecialityKeys(groupState.levelsToDisplay, updateValue)
        else -> GroupListContent(groupState.groupsToDisplay, updateValue)
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
                thickness = 0.5.dp,
                modifier = Modifier.padding(25.dp, 0.dp),
                color = Color.LightGray
            )
        }

        Text(
            text = key,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable { updateValue(key) },
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = Color.Black
        )
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
                thickness = 0.5.dp,
                modifier = Modifier.padding(25.dp, 0.dp),
                color = Color.LightGray
            )
        }

        Text(
            text = speciality,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .clickable { updateValue(speciality) },
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            color = Color.Black
        )
    }

    HorizontalDivider(
        thickness = 0.5.dp,
        modifier = Modifier.padding(25.dp, 0.dp),
        color = Color.LightGray
    )
    Text(
        text = LocalContext.current.getString(R.string.all_specialities),
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable { updateValue("Все специальности") },
        textAlign = TextAlign.Center,
        fontSize = 20.sp,
        color = Color.Black
    )
}

@Composable
fun GroupListContent(
    groupsToDisplay: List<String>,
    updateValue: (String) -> Unit
) {
    LazyColumn {
        itemsIndexed(groupsToDisplay, key = { _, group -> group }) { index, group ->
            group.let { nonNullGroup ->
                if (index != 0) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(25.dp, 0.dp),
                        color = Color.LightGray
                    )
                }

                Text(
                    text = nonNullGroup,
                    modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth()
                        .clickable { updateValue(nonNullGroup) },
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }
        }
    }
}