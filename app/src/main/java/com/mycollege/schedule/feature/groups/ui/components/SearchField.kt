package com.mycollege.schedule.feature.groups.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.R
import com.mycollege.schedule.shared.ui.theme.disabledWhite
import com.mycollege.schedule.shared.ui.theme.secondaryDark

@Preview
@Composable
fun SearchFieldPreview() {
    SearchField("Поиск", {}) {}
}

@Composable
fun SearchField(placeholder: String, onValueChanged: (String) -> Unit, onFocusChanged: (Boolean) -> Unit) {

    var input by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val darkMode = isSystemInDarkTheme()

    TextField(
        value = input,
        onValueChange = {
            input = it
            onValueChanged(it)
        },
        modifier = Modifier.fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 20.dp)
            .imePadding()
            .onFocusChanged { focusState ->
                val wasFocused = isFocused
                isFocused = focusState.isFocused

                if (isFocused != wasFocused) {
                    onFocusChanged(isFocused)
                }
            },
        placeholder = {
            Text(placeholder, modifier = Modifier, color = if (darkMode) Color.LightGray else Color.Gray, fontSize = 14.sp)
        },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.search),
                "search",
                Modifier.size(20.dp),
                tint = if (darkMode) Color.White else Color.Black
            )
        },
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = if (darkMode) secondaryDark else disabledWhite,
            focusedContainerColor = if (darkMode) secondaryDark else disabledWhite,
            focusedTextColor = if (darkMode) Color.White else Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = if (darkMode) Color.White else Color.Black
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
            }
        )
    )
}