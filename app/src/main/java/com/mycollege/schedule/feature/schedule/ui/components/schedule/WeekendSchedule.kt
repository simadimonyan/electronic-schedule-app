package com.mycollege.schedule.feature.schedule.ui.components.schedule

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.ui.components.utils.Loader
import com.mycollege.schedule.shared.ui.theme.buttons

@Composable
fun WeekendUnit() {
    Card(
        modifier = Modifier
            .padding(20.dp, 0.dp, 20.dp, 7.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = buttons)
    ) {
        Card(
            modifier = Modifier
                .padding(4.dp, 0.dp, 0.dp, 0.dp)
                .border(BorderStroke(2.dp, Color.White)),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Loader(resource = R.raw.weekend, 130.dp)
        }
    }
}