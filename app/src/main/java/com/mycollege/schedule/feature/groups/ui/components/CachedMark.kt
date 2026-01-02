package com.mycollege.schedule.feature.groups.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.backgroundDark
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledBlue

@Preview
@Composable
fun CachedMarkPreview() {
    CachedMark(Modifier)
}

@Composable
fun CachedMark(modifier: Modifier) {

    val darkMode = LocalAppDarkTheme.current

    Card(
        modifier = modifier, //.border(1.dp, disabledBlue, RoundedCornerShape(3.dp)),
        shape = RoundedCornerShape(3.dp),
        colors = CardDefaults.cardColors(containerColor = if (darkMode) backgroundDark else Color.White),//buttons),
        elevation = CardDefaults.elevatedCardElevation(0.dp)
    ) {
        Icon(painterResource(R.drawable.cached), "cached", Modifier.padding(5.dp).size(15.dp), if (darkMode) Color.LightGray else Color.Gray)//Color.White)
    }
}