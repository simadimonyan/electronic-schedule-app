package com.mycollege.schedule.feature.schedule.ui.components.schedule

import android.content.Context
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.feature.schedule.ui.components.utils.Loader

@Composable
fun DefaultLoadingUnit() {
    val context: Context = LocalContext.current

    Text(
        context.getString(R.string.empty_screen),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 50.dp, 0.dp, 10.dp),
        textAlign = TextAlign.Center,
        fontSize = 23.sp,
        fontWeight = FontWeight.Bold
    )
    Loader(resource = R.raw.error_animation, 270.dp)
    Spacer(modifier = Modifier.width(10.dp))
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