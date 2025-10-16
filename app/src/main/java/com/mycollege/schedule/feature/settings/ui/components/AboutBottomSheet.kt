package com.mycollege.schedule.feature.settings.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.BuildConfig
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.theme.disabledBlue
import com.mycollege.schedule.shared.ui.theme.disabledVeryLightBlue
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    val formatter = DateTimeFormatter.ofPattern("yyyy", Locale("RU"))
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(R.drawable.imsit),
                contentDescription = "Logo",
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .size(200.dp, 120.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .size(60.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Мой ИМСИТ",
                        fontSize = 24.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = "Версия: " + BuildConfig.VERSION_NAME,
                        color = Color.DarkGray,
                        fontSize = 18.sp
                    )
                }

            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Copyright © 2024 - ${LocalDate.now().format(formatter)}",
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 10.dp)
                .clickable(
                    interactionSource = null,
                    indication = null
                ) {
                    uriHandler.openUri("https://t.me/+KJ4GaYqruzJjOTJi") // android-app-link
                },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = disabledVeryLightBlue)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.telegram_icon),
                    contentDescription = "Logo",
                    modifier = Modifier.size(30.dp),
                    tint = disabledBlue
                )
            }
        }

        //Spacer(modifier = Modifier.height(10.dp))
    }
}