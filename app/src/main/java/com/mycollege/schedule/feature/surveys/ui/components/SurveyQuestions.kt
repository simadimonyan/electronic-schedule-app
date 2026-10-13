package com.mycollege.schedule.feature.surveys.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.core.network.dto.quizzes.QuizQuestion
import com.mycollege.schedule.feature.surveys.data.models.QuestionType
import com.mycollege.schedule.feature.surveys.data.models.SURVEY_TEXT_LIMIT
import com.mycollege.schedule.feature.surveys.data.models.SurveyAnswers
import com.mycollege.schedule.shared.ui.theme.LocalAppDarkTheme
import com.mycollege.schedule.shared.ui.theme.background
import com.mycollege.schedule.shared.ui.theme.buttons
import com.mycollege.schedule.shared.ui.theme.disabledLightBlue
import com.mycollege.schedule.shared.ui.theme.secondaryDark
import com.mycollege.schedule.shared.ui.theme.tertiaryDark

/** цвет пометки обязательного вопроса и ошибок */
val surveyErrorColor = Color(0xFFE5484D)

/** второстепенный текст (описания, счётчики) — читается и на белом, и на тёмном */
val surveySecondaryText = Color(0xFF8E8E93)

@Composable
fun SurveyQuestion(
    index: Int,
    question: QuizQuestion,
    answers: SurveyAnswers,
    enabled: Boolean,
    onSelectSingle: (Long) -> Unit,
    onToggleMultiple: (Long) -> Unit,
    onValueChange: (String) -> Unit
) {
    val darkMode = LocalAppDarkTheme.current
    val textColor = if (darkMode) Color.White else Color.Black

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp)) {

        Text(
            text = buildAnnotatedString {
                append("$index. ${question.label.orEmpty()}")
                if (question.required == true) {
                    withStyle(SpanStyle(color = surveyErrorColor)) { append(" *") }
                }
            },
            fontSize = 16.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )

        if (!question.description.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = question.description,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = surveySecondaryText
            )
        }

        Spacer(Modifier.height(10.dp))

        when (QuestionType.of(question)) {
            QuestionType.RATING -> RatingRow(
                scale = question.scale,
                value = answers.values[question.id]?.toIntOrNull(),
                enabled = enabled,
                onSelect = { onValueChange(it.toString()) }
            )
            QuestionType.SINGLE -> question.answers.orEmpty().forEach { answer ->
                val selected = answers.choices[question.id].orEmpty().contains(answer.id)
                ChoiceRow(
                    label = answer.label.orEmpty(),
                    selected = selected,
                    enabled = enabled,
                    modifier = Modifier.selectable(
                        selected = selected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onSelectSingle(answer.id) }
                    )
                ) {
                    RadioButton(
                        selected = selected,
                        onClick = null,
                        enabled = enabled,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = buttons,
                            unselectedColor = if (darkMode) tertiaryDark else Color.Gray
                        )
                    )
                }
            }
            QuestionType.MULTIPLE -> question.answers.orEmpty().forEach { answer ->
                val checked = answers.choices[question.id].orEmpty().contains(answer.id)
                ChoiceRow(
                    label = answer.label.orEmpty(),
                    selected = checked,
                    enabled = enabled,
                    modifier = Modifier.toggleable(
                        value = checked,
                        enabled = enabled,
                        role = Role.Checkbox,
                        onValueChange = { onToggleMultiple(answer.id) }
                    )
                ) {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                        enabled = enabled,
                        colors = CheckboxDefaults.colors(
                            checkedColor = buttons,
                            uncheckedColor = if (darkMode) tertiaryDark else Color.Gray,
                            checkmarkColor = Color.White
                        )
                    )
                }
            }
            QuestionType.TEXT -> TextAnswer(
                value = answers.values[question.id].orEmpty(),
                enabled = enabled,
                onValueChange = onValueChange
            )
            QuestionType.UNKNOWN -> Unit
        }
    }
}

/**
 * Ряд кружков 1..scale: выбранное значение залито акцентным цветом
 */
@Composable
fun RatingRow(
    scale: Int?,
    value: Int?,
    enabled: Boolean,
    onSelect: (Int) -> Unit
) {
    val darkMode = LocalAppDarkTheme.current
    val max = (scale ?: 5).coerceIn(2, 10)

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            for (i in 1..max) {
                val selected = value == i
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(max = 52.dp)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            when {
                                selected -> buttons
                                darkMode -> secondaryDark
                                else -> background
                            }
                        )
                        .border(
                            BorderStroke(1.dp, if (selected) buttons else if (darkMode) tertiaryDark else disabledLightBlue),
                            CircleShape
                        )
                        .selectable(
                            selected = selected,
                            enabled = enabled,
                            role = Role.RadioButton,
                            onClick = { onSelect(i) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$i",
                        fontSize = 17.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) Color.White else if (darkMode) Color.White else Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp, start = 4.dp, end = 4.dp)) {
            Text("плохо", fontSize = 12.sp, color = surveySecondaryText)
            Spacer(Modifier.weight(1f))
            Text("отлично", fontSize = 12.sp, color = surveySecondaryText)
        }
    }
}

@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier,
    control: @Composable () -> Unit
) {
    val darkMode = LocalAppDarkTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    selected -> if (darkMode) buttons.copy(alpha = 0.18f) else disabledLightBlue.copy(alpha = 0.12f)
                    darkMode -> secondaryDark
                    else -> background
                }
            )
            .then(modifier)
            .heightIn(min = 48.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.padding(4.dp)) { control() }
        Text(
            text = label,
            modifier = Modifier.padding(start = 6.dp, end = 8.dp),
            fontSize = 15.sp,
            color = when {
                !enabled -> surveySecondaryText
                darkMode -> Color.White
                else -> Color.Black
            }
        )
    }
}

@Composable
private fun TextAnswer(
    value: String,
    enabled: Boolean,
    onValueChange: (String) -> Unit
) {
    val darkMode = LocalAppDarkTheme.current
    val fieldColor = if (darkMode) secondaryDark else background

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.take(SURVEY_TEXT_LIMIT)) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
        placeholder = { Text("Напишите пару слов", color = surveySecondaryText, fontSize = 15.sp) },
        minLines = 3,
        maxLines = 8,
        shape = RoundedCornerShape(12.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 15.sp,
            color = if (darkMode) Color.White else Color.Black
        ),
        supportingText = {
            Text(
                text = "${value.length}/$SURVEY_TEXT_LIMIT",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontSize = 12.sp,
                color = if (value.length >= SURVEY_TEXT_LIMIT) surveyErrorColor else surveySecondaryText
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = fieldColor,
            unfocusedContainerColor = fieldColor,
            disabledContainerColor = fieldColor,
            focusedBorderColor = buttons,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            cursorColor = buttons,
            focusedTextColor = if (darkMode) Color.White else Color.Black,
            unfocusedTextColor = if (darkMode) Color.White else Color.Black
        )
    )
}
