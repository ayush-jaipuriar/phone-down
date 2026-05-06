package phonedown.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName")
fun PhoneDownSettingRow(
    title: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    trailing: String? = null,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = false,
    destructive: Boolean = false,
) {
    val clickableModifier =
        if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable(role = Role.Button, onClick = onClick)
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .then(clickableModifier)
                .padding(vertical = PhoneDownSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
        ) {
            Text(
                text = title,
                color = if (destructive) PhoneDownDesign.colors.danger else PhoneDownDesign.colors.textPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (trailing != null) {
            Text(
                text = trailing,
                color = if (destructive) PhoneDownDesign.colors.danger else PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        if (showChevron) {
            Text(
                text = "\u2192",
                color = PhoneDownDesign.colors.textTertiary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(start = PhoneDownSpacing.xs),
            )
        }
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = PhoneDownSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
        ) {
            Text(
                text = title,
                color = PhoneDownDesign.colors.textPrimary,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            if (supportingText != null) {
                Text(
                    text = supportingText,
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = PhoneDownDesign.colors.surface,
                    checkedTrackColor = PhoneDownDesign.colors.toggle,
                ),
        )
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownThemeControl(
    selectedThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(PhoneDownDesign.colors.surface)
                .border(1.dp, PhoneDownDesign.colors.borderSubtle, MaterialTheme.shapes.large)
                .padding(PhoneDownSpacing.xxs)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
    ) {
        ThemeMode.entries.forEach { mode ->
            PhoneDownThemeOption(
                mode = mode,
                selected = selectedThemeMode == mode,
                onThemeModeSelected = onThemeModeSelected,
            )
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun RowScope.PhoneDownThemeOption(
    mode: ThemeMode,
    selected: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    val selectedBackground =
        if (selected) {
            PhoneDownDesign.colors.toggle
        } else {
            PhoneDownDesign.colors.surface
        }
    Box(
        modifier =
            Modifier
                .weight(1f)
                .clip(MaterialTheme.shapes.medium)
                .background(selectedBackground)
                .padding(vertical = PhoneDownSpacing.xs)
                .selectable(
                    selected = selected,
                    role = Role.RadioButton,
                    onClick = { onThemeModeSelected(mode) },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mode.name,
            color =
                if (selected) {
                    PhoneDownDesign.colors.surface
                } else {
                    PhoneDownDesign.colors.textSecondary
                },
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
@Suppress("FunctionName")
fun PhoneDownProBadge(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .clip(MaterialTheme.shapes.medium)
                .background(PhoneDownDesign.colors.toggle)
                .padding(horizontal = PhoneDownSpacing.xs, vertical = PhoneDownSpacing.xxs),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Pro",
            color = PhoneDownDesign.colors.surface,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
