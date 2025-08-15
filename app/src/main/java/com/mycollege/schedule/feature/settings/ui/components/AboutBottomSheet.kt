package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R

@Preview
@Composable
fun AboutPreview() {
    AboutContent()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutBottomSheet(
    onDismiss: () -> Unit
) {

    ModalBottomSheet(
        modifier = Modifier.wrapContentHeight().padding(10.dp, 30.dp, 10.dp, 15.dp),
        sheetState = rememberModalBottomSheetState(),
        shape = RoundedCornerShape(17.dp),
        contentColor = Color.Black,
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Black) },
        onDismissRequest = onDismiss
    ) {
        AboutContent()
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutContent() {
    Column(
        modifier = Modifier
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Logo",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .size(100.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Мой ИМСИТ",
                fontSize = 24.sp,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier
                    .height(60.dp)
                    .padding(vertical = 20.dp, horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "версия приложения: " + BuildConfig.VERSION_NAME,
                    color = Color.Black,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}