package com.mycollege.schedule.feature.surveys.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.mycollege.schedule.shared.ui.design.MaIcon
import com.mycollege.schedule.shared.ui.design.MaIcons
import com.mycollege.schedule.shared.ui.design.maClick
import com.mycollege.schedule.shared.ui.design.maShadow
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaShadow
import com.mycollege.schedule.shared.ui.theme.MaType

/** цвет ошибок формы */
val surveyErrorColor = MaBrand.red

/**
 * Вопрос опроса (Survey.dc.html): подпись, «· необязательно», поле ответа по типу
 */
@Composable
fun SurveyQuestion(
    question: QuizQuestion,
    answers: SurveyAnswers,
    enabled: Boolean,
    onSelectSingle: (Long) -> Unit,
    onToggleMultiple: (Long) -> Unit,
    onValueChange: (String) -> Unit
) {
    val c = Ma.colors
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            buildAnnotatedString {
                append(question.label.orEmpty())
                if (question.required != true) withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = c.faint)) { append(" · необязательно") }
            },
            style = MaType.body(12.5.sp, FontWeight.SemiBold, lineHeight = 17.sp),
            color = c.muted
        )
        if (!question.description.isNullOrBlank()) {
            Text(question.description, style = MaType.body(12.5.sp, lineHeight = 17.sp), color = c.faint)
        }

        when (QuestionType.of(question)) {
            QuestionType.RATING -> RatingRow(
                scale = question.scale,
                value = answers.values[question.id]?.toIntOrNull(),
                enabled = enabled,
                onSelect = { onValueChange(it.toString()) }
            )
            QuestionType.SINGLE -> question.answers.orEmpty().forEach { answer ->
                ChoiceRow(answer.label.orEmpty(), answers.choices[question.id].orEmpty().contains(answer.id), radio = true, enabled = enabled) { onSelectSingle(answer.id) }
            }
            QuestionType.MULTIPLE -> question.answers.orEmpty().forEach { answer ->
                ChoiceRow(answer.label.orEmpty(), answers.choices[question.id].orEmpty().contains(answer.id), radio = false, enabled = enabled) { onToggleMultiple(answer.id) }
            }
            QuestionType.TEXT -> TextAnswer(answers.values[question.id].orEmpty(), enabled, onValueChange)
            QuestionType.UNKNOWN -> Unit
        }
    }
}

private val rateWords = listOf("", "Плохо", "Так себе", "Нормально", "Хорошо", "Отлично")

/** Оценка: ряд квадратных кнопок 1..scale, подписи «Плохо — Отлично» */
@Composable
fun RatingRow(scale: Int?, value: Int?, enabled: Boolean, onSelect: (Int) -> Unit) {
    val c = Ma.colors
    val max = (scale ?: 5).coerceIn(2, 10)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..max) {
                val on = value == i
                val shape = RoundedCornerShape(16.dp)
                Box(
                    Modifier
                        .weight(1f)
                        .height(if (max > 6) 48.dp else 58.dp)
                        .then(if (on) Modifier.maShadow(MaBrand.primaryShadow, 16.dp) else Modifier)
                        .clip(shape)
                        .background(if (on) MaBrand.primary else c.surface)
                        .border(1.dp, if (on) Color.Transparent else c.borderStrong, shape)
                        .maClick(enabled, Role.RadioButton) { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("$i", style = MaType.display(if (max > 6) 17.sp else 21.sp, FontWeight.ExtraBold), color = if (on) Color.White else c.fg)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Плохо", style = MaType.body(12.sp), color = c.faint)
            Text(
                if (max == 5 && value != null) rateWords.getOrElse(value) { "" } else "",
                style = MaType.body(12.sp, FontWeight.SemiBold), color = c.accent,
                textAlign = TextAlign.Center, modifier = Modifier.weight(1f)
            )
            Text("Отлично", style = MaType.body(12.sp), color = c.faint)
        }
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, radio: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val c = Ma.colors
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .alpha(if (enabled) 1f else 0.6f)
            .clip(shape)
            .background(if (selected) c.accentSoft else c.surface)
            .border(1.dp, if (selected) c.accentBorder else c.borderStrong, shape)
            .maClick(enabled, if (radio) Role.RadioButton else Role.Checkbox, onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val markShape = if (radio) CircleShape else RoundedCornerShape(6.dp)
        Box(
            Modifier
                .size(20.dp)
                .clip(markShape)
                .background(if (selected) MaBrand.primary else Color.Transparent)
                .border(if (selected) 0.dp else 1.5.dp, if (selected) Color.Transparent else c.borderStrong, markShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                if (radio) Box(Modifier.size(8.dp).clip(CircleShape).background(Color.White))
                else MaIcon(MaIcons.check, Color.White, 13.dp, 3f)
            }
        }
        Text(label, style = MaType.body(14.5.sp), color = c.fg)
    }
}

@Composable
private fun TextAnswer(value: String, enabled: Boolean, onValueChange: (String) -> Unit) {
    val c = Ma.colors
    val interaction = remember { MutableInteractionSource() }
    val focused by interaction.collectIsFocusedAsState()
    val shape = RoundedCornerShape(12.dp)
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Box(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 78.dp)
                .then(if (focused) Modifier.maShadow(MaShadow(Color(0x291A4FDB), 0f, 0f, 3f), 12.dp) else Modifier)
                .clip(shape)
                .background(c.surface)
                .border(1.5.dp, if (focused) MaBrand.primary else c.borderStrong, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            if (value.isEmpty()) Text("Напишите пару слов", style = MaType.body(14.sp, lineHeight = 21.sp), color = c.faint)
            BasicTextField(
                value = value,
                onValueChange = { onValueChange(it.take(SURVEY_TEXT_LIMIT)) },
                enabled = enabled,
                textStyle = MaType.body(14.sp, lineHeight = 21.sp).copy(color = c.fg),
                cursorBrush = SolidColor(MaBrand.primary),
                interactionSource = interaction,
                minLines = 3,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            "${value.length} / $SURVEY_TEXT_LIMIT",
            style = MaType.mono(10.5.sp, FontWeight.Normal),
            color = if (value.length >= SURVEY_TEXT_LIMIT) surveyErrorColor else c.faint,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
