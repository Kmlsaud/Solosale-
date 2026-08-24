package com.example.solosale.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.solosale.ui.theme.GeometricCashGreen
import com.example.solosale.ui.theme.GeometricCashGreenContainer
import com.example.solosale.ui.theme.GeometricDueRed
import com.example.solosale.ui.theme.GeometricDueRedContainer
import com.example.solosale.ui.theme.GeometricDueRedOnContainer
import com.example.solosale.ui.theme.GeometricWarningAmber
import com.example.solosale.ui.theme.GeometricWarningContainer

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    valueColor: Color = contentColor,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    testTag: String = "stat_card"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 112.dp)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            )
            .testTag(testTag),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = border,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = contentColor
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = valueColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun StockBadge(
    quantity: Double,
    minStock: Double,
    unit: String = "Pcs",
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, text) = when {
        quantity <= 0 -> Triple(
            GeometricDueRedContainer,
            GeometricDueRedOnContainer,
            "OUT OF STOCK"
        )
        quantity <= minStock -> Triple(
            GeometricWarningContainer,
            GeometricWarningAmber,
            "LOW ($quantity $unit)"
        )
        else -> Triple(
            GeometricCashGreenContainer,
            GeometricCashGreen,
            "$quantity $unit"
        )
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(100.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
