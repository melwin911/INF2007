package com.example.inf2007_mad_j1847.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun TimeSlotPicker(selectedTimeSlot: String, onTimeSelected: (String) -> Unit) {
    val morningSlots = listOf(
        "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "12:00"
    )

    val afternoonSlots = listOf(
        "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
    )

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Morning Slots",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(morningSlots) { timeSlot ->
                TimeSlotChip(
                    timeSlot = timeSlot,
                    isSelected = selectedTimeSlot == timeSlot,
                    onTimeSelected = onTimeSelected
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Afternoon Slots",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(afternoonSlots) { timeSlot ->
                TimeSlotChip(
                    timeSlot = timeSlot,
                    isSelected = selectedTimeSlot == timeSlot,
                    onTimeSelected = onTimeSelected
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSlotChip(
    timeSlot: String,
    isSelected: Boolean,
    onTimeSelected: (String) -> Unit
) {
    val displayTime = convertToReadableTime(timeSlot)

    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.clickable { onTimeSelected(timeSlot) }
    ) {
        Text(
            text = displayTime,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

// helper function to convert 24-hour format to 12-hour format with am/pm
fun convertToReadableTime(time24h: String): String {
    val hour = time24h.substring(0, 2).toInt()
    val minute = time24h.substring(3, 5)

    val amPm = if (hour < 12) "AM" else "PM"
    val hour12 = when (hour) {
        0 -> 12
        in 1..12 -> hour
        else -> hour - 12
    }

    return "$hour12:$minute $amPm"
}

// helper function to combine date and time into a firebase Timestamp
fun createTimestamp(dateString: String, timeString: String): com.google.firebase.Timestamp? {
    if (dateString.isBlank() || timeString.isBlank()) {
        return null // Prevents crashes by returning null
    }

    return try {
        val dateTimeString = "$dateString $timeString:00"
        val sdf = java.text.SimpleDateFormat("yyyy-M-d HH:mm:ss", Locale.getDefault())
        val date = sdf.parse(dateTimeString)

        if (date != null) {
            com.google.firebase.Timestamp(date.time / 1000, 0)
        } else {
            null // Return null if parsing fails
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null // Return null if an exception occurs
    }
}
