package com.mycollege.schedule.shared.ui.design

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycollege.schedule.shared.ui.theme.Ma
import com.mycollege.schedule.shared.ui.theme.MaBrand
import com.mycollege.schedule.shared.ui.theme.MaType

/** нажатие с рябью внутри формы */
@Composable
fun Modifier.maClick(enabled: Boolean = true, role: Role = Role.Button, onClick: () -> Unit): Modifier =
    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(),
        enabled = enabled,
        role = role,
        onClick = onClick
    )

/** Надзаголовок-пилюля с точкой (eyebrow на акцентном фоне) */
@Composable
fun EyebrowPill(text: String, modifier: Modifier = Modifier, size: TextUnit = 11.sp) {
    val c = Ma.colors
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(c.accentSoft)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(5.dp).clip(CircleShape).background(c.accent))
        Text(text.uppercase(), style = MaType.eyebrow(size), color = c.accent, maxLines = 1)
    }
}

/** Подпись секции капслоком (faint) */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    val c = Ma.colors
    Row(modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text.uppercase(), style = MaType.eyebrow(10.5.sp), color = c.faint, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, style = MaType.mono(11.sp), color = c.faint)
    }
}

/** Квадратная плитка со значком (32×32, радиус 9) */
@Composable
fun IconTile(path: String, modifier: Modifier = Modifier, bg: Color = Ma.colors.accentSoft, fg: Color = Ma.colors.accent, size: Dp = 32.dp, iconSize: Dp = 18.dp, stroke: Float = 1.8f) {
    Box(modifier.size(size).clip(RoundedCornerShape(9.dp)).background(bg), contentAlignment = Alignment.Center) {
        MaIcon(path, fg, iconSize, stroke)
    }
}

/** Основная кнопка: синяя пилюля с тенью */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    fontSize: TextUnit = 15.5.sp,
    trailingIcon: String? = null,
    content: (@Composable RowScope.() -> Unit)? = null,
) {
    val c = Ma.colors
    val shape = CircleShape
    Row(
        modifier = modifier
            .height(height)
            .then(if (enabled) Modifier.maShadow(MaBrand.primaryShadow, height / 2) else Modifier)
            .clip(shape)
            .background(if (enabled) MaBrand.primary else c.sunk, shape)
            .maClick(enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (content != null) content() else {
            Text(text, style = MaType.body(fontSize, FontWeight.SemiBold), color = if (enabled) Color.White else c.faint, maxLines = 1)
            if (trailingIcon != null) MaIcon(trailingIcon, if (enabled) Color.White else c.faint, 17.dp, 2.2f)
        }
    }
}

/** Вторичная кнопка: пилюля с рамкой */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 42.dp,
    fontSize: TextUnit = 14.sp,
    color: Color = Ma.colors.fg,
    borderColor: Color = Ma.colors.borderStrong,
    background: Color = Color.Transparent,
    trailingIcon: String? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(CircleShape)
            .background(background)
            .border(1.dp, borderColor, CircleShape)
            .maClick(enabled, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, style = MaType.body(fontSize, FontWeight.SemiBold), color = color, maxLines = 1)
        if (trailingIcon != null) MaIcon(trailingIcon, color, 16.dp, 2.2f)
    }
}

/** Маленькая акцентная кнопка с рамкой accentBorder («Повторить», «Сменить») */
@Composable
fun AccentChipButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, height: Dp = 30.dp) {
    val c = Ma.colors
    Box(
        modifier
            .height(height)
            .clip(CircleShape)
            .border(1.dp, c.accentBorder, CircleShape)
            .maClick(onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaType.body(12.5.sp, FontWeight.SemiBold), color = c.accent, maxLines = 1)
    }
}

/** Круглая кнопка-«материал» 44dp со значком (шапка экрана) */
@Composable
fun MatIconButton(path: String, contentDescription: String, onClick: () -> Unit, modifier: Modifier = Modifier, width: Dp = 44.dp, iconSize: Dp = 20.dp, stroke: Float = 1.9f) {
    val c = Ma.colors
    Box(
        modifier
            .size(width, 44.dp)
            .maMat(22.dp)
            .maClick(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        MaIcon(path, c.fg, iconSize, stroke)
    }
}

/** Тумблер 46×28 */
@Composable
fun MaSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val c = Ma.colors
    val track by animateColorAsState(if (checked) MaBrand.primary else c.borderStrong, tween(120), label = "track")
    val knob by animateDpAsState(if (checked) 21.dp else 3.dp, tween(200), label = "knob")
    Box(
        modifier
            .size(46.dp, 28.dp)
            .alpha(if (enabled) 1f else 0.45f)
            .clip(CircleShape)
            .background(track)
            .maClick(enabled, Role.Switch) { onCheckedChange(!checked) }
    ) {
        Box(
            Modifier
                .offset(x = knob, y = 3.dp)
                .size(22.dp)
                .maShadow(com.mycollege.schedule.shared.ui.theme.MaShadow(Color(0x330F172A), 1f, 3f), 11.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

/**
 * Сегментный переключатель на «утопленном» фоне: выбранный сегмент — поверхность с тенью и акцентным текстом
 */
@Composable
fun MaSegmented(
    labels: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 28.dp,
    radius: Dp = 10.dp,
    fontSize: TextUnit = 12.5.sp,
    fill: Boolean = false,
    enabled: Boolean = true,
    mono: Boolean = false,
    icons: List<String>? = null,
    segmentMinWidth: Dp = 34.dp,
) {
    val c = Ma.colors
    Row(
        modifier
            .clip(RoundedCornerShape(radius))
            .background(c.sunk)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        labels.forEachIndexed { i, label ->
            val on = i == selected
            val inner = radius - 3.dp
            Row(
                Modifier
                    .then(if (fill) Modifier.weight(1f) else Modifier.widthIn(min = segmentMinWidth))
                    .height(height)
                    .then(if (on) Modifier.maShadows(c.shSm, inner) else Modifier)
                    .clip(RoundedCornerShape(inner))
                    .background(if (on) c.surface else Color.Transparent)
                    .maClick(enabled) { onSelect(i) }
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icons?.getOrNull(i)?.let { MaIcon(it, if (on) c.accent else c.soft, 16.dp, 2f) }
                Text(
                    label,
                    style = if (mono) MaType.mono(fontSize, FontWeight.Bold) else MaType.body(fontSize, if (fill) FontWeight.SemiBold else FontWeight.Bold),
                    color = if (on) c.accent else c.soft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Строка списка настроек: плитка со значком, подпись, подзаголовок и хвост */
@Composable
fun SettingsRow(
    icon: String,
    label: String,
    modifier: Modifier = Modifier,
    sub: String? = null,
    divider: Boolean = false,
    onClick: (() -> Unit)? = null,
    alpha: Float = 1f,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    val c = Ma.colors
    Box(modifier.fillMaxWidth()) {
        if (divider) Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .then(if (onClick != null) Modifier.maClick(onClick = onClick) else Modifier)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .alpha(alpha),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconTile(icon)
            androidx.compose.foundation.layout.Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(label, style = MaType.body(14.5.sp, FontWeight.Medium), color = c.fg)
                if (!sub.isNullOrBlank()) Text(sub, style = MaType.body(12.sp, lineHeight = 16.sp), color = c.faint)
            }
            trailing()
        }
    }
}

/** Пустой отступ-«воздух» */
@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))
