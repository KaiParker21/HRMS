package com.skye.hrms.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun SingleChoiceButtonGroup(
    selectedIndex: Int,
    onSelectionChanged: (Int) -> Unit
) {

    ButtonGroup(
        modifier = Modifier.fillMaxWidth(),
        expandedRatio = 0.1f,
        overflowIndicator = {},
        horizontalArrangement = Arrangement.Center
    ) {
        leaves.forEach { leave->
            val checked = leave.leaveIdx == selectedIndex
            this.toggleableItem(
                weight = ButtonGroupDefaults.ExpandedRatio,
                checked = checked,
                onCheckedChange = { onSelectionChanged(leave.leaveIdx) },
                label = leave.name,
                icon = {}
            )
        }
    }
}

data class LeaveType (
    val leaveIdx: Int,
    val name: String
)

val leaves = listOf<LeaveType>(
    LeaveType(0, "Casual"),
    LeaveType(1, "Unpaid"),
    LeaveType(2, "Sick"),
//    LeaveType(3, "Earned")
)