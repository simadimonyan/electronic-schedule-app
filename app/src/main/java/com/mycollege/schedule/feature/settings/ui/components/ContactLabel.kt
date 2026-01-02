package com.mycollege.schedule.feature.settings.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.tertiaryDark

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ContactLabel() {

    val darkMode = LocalAppDarkTheme.current

    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        // 400.dp is size of width when 13.sp is too big
        val textSize = if (maxWidth >= 400.dp) 12.sp else 13.sp

        val inlineContent = mapOf(
            "inlineImage" to InlineTextContent(
                placeholder = Placeholder(
                    width = 16.sp,
                    height = 16.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                ),
                children = { altText ->
                    Image(
                        painter = painterResource(id = R.drawable.telegram),
                        contentDescription = altText,
                        modifier = Modifier
                            .size(16.dp)
                            .offset(y = 1.dp),
                        colorFilter = ColorFilter.tint(if (darkMode) tertiaryDark else Color.Black)
                    )
                }
            )
        )

        SelectionContainer {
            Text(
                text = buildAnnotatedString {
                    append("${LocalContext.current.getString(R.string.contacts)} ")
                    appendInlineContent("inlineImage", "telegram: ")
                    append(" ${LocalContext.current.getString(R.string.tg)}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 60.dp),
                color = if (darkMode) tertiaryDark else Color.Gray,
                lineHeight = 17.sp,
                minLines = 2,
                maxLines = 2,
                textAlign = TextAlign.Center,
                inlineContent = inlineContent,
                fontSize = textSize,
            )
        }
    }
}