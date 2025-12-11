package com.example.moneymate.ui.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.transaction.model.TransactionEntity
import com.example.moneymate.R

@Composable
private fun TransactionItem(
    transaction: TransactionEntity,
    currencySymbol: String = "$"
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF1A1A1A),
                    fontWeight = FontWeight.Medium
                )

                if (transaction.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        transaction.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("#$tag", color = Color.Gray, fontSize = 10.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Category ID: ${transaction.categoryId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF666666)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (transaction.type == "income")
                    "+$currencySymbol${transaction.amount}"
                else
                    "-$currencySymbol${transaction.amount}",
                style = MaterialTheme.typography.bodyMedium,
                color = if (transaction.type == "income") Color(0xFF10B981) else Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TransactionsSection(
    transactions: List<TransactionEntity>,
    currencySymbol: String = "$",
    availableTags: List<String> = emptyList(),
    onSeeAll: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var isTagFilterExpanded by remember { mutableStateOf(false) }

    val filteredTransactions = transactions.filter { transaction ->
        val matchesSearch = searchText.isBlank() ||
                transaction.name.contains(searchText, ignoreCase = true)

        val matchesTags = selectedTags.isEmpty() ||
                selectedTags.any { tag -> transaction.tags.contains(tag) }

        matchesSearch && matchesTags
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // SEARCH BUTTON
                IconButton(onClick = {
                    isSearchExpanded = !isSearchExpanded
                    isTagFilterExpanded = false
                }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }

                // TAG FILTER BUTTON
                if (availableTags.isNotEmpty()) {
                    IconButton(onClick = {
                        isTagFilterExpanded = !isTagFilterExpanded
                        isSearchExpanded = false
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_filter),
                            contentDescription = "Filter tags",
                            tint = if (selectedTags.isNotEmpty()) Color(0xFF2196F3) else Color.Gray
                        )
                    }
                }

                Text(
                    "See All",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.clickable { onSeeAll() }
                )
            }
        }

        // Search bar
        if (isSearchExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            SearchBar(
                searchText = searchText,
                onSearchTextChanged = { searchText = it },
                onCloseClicked = {
                    searchText = ""
                    isSearchExpanded = false
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Tag Filter Section
        if (isTagFilterExpanded) {
            Spacer(modifier = Modifier.height(8.dp))
            TagFilterSection(
                availableTags = availableTags,
                selectedTags = selectedTags,
                onTagSelected = { tag ->
                    selectedTags = if (selectedTags.contains(tag))
                        selectedTags - tag
                    else
                        selectedTags + tag
                },
                onClearFilters = {
                    selectedTags = emptySet()
                    isTagFilterExpanded = false
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredTransactions.isEmpty()) {
            EmptyFilteredState(
                searchText = searchText,
                selectedTags = selectedTags,
                onClearFilters = {
                    searchText = ""
                    selectedTags = emptySet()
                    isSearchExpanded = false
                    isTagFilterExpanded = false
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredTransactions) { transaction ->
                    TransactionItem(transaction = transaction, currencySymbol = currencySymbol)
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchText: String,
    onSearchTextChanged: (String) -> Unit,
    onCloseClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color.Gray
            )

            Spacer(modifier = Modifier.width(8.dp))

            BasicTextField(
                value = searchText,
                onValueChange = onSearchTextChanged,
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (searchText.isEmpty()) {
                        Text("Search transactions...", color = Color.Gray)
                    }
                    innerTextField()
                },
                singleLine = true
            )

            if (searchText.isNotEmpty()) {
                IconButton(onClick = onCloseClicked) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun TagFilterSection(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onTagSelected: (String) -> Unit,
    onClearFilters: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Filter by Tags", fontWeight = FontWeight.Medium)

            if (selectedTags.isNotEmpty()) {
                Text(
                    "Clear",
                    color = Color(0xFF2196F3),
                    modifier = Modifier.clickable { onClearFilters() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(availableTags) { tag ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (selectedTags.contains(tag))
                                Color(0xFF2196F3)
                            else
                                Color.LightGray.copy(alpha = 0.3f)
                        )
                        .clickable { onTagSelected(tag) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "#$tag",
                        color = if (selectedTags.contains(tag)) Color.White else Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFilteredState(
    searchText: String,
    selectedTags: Set<String>,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No matching transactions", color = Color.Gray)

        val txt = buildString {
            if (searchText.isNotBlank()) append("Search: '$searchText'")
            if (selectedTags.isNotEmpty()) append(" Tags: ${selectedTags.joinToString()}")
        }

        Text(txt, color = Color.Gray, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Clear filters",
            color = Color(0xFF2196F3),
            modifier = Modifier.clickable { onClearFilters() }
        )
    }
}
