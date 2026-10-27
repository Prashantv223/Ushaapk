package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HindiCalendarData
import com.example.data.model.HindiMonth
import com.example.data.model.HolidayItem
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HindiCalendarScreen(viewModel: MainViewModel) {
    val selectedMonth by viewModel.selectedHindiMonth.collectAsState()
    val allHolidays by viewModel.holidays.collectAsState()
    val panchang = remember { HindiCalendarData.calculatePanchangToday() }

    var selectedRegionFilter by remember { mutableStateOf("All") }
    var showAddEventDialog by remember { mutableStateOf(false) }

    var eventNameInput by remember { mutableStateOf("") }
    var eventDateInput by remember { mutableStateOf("25 Oct 2027") }
    var eventRegionInput by remember { mutableStateOf("North") }
    var eventDescInput by remember { mutableStateOf("") }

    val regions = listOf("All", "National", "North", "South", "East", "West")

    val filteredHolidays = allHolidays.filter { item ->
        val matchesRegion = selectedRegionFilter == "All" || item.region.contains(selectedRegionFilter, ignoreCase = true)
        val matchesMonth = item.hindiMonth.equals(selectedMonth.englishName, ignoreCase = true) || item.isRegionalCustom
        matchesRegion && matchesMonth
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddEventDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_holiday_event")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Plan Regional Event")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Panchang Header Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = panchang["samvat"] ?: "विक्रम संवत 2083",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = panchang["shaka"] ?: "शक संवत 1948",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Speech Vani button to speak panchang
                            Button(
                                onClick = {
                                    val speechText = "आज का पंचांग: ${panchang["samvat"]}, ${panchang["maas"]}, ${panchang["paksha"]}, तिथि ${panchang["tithi"]}, नक्षत्र ${panchang["nakshatra"]}, अभिजीत मुहूर्त ${panchang["abhijitMuhurat"]}."
                                    viewModel.speakText(speechText)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_speak_panchang")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("पंचांग वाणी", fontSize = 11.sp)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            PanchangPill("मास", panchang["maas"]?.substringBefore(" ") ?: "कार्तिक")
                            PanchangPill("पक्ष", panchang["paksha"]?.substringBefore(" ") ?: "शुक्ल")
                            PanchangPill("तिथि", panchang["tithi"]?.substringBefore(" ") ?: "द्वादशी")
                            PanchangPill("नक्षत्र", panchang["nakshatra"]?.substringBefore(" ") ?: "रोहिणी")
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "राहुकाल: ${panchang["rahuKaal"]}",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444)
                            )
                            Text(
                                text = "शुभ मुहूर्त: ${panchang["abhijitMuhurat"]}",
                                fontSize = 11.sp,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            item {
                // 12 Hindi Months Horizontal Selector
                Text(
                    text = "हिन्दी मास (Hindi Months)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(HindiCalendarData.hindiMonths) { month ->
                        val isSelected = selectedMonth.englishName == month.englishName
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectHindiMonth(month) },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(month.hindiName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(month.englishName, fontSize = 10.sp)
                                }
                            },
                            modifier = Modifier.testTag("chip_month_${month.englishName}")
                        )
                    }
                }
            }

            item {
                // Month overview description
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${selectedMonth.hindiName} (${selectedMonth.englishName}) • ${selectedMonth.season}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Gregorian window: ${selectedMonth.gregorianApprox}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Regional Filter Chips
                Text(
                    text = "Regional Holiday & Event Tracker",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(regions) { reg ->
                        val isSelected = selectedRegionFilter == reg
                        SuggestionChip(
                            onClick = { selectedRegionFilter = reg },
                            label = { Text(reg, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }

            if (filteredHolidays.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 30.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recorded festivals in ${selectedMonth.hindiName} for $selectedRegionFilter. Tap '+ Plan Regional Event' to create one.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(filteredHolidays, key = { it.id }) { item ->
                    HolidayCard(
                        item = item,
                        onSpeak = {
                            viewModel.speakText("${item.hindiName}. ${item.englishName}. Celebrated in ${item.region} on ${item.dateString}. ${item.description}")
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Dialog: Add Custom Regional Event Planner
    if (showAddEventDialog) {
        AlertDialog(
            onDismissRequest = { showAddEventDialog = false },
            title = { Text("Plan Regional Event") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = eventNameInput,
                        onValueChange = { eventNameInput = it },
                        label = { Text("Event / Festival Name") },
                        placeholder = { Text("e.g. Baisakhi / Bihu / Chhath Gathering") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_event_name")
                    )
                    OutlinedTextField(
                        value = eventDateInput,
                        onValueChange = { eventDateInput = it },
                        label = { Text("Date & Time") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_event_date")
                    )
                    OutlinedTextField(
                        value = eventRegionInput,
                        onValueChange = { eventRegionInput = it },
                        label = { Text("Region (North, South, East, West, National)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("input_event_region")
                    )
                    OutlinedTextField(
                        value = eventDescInput,
                        onValueChange = { eventDescInput = it },
                        label = { Text("Event Planning Notes & Rituals") },
                        placeholder = { Text("Prasad preparations, guests, venue") },
                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("input_event_desc")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (eventNameInput.isNotBlank()) {
                            viewModel.addCustomHoliday(
                                eventNameInput.trim(),
                                eventDateInput.trim(),
                                eventRegionInput.trim(),
                                eventDescInput.trim()
                            )
                            eventNameInput = ""
                            eventDescInput = ""
                            showAddEventDialog = false
                        }
                    },
                    modifier = Modifier.testTag("btn_confirm_add_event")
                ) {
                    Text("Add to Planner")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEventDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun PanchangPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HolidayCard(item: HolidayItem, onSpeak: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.hindiName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = item.englishName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = item.region,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 ${item.dateString} • ${item.tithi}",
                    fontSize = 12.sp,
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = onSpeak,
                    modifier = Modifier.size(32.dp).testTag("holiday_speak_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Speak Holiday Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
