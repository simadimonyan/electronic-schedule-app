package com.mycollege.schedule.feature.schedule.ui.components.schedule

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.ui.components.utils.Loader
import com.mycollege.schedule.feature.schedule.ui.state.ScheduleState
import com.mycollege.schedule.feature.settings.ui.state.SettingsState
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.disabledWhite

@Composable
fun DefaultLoadingUnit(scheduleState: ScheduleState, settingsState: SettingsState) {
    val context: Context = LocalContext.current
    val darkMode = LocalAppDarkTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(25.dp, 45.dp, 80.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = scheduleState.todayDate,
                color = if (darkMode) Color.White else Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Неделя ${if (settingsState.weekCount) 2 else 1}",
                color = if (darkMode) disabledWhite else Color.Gray,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(50.dp))
        Text(
            context.getString(R.string.empty_screen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(0.dp, 50.dp, 0.dp, 10.dp),
            textAlign = TextAlign.Center,
            color = if (darkMode) Color.White else Color.Black,
            fontSize = 23.sp,
            fontWeight = FontWeight.Bold
        )
        Loader(resource = R.raw.error_animation, 270.dp)
    }
    /*
    * Text(
        text = context.getString(R.string.message),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 45.dp),
        textAlign = TextAlign.Left,
        color = Color.Black,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = context.getString(R.string.recommendations),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 45.dp),
        textAlign = TextAlign.Left,
        color = Color.Gray,
        fontSize = 13.sp
    )
    * */
}